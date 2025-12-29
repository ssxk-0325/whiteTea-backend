package com.fuding.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fuding.entity.CultureContent;
import java.util.List;
import java.util.Map;

/**
 * 文化内容服务接口
 */
public interface CultureService {

    /**
     * 创建文化内容
     */
    CultureContent createContent(CultureContent content);

    /**
     * 获取内容列表（分页）
     */
    IPage<CultureContent> getContentList(Page<CultureContent> page, Integer contentType, Integer type, String keyword);

    /**
     * 获取内容详情
     */
    CultureContent getContentById(Long id);

    /**
     * 更新内容
     */
    CultureContent updateContent(CultureContent content);

    /**
     * 删除内容
     */
    void deleteContent(Long id);

    /**
     * 增加浏览量
     */
    void incrementViewCount(Long id);

    /**
     * 点赞内容
     */
    void likeContent(Long id);

    /**
     * 取消点赞
     */
    void unlikeContent(Long id);

    /**
     * 获取热门内容
     */
    List<CultureContent> getHotContents(Integer contentType, Integer limit);

    /**
     * 管理员获取内容列表（包括未发布的）
     */
    IPage<CultureContent> getAdminContentList(Page<CultureContent> page, Integer contentType, Integer type, String keyword);
}

