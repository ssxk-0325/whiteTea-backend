package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.CultureContent;
import com.fuding.mapper.CultureContentMapper;
import com.fuding.service.CultureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文化内容服务实现类
 */
@Service
@Transactional
public class CultureServiceImpl extends ServiceImpl<CultureContentMapper, CultureContent> implements CultureService {

    @Autowired
    private CultureContentMapper contentMapper;

    @Override
    public CultureContent createContent(CultureContent content) {
        content.setViewCount(0);
        content.setLikeCount(0);
        content.setStatus(1); // 默认发布
        contentMapper.insert(content);
        return content;
    }

    @Override
    public IPage<CultureContent> getContentList(Page<CultureContent> page, Integer contentType, Integer type, String keyword) {
        LambdaQueryWrapper<CultureContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CultureContent::getStatus, 1); // 只查询已发布的
        if (contentType != null) {
            wrapper.eq(CultureContent::getContentType, contentType);
        }
        if (type != null) {
            wrapper.eq(CultureContent::getType, type);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(CultureContent::getTitle, keyword)
                    .or().like(CultureContent::getContent, keyword));
        }
        wrapper.orderByDesc(CultureContent::getCreateTime);
        return contentMapper.selectPage(page, wrapper);
    }

    @Override
    public CultureContent getContentById(Long id) {
        CultureContent content = contentMapper.selectById(id);
        if (content == null || content.getStatus() != 1) {
            throw new RuntimeException("内容不存在或已删除");
        }
        return content;
    }

    @Override
    public CultureContent updateContent(CultureContent content) {
        CultureContent existing = contentMapper.selectById(content.getId());
        if (existing == null) {
            throw new RuntimeException("内容不存在");
        }
        contentMapper.updateById(content);
        return content;
    }

    @Override
    public void deleteContent(Long id) {
        contentMapper.deleteById(id);
    }

    @Override
    public void incrementViewCount(Long id) {
        LambdaUpdateWrapper<CultureContent> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CultureContent::getId, id)
                .setSql("view_count = view_count + 1");
        contentMapper.update(null, wrapper);
    }

    @Override
    public void likeContent(Long id) {
        LambdaUpdateWrapper<CultureContent> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CultureContent::getId, id)
                .setSql("like_count = like_count + 1");
        contentMapper.update(null, wrapper);
    }

    @Override
    public void unlikeContent(Long id) {
        LambdaUpdateWrapper<CultureContent> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CultureContent::getId, id)
                .setSql("like_count = GREATEST(like_count - 1, 0)");
        contentMapper.update(null, wrapper);
    }

    @Override
    public List<CultureContent> getHotContents(Integer contentType, Integer limit) {
        LambdaQueryWrapper<CultureContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CultureContent::getStatus, 1);
        if (contentType != null) {
            wrapper.eq(CultureContent::getContentType, contentType);
        }
        wrapper.orderByDesc(CultureContent::getViewCount);
        wrapper.last("LIMIT " + (limit != null ? limit : 10));
        return contentMapper.selectList(wrapper);
    }

    @Override
    public IPage<CultureContent> getAdminContentList(Page<CultureContent> page, Integer contentType, Integer type, String keyword) {
        LambdaQueryWrapper<CultureContent> wrapper = new LambdaQueryWrapper<>();
        // 管理员可以看到所有状态的内容，不限制status
        if (contentType != null) {
            wrapper.eq(CultureContent::getContentType, contentType);
        }
        if (type != null) {
            wrapper.eq(CultureContent::getType, type);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(CultureContent::getTitle, keyword)
                    .or().like(CultureContent::getContent, keyword));
        }
        wrapper.orderByDesc(CultureContent::getCreateTime);
        return contentMapper.selectPage(page, wrapper);
    }
}

