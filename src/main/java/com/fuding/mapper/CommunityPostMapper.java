package com.fuding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fuding.entity.CommunityPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社区帖子Mapper接口
 */
@Mapper
public interface CommunityPostMapper extends BaseMapper<CommunityPost> {
}

