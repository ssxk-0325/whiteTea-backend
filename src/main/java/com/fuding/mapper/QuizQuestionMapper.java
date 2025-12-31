package com.fuding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fuding.entity.QuizQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 趣味问答问题Mapper接口
 */
@Mapper
public interface QuizQuestionMapper extends BaseMapper<QuizQuestion> {
}

