package com.fuding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fuding.entity.CommunityPostFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 社区帖子收藏Mapper接口
 */
@Mapper
public interface CommunityPostFavoriteMapper extends BaseMapper<CommunityPostFavorite> {

    /**
     * 恢复已删除的收藏记录（绕过逻辑删除）
     */
    @Update("UPDATE tb_community_post_favorite SET deleted = 0 WHERE post_id = #{postId} AND user_id = #{userId} AND deleted = 1")
    int restoreDeletedFavorite(@Param("postId") Long postId, @Param("userId") Long userId);
}

