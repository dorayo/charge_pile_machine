package com.huamar.charge.pile;


import com.huamar.charge.pile.config.PrintDocInfo;
import com.huamar.charge.pile.config.ServerApplicationProperties;
import com.huamar.charge.pile.enums.LoggerEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 服务端程序入口
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.huamar.charge.pile.api")
@EnableConfigurationProperties(ServerApplicationProperties.class)
public class ServerApplication {

    private static final Logger logger = LoggerFactory.getLogger(LoggerEnum.APPLICATION_MAIN_LOGGER.getCode());

    public static void main(String[] args) {
        Thread.currentThread().setName("main");
        SpringApplication.run(ServerApplication.class, args);
        logger.info("ServerApplication start success...");
    }

    /**
     * 打印系统相关信息
     *
     * @param event event
     */
    @EventListener
    public void onApplicationEvent(ApplicationStartedEvent event) {
        PrintDocInfo.print(event.getApplicationContext());
    }


    /**
     * 日志测试
     */
    static class LogFileRecursiveCleaner {
        public static boolean isOldFile(File file) {
            // 删除7天前的文件
            long daysAgo = 7;
            try {
                BasicFileAttributes attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                FileTime fileTime = attrs.lastModifiedTime();
                LocalDateTime lastModified = fileTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime threshold = LocalDateTime.now().minusDays(daysAgo);
                return lastModified.isBefore(threshold);
            } catch (IOException e) {
                logger.error("Error reading attributes for file: " + file.getAbsolutePath());
                return false;
            }
        }
    }

}