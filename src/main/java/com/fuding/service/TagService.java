package com.fuding.service;

import com.fuding.entity.CustomerServiceTag;

import java.util.List;

/**
 * Tag服务接口
 */
public interface TagService {

    /**
     * 记录用户问题（用于统计）
     */
    void recordQuestion(String question, Long userId);

    /**
     * 生成Top N Tag
     */
    void generateTopTags(int topN);

    /**
     * 获取Top N Tag列表
     */
    List<CustomerServiceTag> getTopTags(int topN);

    /**
     * 点击Tag，返回答案并增加点击次数
     */
    String clickTag(Long tagId);

    /**
     * 根据Tag获取答案
     */
    String getTagAnswer(Long tagId);
}

