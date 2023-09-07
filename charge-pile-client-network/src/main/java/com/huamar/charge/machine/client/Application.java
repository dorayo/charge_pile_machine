package com.huamar.charge.machine.client;


import com.huamar.charge.machine.client.starter.MachineClient;
import de.vandermeer.asciitable.AsciiTable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

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

    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        print();
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(0)
    public void listen(ApplicationReadyEvent event) throws Exception {
        applicationContext = event.getApplicationContext();
        MachineClient machineClient = event.getApplicationContext().getBean(MachineClient.class);
        machineClient.connect();
    }

    /**
     * 启动打印信息
     */
    @SneakyThrows
    private static void print(){
        Environment environment = applicationContext.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = environment.getProperty("server.port");
        String path = environment.getProperty("server.servlet.context-path");
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("Application is running! Access URLs", "");
        at.addRule();
        at.addRow("Local:","http://localhost:" + port + path + "doc.html");
        at.addRule();
        at.addRow("External:", "http://" + ip + ":" + port + path + "doc.html");
        at.addRule();
        at.addRow("Swagger doc :", "http://" + ip + ":" + port + path + "doc.html");
        at.addRule();
        log.info(System.getProperty("line.separator") + at.render());
    }
}