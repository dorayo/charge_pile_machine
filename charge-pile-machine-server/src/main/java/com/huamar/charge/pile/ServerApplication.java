package com.huamar.charge.pile;


import com.huamar.charge.pile.server.MachineServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

/**
 * 服务端程序入口
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@EnableConfigurationProperties
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.huamar.charge.pile.api")
@Slf4j
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Order(0)
    @EventListener(ApplicationReadyEvent.class)
    public void listen(ApplicationReadyEvent event) throws Exception {
        MachineServer machineServer = event.getApplicationContext().getBean(MachineServer.class);
        machineServer.start();
    }

}