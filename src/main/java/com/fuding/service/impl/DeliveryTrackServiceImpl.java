package com.fuding.service.impl;

import com.fuding.entity.Order;
import com.fuding.service.DeliveryTrackService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 福鼎市域 bbox 内生成演示坐标；收货点为地址哈希确定性落点；中途 4 次随机跳点 + 最终送达，共 6 个位置、5 次变化。
 */
@Service
public class DeliveryTrackServiceImpl implements DeliveryTrackService {

    /** 福鼎市大致范围（东经北纬），用于「同城」演示点 */
    private static final double MIN_LON = 119.92;
    private static final double MAX_LON = 120.42;
    private static final double MIN_LAT = 26.88;
    private static final double MAX_LAT = 27.44;

    /** 每阶段时长（毫秒），超过后进入下一节点 */
    private static final long STEP_DURATION_MS = 120_000L;

    private static final String[] STEP_TITLES = {
            "快件已揽收（同城始发）",
            "运输中",
            "运输中",
            "运输中",
            "派送中",
            "已送达收货地址附近"
    };

    @Override
    public Map<String, Object> buildDeliveryTrack(Order order) {
        if (order == null) {
            return null;
        }
        if (order.getDeliveryType() != null && order.getDeliveryType() == 2) {
            return null;
        }
        if (order.getShipTime() == null || order.getStatus() == null || order.getStatus() < 2) {
            return null;
        }

        long seedBase = order.getId() * 31L;
        long shipEpoch = order.getShipTime().atZone(ZoneId.of("Asia/Shanghai")).toEpochSecond();
        long seed = seedBase ^ (shipEpoch * 1315423911L);

        double[] dest = destinationPoint(order);
        List<double[]> points = buildPathPoints(seed, dest);

        List<Map<String, Object>> steps = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            double[] p = points.get(i);
            double km = haversineKm(p[1], p[0], dest[1], dest[0]);
            Map<String, Object> step = new HashMap<>();
            step.put("index", i);
            step.put("longitude", round(p[0], 6));
            step.put("latitude", round(p[1], 6));
            step.put("distanceKm", roundMoney(km));
            step.put("title", STEP_TITLES[i]);
            steps.add(step);
        }

        int current = computeCurrentStepIndex(order);
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        LocalDateTime nextAt = null;
        if (current < 5) {
            nextAt = order.getShipTime().plus((current + 1) * STEP_DURATION_MS, ChronoUnit.MILLIS);
        }

        Map<String, Object> destMap = new HashMap<>();
        destMap.put("longitude", round(dest[0], 6));
        destMap.put("latitude", round(dest[1], 6));
        destMap.put("address", order.getReceiverAddress());

        Map<String, Object> data = new HashMap<>();
        data.put("cityLabel", cityLabelFromAddress(order.getReceiverAddress()));
        data.put("destination", destMap);
        data.put("steps", steps);
        data.put("currentStepIndex", current);
        data.put("delivered", current >= 5);
        data.put("stepDurationMs", STEP_DURATION_MS);
        data.put("nextStepAt", nextAt);
        data.put("serverTime", now);
        return data;
    }

    private int computeCurrentStepIndex(Order order) {
        if (order.getStatus() != null && order.getStatus() >= 3) {
            return 5;
        }
        LocalDateTime ship = order.getShipTime();
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        long elapsedMs = java.time.Duration.between(ship, now).toMillis();
        if (elapsedMs < 0) {
            return 0;
        }
        int idx = (int) (elapsedMs / STEP_DURATION_MS);
        if (idx > 5) {
            idx = 5;
        }
        return idx;
    }

    private String cityLabelFromAddress(String address) {
        if (address == null) {
            return "福鼎市";
        }
        if (address.contains("福鼎")) {
            return "福鼎市";
        }
        if (address.contains("宁德")) {
            return "宁德市";
        }
        return "同城（演示）";
    }

    /** 收货点：由地址与订单 ID 确定性映射到市域内一点 */
    private double[] destinationPoint(Order order) {
        String addr = order.getReceiverAddress() == null ? "" : order.getReceiverAddress();
        long h = addr.hashCode();
        h ^= (long) order.getId() * 0x9E3779B9L;
        Random r = new Random(h);
        return randomInBBox(r);
    }

    private List<double[]> buildPathPoints(long seed, double[] dest) {
        List<double[]> list = new ArrayList<>(6);
        Random rPath = new Random(seed);

        double[] p0 = randomStart(rPath, dest);
        list.add(p0);

        for (int i = 1; i <= 4; i++) {
            Random ri = new Random(seed + i * 10007L);
            list.add(randomInBBox(ri));
        }
        list.add(new double[]{dest[0], dest[1]});
        return list;
    }

    /** 首点：同城随机，与收货点保持一定距离 */
    private double[] randomStart(Random rnd, double[] dest) {
        for (int k = 0; k < 40; k++) {
            double[] p = randomInBBox(rnd);
            if (haversineKm(p[1], p[0], dest[1], dest[0]) >= 1.2) {
                return p;
            }
        }
        double[] p = randomInBBox(rnd);
        p[0] = clamp(p[0] + (rnd.nextBoolean() ? 0.02 : -0.02), MIN_LON, MAX_LON);
        p[1] = clamp(p[1] + (rnd.nextBoolean() ? 0.015 : -0.015), MIN_LAT, MAX_LAT);
        return p;
    }

    private double[] randomInBBox(Random rnd) {
        double lon = MIN_LON + rnd.nextDouble() * (MAX_LON - MIN_LON);
        double lat = MIN_LAT + rnd.nextDouble() * (MAX_LAT - MIN_LAT);
        return new double[]{lon, lat};
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static double round(double v, int scale) {
        BigDecimal b = BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP);
        return b.doubleValue();
    }

    private static BigDecimal roundMoney(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }
}
