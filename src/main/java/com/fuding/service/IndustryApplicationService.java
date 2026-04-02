package com.fuding.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fuding.entity.IndustryApplication;

import java.util.Map;

public interface IndustryApplicationService {

    IndustryApplication submit(Long userId, Long activityId, Map<String, Object> params);

    IndustryApplication getMyApplication(Long userId, Long activityId);

    IPage<Map<String, Object>> getMyApplications(Page<IndustryApplication> page, Long userId, Integer status, Integer type);

    IPage<Map<String, Object>> adminList(Page<IndustryApplication> page, Integer status, Integer type, String keyword);

    IndustryApplication adminReview(Long applicationId, Integer status, String adminRemark);
}

