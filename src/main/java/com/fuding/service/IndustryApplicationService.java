package com.fuding.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fuding.entity.IndustryApplication;

import java.util.Map;

public interface IndustryApplicationService {

    IndustryApplication submit(Long userId, Long activityId, Map<String, Object> params);

    IndustryApplication getMyApplication(Long userId, Long activityId);

    /**
     * 当前用户在指定活动下的报名详情（含审核通过后的对接信息；无报名返回 null）
     */
    Map<String, Object> getMyJoinDetail(Long userId, Long activityId);

    IPage<Map<String, Object>> getMyApplications(Page<IndustryApplication> page, Long userId, Integer status, Integer type);

    IPage<Map<String, Object>> adminList(Page<IndustryApplication> page, Integer status, Integer type, String keyword, Long activityId);

    IndustryApplication adminReview(Long applicationId, Integer status, String adminRemark);

    /** 采摘招募：管理员为已通过用户标记到岗签到 */
    IndustryApplication adminCheckInPick(Long applicationId);

    /**
     * 是否可浏览「培训专区」文化/问答：管理员，或存在任一已通过「批发与培训」(活动 type=6) 的产业报名
     */
    boolean canAccessTrainingZone(Long userId);
}

