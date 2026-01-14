package com.fuding.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fuding.entity.BrowseHistory;

/**
 * 浏览历史服务接口
 */
public interface BrowseHistoryService extends IService<BrowseHistory> {
    
    /**
     * 记录浏览历史
     */
    void record(Long userId, Integer targetType, Long targetId, String title, String image);
    
    /**
     * 获取用户的浏览历史
     */
    IPage<BrowseHistory> getUserHistory(Page<BrowseHistory> page, Long userId);
    
    /**
     * 清空浏览历史
     */
    void clearHistory(Long userId);
}

