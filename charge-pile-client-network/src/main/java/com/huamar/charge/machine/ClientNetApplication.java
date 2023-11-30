package com.huamar.charge.machine;


import com.huamar.charge.common.protocol.BasePacket;
import com.huamar.charge.machine.netty.MachineNetClient;
import de.vandermeer.asciitable.AsciiTable;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

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
        ConfigurableApplicationContext applicationContext = SpringApplication.run(ClientNetApplication.class, args);

        List<MachineNetClient> clientList = ClientNetApplication.connect();
        ClientNetApplication.sendPacket(clientList);
        clientList.forEach(MachineNetClient::close);

        applicationContext.close();

    }


    @EventListener(ApplicationReadyEvent.class)
    @Order(Integer.MAX_VALUE)
    public void listenReady(ApplicationReadyEvent event) {
        ClientNetApplication.print(event.getApplicationContext());
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(Integer.MAX_VALUE)
    public void listen(ApplicationReadyEvent event) throws Exception {

    }


    /**
     * 测试包发送
     *
     * @param clients clients
     */
    @SuppressWarnings("DuplicatedCode")
    private static void sendPacket(List<MachineNetClient> clients) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
        List<FutureTask<TcpTask>> futureTasks = new ArrayList<>();
        clients.forEach(var -> {

            FutureTask<TcpTask> futureTask = new FutureTask<>(() -> {
                try {
                    cyclicBarrier.await();

                    // 并发任务开始
                    TcpTask task = new TcpTask();
                    BasePacket basePacket = var.heartbeatPacket();
                    var.getChannel().writeAndFlush(basePacket).sync();
                    task.setEnd(LocalDateTime.now());
                    return task;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            });


            futureTasks.add(futureTask);
            new Thread(futureTask).start();

        });


        List<Long> timeCount = new ArrayList<>();
        futureTasks.forEach(var -> {
            try {
                TcpTask task = var.get();
                long seconds = Duration.between(task.start, task.end).getSeconds();
                if (seconds <= 2) {
                    seconds = 2;
                } else if (seconds <= 5) {
                    seconds = 5;
                } else if (seconds <= 10) {
                    seconds = 10;
                } else {
                    seconds = 100;
                }
                timeCount.add(seconds);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });


        Map<Long, Long> map = timeCount.stream().collect(Collectors.groupingBy(p -> p, Collectors.counting()));
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        joiner.add("========== 数据包发送测试 ===========");
        joiner.add("2S内返回：  " + map.getOrDefault(2L, 0L) + "占比： " + map.getOrDefault(2L, 0L) / timeCount.size() * 100);
        joiner.add("5S内返回：  " + map.getOrDefault(5L, 0L) + "占比： " + map.getOrDefault(5L, 0L) / timeCount.size() * 100);
        joiner.add("10S内返回： " + map.getOrDefault(10L, 0L) + "占比： " + map.getOrDefault(10L, 0L) / timeCount.size() * 100);
        joiner.add("超时内返回： " + map.getOrDefault(100L, 0L) + "占比； " + map.getOrDefault(100L, 0L) / timeCount.size() * 100);
        log.info("{}{}", System.lineSeparator(), joiner);
    }


    private static List<MachineNetClient> connect(){
        int threadCount = 100;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
        List<FutureTask<TcpTask>> futureTasks = new ArrayList<>();
        List<MachineNetClient> clients = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            MachineNetClient client = new MachineNetClient("127.0.0.1", 8886, String.format("%s-%s", "client", i), String.format("%018d", i));
            clients.add(client);

            FutureTask<TcpTask> futureTask = new FutureTask<>(() -> {
                cyclicBarrier.await();

                // 并发任务开始
                TcpTask task = new TcpTask();
                client.start();
                task.setEnd(LocalDateTime.now());
                return task;

            });

            futureTasks.add(futureTask);
            new Thread(futureTask).start();
        }


        List<Long> timeCount = new ArrayList<>();
        futureTasks.forEach(var -> {
            try {
                TcpTask task = var.get();
                long seconds = Duration.between(task.start, task.end).getSeconds();
                if (seconds <= 2) {
                    seconds = 2;
                } else if (seconds <= 5) {
                    seconds = 5;
                } else if (seconds <= 10) {
                    seconds = 10;
                } else {
                    seconds = 100;
                }
                timeCount.add(seconds);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });


        Map<Long, Long> map = timeCount.stream().collect(Collectors.groupingBy(p -> p, Collectors.counting()));
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        joiner.add("2S内返回：  " + map.getOrDefault(2L, 0L) + "占比： " + map.getOrDefault(2L, 0L) / timeCount.size() * 100);
        joiner.add("5S内返回：  " + map.getOrDefault(5L, 0L) + "占比： " + map.getOrDefault(5L, 0L) / timeCount.size() * 100);
        joiner.add("10S内返回： " + map.getOrDefault(10L, 0L) + "占比： " + map.getOrDefault(10L, 0L) / timeCount.size() * 100);
        joiner.add("超时内返回： " + map.getOrDefault(100L, 0L) + "占比； " + map.getOrDefault(100L, 0L) / timeCount.size() * 100);
        log.info("{}{}", System.lineSeparator(), joiner);

        return clients;
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

    @Data
    private static class TcpTask {

        private final LocalDateTime start = LocalDateTime.now();

        private LocalDateTime end;

        public TcpTask() {
        }
    }

}