package com.fuding.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.QuizAnswer;
import com.fuding.entity.QuizQuestion;
import com.fuding.mapper.QuizAnswerMapper;
import com.fuding.mapper.QuizQuestionMapper;
import com.fuding.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 趣味问答服务实现类
 */
@Service
@Transactional
public class QuizServiceImpl extends ServiceImpl<QuizQuestionMapper, QuizQuestion> implements QuizService {

    @Autowired
    private QuizQuestionMapper questionMapper;

    @Autowired
    private QuizAnswerMapper answerMapper;

    @Override
    public IPage<QuizQuestion> getQuestionList(Page<QuizQuestion> page, Integer category, Integer difficulty, String keyword, Long userId) {
        LambdaQueryWrapper<QuizQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuizQuestion::getStatus, 1); // 只查询已发布的
        if (category != null) {
            wrapper.eq(QuizQuestion::getCategory, category);
        }
        if (difficulty != null) {
            wrapper.eq(QuizQuestion::getDifficulty, difficulty);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(QuizQuestion::getQuestion, keyword);
        }
        wrapper.orderByDesc(QuizQuestion::getCreateTime);
        IPage<QuizQuestion> result = questionMapper.selectPage(page, wrapper);
        
        // 如果用户已登录，查询答题状态
        if (userId != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            List<Long> questionIds = result.getRecords().stream()
                    .map(QuizQuestion::getId)
                    .collect(Collectors.toList());
            
            // 批量查询用户的答题记录
            LambdaQueryWrapper<QuizAnswer> answerWrapper = new LambdaQueryWrapper<>();
            answerWrapper.eq(QuizAnswer::getUserId, userId);
            answerWrapper.in(QuizAnswer::getQuestionId, questionIds);
            List<QuizAnswer> userAnswers = answerMapper.selectList(answerWrapper);
            
            // 构建问题ID到答题记录的映射
            Map<Long, QuizAnswer> answerMap = userAnswers.stream()
                    .collect(Collectors.toMap(
                            QuizAnswer::getQuestionId,
                            answer -> answer,
                            (existing, replacement) -> existing // 如果有重复，保留第一个
                    ));
            
            // 为每个问题设置答题状态
            for (QuizQuestion question : result.getRecords()) {
                QuizAnswer answer = answerMap.get(question.getId());
                if (answer != null) {
                    // 1-已答正确，2-已答错误
                    question.setAnswerStatus(answer.getIsCorrect() == 1 ? 1 : 2);
                } else {
                    // 0-未答
                    question.setAnswerStatus(0);
                }
            }
        } else {
            // 未登录用户，所有问题都标记为未答
            for (QuizQuestion question : result.getRecords()) {
                question.setAnswerStatus(0);
            }
        }
        
