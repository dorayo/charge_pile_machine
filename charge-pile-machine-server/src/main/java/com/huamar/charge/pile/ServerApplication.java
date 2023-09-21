package com.huamar.charge.pile;


import com.huamar.charge.pile.config.PrintDocInfo;
import com.huamar.charge.pile.config.ServerApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.event.EventListener;

/**
 * 服务端程序入口
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.huamar.charge.pile.api")
@EnableConfigurationProperties(ServerApplicationProperties.class)
public class ServerApplication {

    public static void main(String[] args) {
        Thread.currentThread().setName("main");
        SpringApplication.run(ServerApplication.class, args);
    }

    /**
     * 打印系统相关信息
     * @param event event
     */
    @EventListener
    public void onApplicationEvent(ApplicationStartedEvent event) {
        PrintDocInfo.print(event.getApplicationContext());
    }

}