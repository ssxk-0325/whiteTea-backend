package com.fuding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fuding.entity.ExperienceActivity;
import com.fuding.entity.IndustryApplication;
import com.fuding.entity.User;
import com.fuding.mapper.ExperienceActivityMapper;
import com.fuding.mapper.IndustryApplicationMapper;
import com.fuding.mapper.UserMapper;
import com.fuding.service.IndustryApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class IndustryApplicationServiceImpl extends ServiceImpl<IndustryApplicationMapper, IndustryApplication> implements IndustryApplicationService {

    @Autowired
    private IndustryApplicationMapper applicationMapper;

    @Autowired
    private ExperienceActivityMapper activityMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public IndustryApplication submit(Long userId, Long activityId, Map<String, Object> params) {
        ExperienceActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getStatus() == 3) {
            throw new RuntimeException("信息不存在或已取消");
        }
        if (activity.getType() == null || (activity.getType() != 5 && activity.getType() != 6)) {
            throw new RuntimeException("该内容不支持加入申请");
        }

        LambdaQueryWrapper<IndustryApplication> existsWrapper = new LambdaQueryWrapper<>();
        existsWrapper.eq(IndustryApplication::getUserId, userId)
                .eq(IndustryApplication::getActivityId, activityId);
        if (applicationMapper.selectCount(existsWrapper) > 0) {
            throw new RuntimeException("您已提交过申请，请等待审核");
        }

        String realName = params.get("realName") != null ? String.valueOf(params.get("realName")).trim() : "";
        String phone = params.get("phone") != null ? String.valueOf(params.get("phone")).trim() : "";
        String location = params.get("location") != null ? String.valueOf(params.get("location")).trim() : null;
        String remark = params.get("remark") != null ? String.valueOf(params.get("remark")).trim() : null;

        if (realName.isEmpty()) {
            throw new RuntimeException("请填写姓名");
        }
        if (phone.isEmpty()) {
            throw new RuntimeException("请填写手机号");
        }

        IndustryApplication app = new IndustryApplication();
        app.setUserId(userId);
        app.setActivityId(activityId);
        app.setRealName(realName);
        app.setPhone(phone);
        app.setLocation(location);
        app.setRemark(remark);
        app.setStatus(0);
        applicationMapper.insert(app);
        return app;
    }

    @Override
    public IndustryApplication getMyApplication(Long userId, Long activityId) {
        LambdaQueryWrapper<IndustryApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IndustryApplication::getUserId, userId)
                .eq(IndustryApplication::getActivityId, activityId)
                .orderByDesc(IndustryApplication::getCreateTime)
                .last("limit 1");
        return applicationMapper.selectOne(wrapper);
    }

    @Override
    public IPage<Map<String, Object>> adminList(Page<IndustryApplication> page, Integer status, Integer type, String keyword) {
        LambdaQueryWrapper<IndustryApplication> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(IndustryApplication::getStatus, status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(IndustryApplication::getRealName, kw)
                    .or().like(IndustryApplication::getPhone, kw)
                    .or().like(IndustryApplication::getLocation, kw)
                    .or().like(IndustryApplication::getRemark, kw));
        }
        wrapper.orderByDesc(IndustryApplication::getCreateTime);

        IPage<IndustryApplication> appPage = applicationMapper.selectPage(page, wrapper);
        List<IndustryApplication> apps = appPage.getRecords() != null ? appPage.getRecords() : Collections.emptyList();

        Set<Long> activityIds = apps.stream().map(IndustryApplication::getActivityId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> userIds = apps.stream().map(IndustryApplication::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, ExperienceActivity> activityMap = activityIds.isEmpty()
                ? Collections.emptyMap()
                : activityMapper.selectBatchIds(activityIds).stream().collect(Collectors.toMap(ExperienceActivity::getId, Function.identity(), (a, b) -> a));
        Map<Long, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (IndustryApplication app : apps) {
            ExperienceActivity act = app.getActivityId() != null ? activityMap.get(app.getActivityId()) : null;
            if (type != null) {
                Integer actType = act != null ? act.getType() : null;
                if (actType == null || !Objects.equals(actType, type)) {
                    continue;
                }
            }

            User u = app.getUserId() != null ? userMap.get(app.getUserId()) : null;
            Map<String, Object> row = new HashMap<>();
            row.put("id", app.getId());
            row.put("activityId", app.getActivityId());
            row.put("userId", app.getUserId());
            row.put("realName", app.getRealName());
            row.put("phone", app.getPhone());
            row.put("location", app.getLocation());
            row.put("remark", app.getRemark());
            row.put("status", app.getStatus());
            row.put("adminRemark", app.getAdminRemark());
            row.put("createTime", app.getCreateTime());

            row.put("activityName", act != null ? act.getName() : null);
            row.put("activityType", act != null ? act.getType() : null);
            row.put("username", u != null ? u.getUsername() : null);
            row.put("nickname", u != null ? u.getNickname() : null);

            rows.add(row);
        }

        Page<Map<String, Object>> voPage = new Page<>(appPage.getCurrent(), appPage.getSize(), appPage.getTotal());
        voPage.setRecords(rows);
        return voPage;
    }

    @Override
    public IndustryApplication adminReview(Long applicationId, Integer status, String adminRemark) {
        if (status == null || (status != 1 && status != 2)) {
            throw new RuntimeException("审核状态不合法");
        }
        IndustryApplication app = applicationMapper.selectById(applicationId);
        if (app == null) {
            throw new RuntimeException("申请不存在");
        }
        app.setStatus(status);
        app.setAdminRemark(adminRemark);
        applicationMapper.updateById(app);
        return app;
    }
}

