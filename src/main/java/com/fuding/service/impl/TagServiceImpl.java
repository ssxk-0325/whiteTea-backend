package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fuding.entity.CustomerServiceQuestion;
import com.fuding.entity.CustomerServiceTag;
import com.fuding.mapper.CustomerServiceQuestionMapper;
import com.fuding.mapper.CustomerServiceTagMapper;
import com.fuding.service.AIService;
import com.fuding.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tag服务实现类
 */
@Service
public class TagServiceImpl implements TagService {

    private static final Logger logger = LoggerFactory.getLogger(TagServiceImpl.class);

    @Autowired
    private CustomerServiceQuestionMapper questionMapper;

    @Autowired
    private CustomerServiceTagMapper tagMapper;

    @Autowired
    private AIService aiService;

    @Override
    @Transactional
    public void recordQuestion(String question, Long userId) {
        try {
            // 计算问题哈希值（用于去重）
            String questionHash = calculateHash(question);

            // 查找是否已存在相同问题
            LambdaQueryWrapper<CustomerServiceQuestion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CustomerServiceQuestion::getQuestionHash, questionHash);
            CustomerServiceQuestion existingQuestion = questionMapper.selectOne(wrapper);

            if (existingQuestion != null) {
                // 更新统计信息
                existingQuestion.setAskCount(existingQuestion.getAskCount() + 1);
                existingQuestion.setLastAskTime(LocalDateTime.now());
                // 注意：这里简化处理，实际应该统计不同用户数
                questionMapper.updateById(existingQuestion);
            } else {
                // 创建新问题记录
                CustomerServiceQuestion newQuestion = new CustomerServiceQuestion();
                newQuestion.setQuestion(question);
                newQuestion.setQuestionHash(questionHash);
                newQuestion.setAskCount(1);
                newQuestion.setUserCount(1);
                newQuestion.setLastAskTime(LocalDateTime.now());
                questionMapper.insert(newQuestion);
            }
        } catch (Exception e) {
            logger.error("记录问题失败：", e);
        }
    }

    @Override
    @Transactional
    public void generateTopTags(int topN) {
        try {
            logger.info("开始生成Top {} Tag...", topN);

            // 1. 获取最近7天的问题统计
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
            LambdaQueryWrapper<CustomerServiceQuestion> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(CustomerServiceQuestion::getLastAskTime, sevenDaysAgo);
            wrapper.orderByDesc(CustomerServiceQuestion::getAskCount);
            wrapper.last("LIMIT " + (topN * 3)); // 获取更多候选问题

            List<CustomerServiceQuestion> questions = questionMapper.selectList(wrapper);

            if (questions == null || questions.isEmpty()) {
                logger.info("没有足够的问题数据生成Tag");
                return;
            }

            // 2. 对问题进行聚类和去重（简化版：按问题相似度分组）
            List<CustomerServiceQuestion> topQuestions = questions.stream()
                    .limit(topN)
                    .collect(Collectors.toList());

            // 3. 为每个问题生成Tag和答案
            List<CustomerServiceTag> tags = new ArrayList<>();
            for (int i = 0; i < topQuestions.size() && i < topN; i++) {
                CustomerServiceQuestion question = topQuestions.get(i);

                // 检查是否已存在相同Tag
                LambdaQueryWrapper<CustomerServiceTag> tagWrapper = new LambdaQueryWrapper<>();
                tagWrapper.eq(CustomerServiceTag::getTagName, question.getQuestion());
                CustomerServiceTag existingTag = tagMapper.selectOne(tagWrapper);

                if (existingTag != null) {
                    // 更新Tag信息
                    existingTag.setQuestionCount(question.getAskCount());
                    existingTag.setScore(calculateTagScore(question));
                    existingTag.setSortOrder(i + 1);
                    tagMapper.updateById(existingTag);
                    tags.add(existingTag);
                } else {
                    // 生成新Tag
                    CustomerServiceTag tag = new CustomerServiceTag();
                    tag.setTagName(question.getQuestion());
                    tag.setTagDescription("热门问题：" + question.getQuestion());
                    
                    // 调用AI生成答案
                    String answer = aiService.generateReply(question.getQuestion(), new ArrayList<>());
                    tag.setAnswer(answer);
                    
                    tag.setQuestionCount(question.getAskCount());
                    tag.setScore(calculateTagScore(question));
                    tag.setSortOrder(i + 1);
                    tag.setStatus(1);
                    tagMapper.insert(tag);
                    tags.add(tag);
                }
            }

            // 4. 禁用不在Top N中的Tag
            List<Long> activeTagIds = tags.stream()
                    .map(CustomerServiceTag::getId)
                    .collect(Collectors.toList());

            if (!activeTagIds.isEmpty()) {
                LambdaUpdateWrapper<CustomerServiceTag> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.notIn(CustomerServiceTag::getId, activeTagIds);
                updateWrapper.set(CustomerServiceTag::getStatus, 0);
                tagMapper.update(null, updateWrapper);
            }

            logger.info("成功生成 {} 个Tag", tags.size());
        } catch (Exception e) {
            logger.error("生成Tag失败：", e);
        }
    }

    @Override
    public List<CustomerServiceTag> getTopTags(int topN) {
        LambdaQueryWrapper<CustomerServiceTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerServiceTag::getStatus, 1);
        wrapper.orderByDesc(CustomerServiceTag::getScore);
        wrapper.orderByAsc(CustomerServiceTag::getSortOrder);
        wrapper.last("LIMIT " + topN);
        return tagMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public String clickTag(Long tagId) {
        CustomerServiceTag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            return null;
        }

        // 增加点击次数
        tag.setHitCount(tag.getHitCount() + 1);
        tagMapper.updateById(tag);

        return tag.getAnswer();
    }

    @Override
    public String getTagAnswer(Long tagId) {
        CustomerServiceTag tag = tagMapper.selectById(tagId);
        return tag != null ? tag.getAnswer() : null;
    }

    /**
     * 计算Tag得分
     * 得分 = 提问次数 × 时间衰减系数
     */
    private BigDecimal calculateTagScore(CustomerServiceQuestion question) {
        int askCount = question.getAskCount();
        LocalDateTime lastAskTime = question.getLastAskTime();
        
        // 时间衰减系数：距离现在越近，系数越大（最大1.0）
        long hoursAgo = ChronoUnit.HOURS.between(lastAskTime, LocalDateTime.now());
        double timeDecay = Math.max(0.5, 1.0 - (hoursAgo / 168.0)); // 7天内线性衰减
        
        return BigDecimal.valueOf(askCount * timeDecay);
    }

    /**
     * 计算字符串哈希值
     */
    private String calculateHash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("计算哈希值失败：", e);
            return String.valueOf(text.hashCode());
        }
    }
}

