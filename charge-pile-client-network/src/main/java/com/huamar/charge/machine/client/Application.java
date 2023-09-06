package com.huamar.charge.machine.client;


import com.huamar.charge.machine.client.starter.MachineClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@Slf4j
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(0)
    public void listen(ApplicationReadyEvent event) throws Exception {
        MachineClient machineClient = event.getApplicationContext().getBean(MachineClient.class);
        machineClient.connect();
    }
}