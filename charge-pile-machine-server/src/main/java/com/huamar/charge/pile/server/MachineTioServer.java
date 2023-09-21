package com.huamar.charge.pile.server;


import com.huamar.charge.pile.config.ServerApplicationProperties;
import com.huamar.charge.pile.server.handle.tio.MachineHandler;
import com.huamar.charge.pile.server.handle.tio.TioMachIneAioListener;
import com.huamar.charge.pile.server.service.factory.MachinePacketFactory;
import com.huamar.charge.pile.server.session.context.TioSessionContext;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextClosedEvent;
import org.tio.server.ServerTioConfig;
import org.tio.server.TioServer;

/**
 * 服务端启动
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Import(TioSessionContext.class)
@Configuration
@ConditionalOnProperty(name = "machine.server.net-socket-model", havingValue = "tio")
public class MachineTioServer implements NetServer {

    private static final Logger log = LoggerFactory.getLogger(MachineTioServer.class);

    /**
     * 设备配置信息
     */
    private final ServerApplicationProperties properties;

    private final MachinePacketFactory machinePacketFactory;

    /**
     * 服务端入口
     */
    public static TioServer tioServer;

    /**
     * 一组连接共用的上下文对象
     */
    public static ServerTioConfig serverTioConfig;

    public MachineTioServer(ServerApplicationProperties properties, MachinePacketFactory machinePacketFactory) {
        this.properties = properties;
        this.machinePacketFactory = machinePacketFactory;
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> serverStart(){
        return event -> {
            MachineTioServer server = event.getApplicationContext().getBean(this.getClass());
            server.start();
            log.info("Server Net start ...{}", server.getClass().getName());
        };
    }

    @Bean
    public ApplicationListener<ContextClosedEvent> stopApplicationListener(){
        return event -> {
            event.getApplicationContext().getBean(this.getClass()).close();
            log.info("MachineNetServer close ...");
        };
    }


    @Bean
    public ServerTioConfig serverTioConfig() {
        // 一组连接共用的上下文对象
        serverTioConfig = new ServerTioConfig("MachineHandler Server", new MachineHandler(machinePacketFactory), new TioMachIneAioListener());
        return serverTioConfig;
    }

    /**
     * 启动程序入口
     */
    @SneakyThrows
    @Override
    public void start() {
        // 心跳超时时间
        serverTioConfig.setHeartbeatTimeout(properties.getTimeout().toMillis());
        tioServer = new TioServer(serverTioConfig);
        // 启动服务
        tioServer.start(properties.getHost(), properties.getPort());
    }

    /**
     * 关闭程序
     */
    @Override
    public void close() {
        tioServer.stop();
    }
}