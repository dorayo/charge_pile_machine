package com.huamar.charge.pile;


import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.pile.config.ServerApplicationProperties;
import com.huamar.charge.pile.server.MachineNetServer;
import de.vandermeer.asciitable.AsciiTable;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
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
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.huamar.charge.pile.api")
@EnableConfigurationProperties(ServerApplicationProperties.class)
public class ServerApplication {

    private final static Logger log = LoggerFactory.getLogger(ServerApplication.class);


    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }


    @Bean
    @Order(100)
    public ApplicationListener<ApplicationReadyEvent> serverStart(){
        return event -> {
            //event.getApplicationContext().getBean(MachineTioServer.class).start();
            MachineNetServer netServer = event.getApplicationContext().getBean(MachineNetServer.class);
            netServer.start();
            log.info("Server Net start ...{}", netServer.getClass().getName());
            print(event.getApplicationContext());
        };
    }

    @Bean
    @Order(100)
    public ApplicationListener<ContextClosedEvent> stopApplicationListener(){
        return event -> {
            event.getApplicationContext().getBean(MachineNetServer.class).close();
            log.info("MachineNetServer close ...");
        };
    }


    /**
     * 启动打印信息
     */
    @SneakyThrows
    private static void print(ApplicationContext applicationContext){
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