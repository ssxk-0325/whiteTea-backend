package com.fuding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fuding.entity.CustomerServiceMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客服消息Mapper接口
 */
@Mapper
public interface CustomerServiceMessageMapper extends BaseMapper<CustomerServiceMessage> {
}

