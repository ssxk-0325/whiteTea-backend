package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.BrowseHistory;
import com.fuding.mapper.BrowseHistoryMapper;
import com.fuding.service.BrowseHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 浏览历史服务实现类
 */
@Service
@Transactional
public class BrowseHistoryServiceImpl extends ServiceImpl<BrowseHistoryMapper, BrowseHistory> implements BrowseHistoryService {

    @Override
    public void record(Long userId, Integer targetType, Long targetId, String title, String image) {
        // 检查是否已有该内容的记录
        LambdaQueryWrapper<BrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistory::getUserId, userId);
        wrapper.eq(BrowseHistory::getTargetType, targetType);
        wrapper.eq(BrowseHistory::getTargetId, targetId);
        BrowseHistory existing = this.getOne(wrapper);

        if (existing != null) {
            // 更新时间
            existing.setUpdateTime(LocalDateTime.now());
            this.updateById(existing);
        } else {
            // 插入新记录
            BrowseHistory history = new BrowseHistory();
            history.setUserId(userId);
            history.setTargetType(targetType);
            history.setTargetId(targetId);
            history.setTitle(title);
            history.setImage(image);
            this.save(history);
        }
    }

    @Override
    public IPage<BrowseHistory> getUserHistory(Page<BrowseHistory> page, Long userId) {
        LambdaQueryWrapper<BrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistory::getUserId, userId);
        wrapper.orderByDesc(BrowseHistory::getUpdateTime);
        return this.page(page, wrapper);
    }

    @Override
    public void clearHistory(Long userId) {
        LambdaQueryWrapper<BrowseHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BrowseHistory::getUserId, userId);
        this.remove(wrapper);
    }
}

