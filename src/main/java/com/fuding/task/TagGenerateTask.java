package com.fuding.task;

import com.fuding.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tag生成定时任务
 * 每天凌晨生成Top 10 Tag
 */
@Component
public class TagGenerateTask {

    private static final Logger logger = LoggerFactory.getLogger(TagGenerateTask.class);

    @Autowired
    private TagService tagService;

    /**
     * 定时任务：每天凌晨2点执行，生成Top 10 Tag
     * cron表达式：0 0 2 * * ? 表示每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateTopTags() {
        logger.info("开始执行Tag生成定时任务...");
        try {
            tagService.generateTopTags(10);
            logger.info("Tag生成定时任务完成");
        } catch (Exception e) {
            logger.error("执行Tag生成定时任务时发生错误：", e);
        }
    }
}

