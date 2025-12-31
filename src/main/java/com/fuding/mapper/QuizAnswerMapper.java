package com.fuding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fuding.entity.QuizAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户答题记录Mapper接口
 */
@Mapper
public interface QuizAnswerMapper extends BaseMapper<QuizAnswer> {

    /**
     * 恢复已删除的答题记录（绕过逻辑删除）
     */
    @Update("UPDATE tb_quiz_answer SET deleted = 0 WHERE user_id = #{userId} AND question_id = #{questionId} AND deleted = 1")
    int restoreDeletedAnswer(@Param("userId") Long userId, @Param("questionId") Long questionId);
}

