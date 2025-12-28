package com.fuding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fuding.entity.CommunityComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社区评论Mapper接口
 */
@Mapper
public interface CommunityCommentMapper extends BaseMapper<CommunityComment> {
}

