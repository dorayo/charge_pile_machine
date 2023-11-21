package com.huamar.charge.machine;


import com.huamar.charge.machine.client.MachineAClient;
import com.huamar.charge.machine.client.MachineBClient;
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
import org.tio.core.Node;
import org.tio.core.Tio;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * 服务端程序入口
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@SuppressWarnings("DefaultAnnotationParam")
@EnableConfigurationProperties
@SpringBootApplication
@Slf4j
public class ClientNetApplication {

    @SneakyThrows
    public static void main(String[] args) {
        Thread.currentThread().setName("main");
        SpringApplication.run(ClientNetApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(0)
    public void listen(ApplicationReadyEvent event) throws Exception {
        MachineAClient a = event.getApplicationContext().getBean(MachineAClient.class);
        a.connect(new Node("121.36.36.155", 8889));

        MachineBClient b = event.getApplicationContext().getBean(MachineBClient.class);
        b.connect(new Node("121.36.36.155", 8889));

        TimeUnit.SECONDS.sleep(60);
        Tio.close(b.getClientChannelContext(), "close");
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(Integer.MAX_VALUE)
    public void listenReady(ApplicationReadyEvent event) {
        ClientNetApplication.print(event.getApplicationContext());
    }


    /**
     * 启动打印信息
     */
    @SneakyThrows
    private static void print(ApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = environment.getProperty("server.port");
        String path = environment.getProperty("server.servlet.context-path");
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("Application is running! Access URLs", "");
        at.addRule();
        at.addRow("Local:", "http://localhost:" + port + path + "doc.html");
        at.addRule();
        at.addRow("External:", "http://" + ip + ":" + port + path + "doc.html");
        at.addRule();
        at.addRow("Swagger doc :", "http://" + ip + ":" + port + path + "doc.html");
        at.addRule();
        log.info(System.getProperty("line.separator") + at.render());
    }
}