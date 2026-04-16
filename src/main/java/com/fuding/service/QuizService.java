package com.fuding.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fuding.entity.QuizAnswer;
import com.fuding.entity.QuizQuestion;

import java.util.List;
import java.util.Map;

/**
 * 趣味问答服务接口
 */
public interface QuizService {

    /**
     * 获取问题列表（分页）
     */
    IPage<QuizQuestion> getQuestionList(Page<QuizQuestion> page, Integer category, Integer difficulty, String keyword, Long userId);

    /**
     * 获取问题详情
     */
    QuizQuestion getQuestionById(Long id);

    /**
     * 提交答案
     */
    QuizAnswer submitAnswer(Long userId, Long questionId, Integer userAnswer);

    /**
     * 获取用户的答题记录
     */
    List<QuizAnswer> getUserAnswers(Long userId, Integer category);

    /**
     * 获取用户的答题统计
     */
    Map<String, Object> getUserStatistics(Long userId);

    /**
     * 管理员获取问题列表（包括所有状态）
     */
    IPage<QuizQuestion> getAdminQuestionList(Page<QuizQuestion> page, Integer category, Integer difficulty, String keyword, Integer status);

    /**
     * 管理员创建问题
     */
    QuizQuestion createQuestion(Map<String, Object> params);

    /**
     * 管理员更新问题
     */
    QuizQuestion updateQuestion(Map<String, Object> params);

    /**
     * 管理员删除问题
     */
    void deleteQuestion(Long id);
}

