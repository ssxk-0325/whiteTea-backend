package com.fuding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fuding.entity.CommunityPostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 社区帖子点赞Mapper接口
 */
@Mapper
public interface CommunityPostLikeMapper extends BaseMapper<CommunityPostLike> {

    /**
     * 恢复已删除的点赞记录（绕过逻辑删除）
     */
    @Update("UPDATE tb_community_post_like SET deleted = 0 WHERE post_id = #{postId} AND user_id = #{userId} AND deleted = 1")
    int restoreDeletedLike(@Param("postId") Long postId, @Param("userId") Long userId);
}

