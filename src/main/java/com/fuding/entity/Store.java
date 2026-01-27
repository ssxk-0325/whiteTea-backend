package com.fuding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fuding.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 门店实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tb_store")
public class Store extends BaseEntity {

    /**
     * 门店名称
     */
    @TableField("name")
    private String name;

    /**
     * 门店描述
     */
    @TableField("description")
    private String description;

    /**
     * 门店图片
     */
    @TableField("image")
    private String image;

    /**
     * 省份
     */
    @TableField("province")
    private String province;

    /**
     * 城市
     */
    @TableField("city")
    private String city;

    /**
     * 区县
     */
    @TableField("district")
    private String district;

    /**
     * 详细地址
     */
    @TableField("address")
    private String address;

    /**
     * 经度
     */
    @TableField("longitude")
    private BigDecimal longitude;

    /**
     * 纬度
     */
    @TableField("latitude")
    private BigDecimal latitude;

    /**
     * 联系电话
     */
    @TableField("phone")
    private String phone;

    /**
     * 营业时间
     */
    @TableField("business_hours")
    private String businessHours;

    /**
     * 状态：0-关闭，1-营业
     */
    @TableField("status")
    private Integer status = 1;
}

