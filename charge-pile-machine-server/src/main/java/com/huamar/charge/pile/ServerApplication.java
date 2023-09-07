package com.huamar.charge.pile;


import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.pile.server.MachineServer;
import de.vandermeer.asciitable.AsciiTable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.InetAddress;

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

    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
        print();
    }

    @Order(0)
    @EventListener(ApplicationReadyEvent.class)
    public void listen(ApplicationReadyEvent event) throws Exception {
        applicationContext = event.getApplicationContext();
        MachineServer machineServer = event.getApplicationContext().getBean(MachineServer.class);
        machineServer.start();
    }


    /**
     * 启动打印信息
     */
    @SneakyThrows
    private static void print(){
        Environment environment = applicationContext.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = environment.getProperty("server.port");
        String path = environment.getProperty("server.servlet.context-path", StringPool.EMPTY);

        UriComponentsBuilder local = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(port)
                .path(path);

        UriComponentsBuilder external = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(ip)
                .port(port)
                .path(path);

        UriComponentsBuilder swagger = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(ip)
                .port(port)
                .path(path)
                .path("doc.html");

        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("Application is running! Access URLs", "The format is wrong when using Chinese");
        at.addRule();
        at.addRow("Local:",local.build().toString());
        at.addRule();
        at.addRow("External:", external.build().toString());
        at.addRule();
        at.addRow("Swagger Api :", swagger.build().toString());
        at.addRule();
        log.info(System.getProperty("line.separator") + at.render());
    }
}