        return result;
    }

    @Override
    public QuizQuestion getQuestionById(Long id) {
        QuizQuestion question = questionMapper.selectById(id);
        if (question == null || question.getStatus() != 1) {
            throw new RuntimeException("问题不存在或已删除");
        }
        // 增加答题次数
        question.setViewCount(question.getViewCount() + 1);
        questionMapper.updateById(question);
        return question;
    }

    @Override
    public QuizAnswer submitAnswer(Long userId, Long questionId, Integer userAnswer) {
        // 检查问题是否存在
        QuizQuestion question = questionMapper.selectById(questionId);
        if (question == null || question.getStatus() != 1) {
            throw new RuntimeException("问题不存在或已删除");
        }

        // 检查答案是否有效
        List<String> options = JSON.parseArray(question.getOptions(), String.class);
        if (userAnswer < 0 || userAnswer >= options.size()) {
            throw new RuntimeException("答案选项无效");
        }

        // 判断是否正确
        boolean isCorrect = question.getCorrectAnswer().equals(userAnswer);

        // 先尝试恢复逻辑删除的记录
        int restored = answerMapper.restoreDeletedAnswer(userId, questionId);

        // 检查是否已有答题记录
        LambdaQueryWrapper<QuizAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuizAnswer::getUserId, userId);
        wrapper.eq(QuizAnswer::getQuestionId, questionId);
        QuizAnswer existingAnswer = answerMapper.selectOne(wrapper);

        QuizAnswer answer;
        if (existingAnswer != null) {
            // 更新现有记录
            existingAnswer.setUserAnswer(userAnswer);
            existingAnswer.setIsCorrect(isCorrect ? 1 : 0);
            answerMapper.updateById(existingAnswer);
            answer = existingAnswer;
        } else {
            // 创建新记录
            answer = new QuizAnswer();
            answer.setUserId(userId);
            answer.setQuestionId(questionId);
            answer.setUserAnswer(userAnswer);
            answer.setIsCorrect(isCorrect ? 1 : 0);
            answerMapper.insert(answer);
        }

        // 更新问题的答对次数
        if (isCorrect && restored == 0) {
            // 只有新答对或恢复后第一次答对才增加
            question.setCorrectCount(question.getCorrectCount() + 1);
            questionMapper.updateById(question);
        }

        return answer;
    }

    @Override
    public List<QuizAnswer> getUserAnswers(Long userId, Integer category) {
        LambdaQueryWrapper<QuizAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuizAnswer::getUserId, userId);
        if (category != null) {
            // 需要关联查询问题表来过滤分类
            // 这里简化处理，先查询所有，后续可以优化
        }
        wrapper.orderByDesc(QuizAnswer::getCreateTime);
        return answerMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> getUserStatistics(Long userId) {
        LambdaQueryWrapper<QuizAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuizAnswer::getUserId, userId);
        List<QuizAnswer> allAnswers = answerMapper.selectList(wrapper);

        int totalCount = allAnswers.size();
        int correctCount = 0;
        for (QuizAnswer answer : allAnswers) {
            if (answer.getIsCorrect() == 1) {
                correctCount++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", totalCount);
        stats.put("correctCount", correctCount);
        stats.put("wrongCount", totalCount - correctCount);
        stats.put("correctRate", totalCount > 0 ? (double) correctCount / totalCount * 100 : 0);
        return stats;
    }

    @Override
    public IPage<QuizQuestion> getAdminQuestionList(Page<QuizQuestion> page, Integer category, Integer difficulty, String keyword) {
        LambdaQueryWrapper<QuizQuestion> wrapper = new LambdaQueryWrapper<>();
        // 管理员可以看到所有状态的问题
        if (category != null) {
            wrapper.eq(QuizQuestion::getCategory, category);
        }
        if (difficulty != null) {
            wrapper.eq(QuizQuestion::getDifficulty, difficulty);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(QuizQuestion::getQuestion, keyword);
        }
        wrapper.orderByDesc(QuizQuestion::getCreateTime);
        return questionMapper.selectPage(page, wrapper);
    }

    @Override
    public QuizQuestion createQuestion(Map<String, Object> params) {
        QuizQuestion question = new QuizQuestion();
        question.setQuestion(params.get("question").toString());
        question.setOptions(params.get("options").toString());
        question.setCorrectAnswer(Integer.valueOf(params.get("correctAnswer").toString()));
        question.setExplanation(params.get("explanation") != null ? params.get("explanation").toString() : null);
        question.setCategory(params.get("category") != null ? Integer.valueOf(params.get("category").toString()) : 1);
        question.setDifficulty(params.get("difficulty") != null ? Integer.valueOf(params.get("difficulty").toString()) : 1);
        question.setImage(params.get("image") != null ? params.get("image").toString() : null);
        question.setStatus(params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : 1);
        question.setViewCount(0);
        question.setCorrectCount(0);

        questionMapper.insert(question);
        return question;
    }

    @Override
    public QuizQuestion updateQuestion(Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        QuizQuestion question = questionMapper.selectById(id);
        if (question == null) {
            throw new RuntimeException("问题不存在");
        }

        if (params.get("question") != null) {
            question.setQuestion(params.get("question").toString());
        }
        if (params.get("options") != null) {
            question.setOptions(params.get("options").toString());
        }
        if (params.get("correctAnswer") != null) {
            question.setCorrectAnswer(Integer.valueOf(params.get("correctAnswer").toString()));
        }
        if (params.get("explanation") != null) {
            question.setExplanation(params.get("explanation").toString());
        }
        if (params.get("category") != null) {
            question.setCategory(Integer.valueOf(params.get("category").toString()));
        }
        if (params.get("difficulty") != null) {
            question.setDifficulty(Integer.valueOf(params.get("difficulty").toString()));
        }
        if (params.get("image") != null) {
            question.setImage(params.get("image").toString());
        }
        if (params.get("status") != null) {
            question.setStatus(Integer.valueOf(params.get("status").toString()));
        }

        questionMapper.updateById(question);
        return question;
    }

    @Override
    public void deleteQuestion(Long id) {
        questionMapper.deleteById(id);
    }
}

