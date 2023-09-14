package com.huamar.charge.pile.server;


import com.huamar.charge.pile.config.ServerApplicationProperties;
import com.huamar.charge.pile.server.handle.tio.MachineHandler;
import com.huamar.charge.pile.server.handle.tio.TioMachIneAioListener;
import com.huamar.charge.pile.server.service.factory.MachinePacketFactory;
import com.huamar.charge.pile.server.session.context.TioSessionContext;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.tio.server.ServerTioConfig;
import org.tio.server.TioServer;
import org.tio.server.intf.ServerAioHandler;

/**
 * 服务端启动
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Import(TioSessionContext.class)
@Configuration
@ConditionalOnProperty(name = "machine.server.net-socket-model", havingValue = "TIO")
public class MachineTioServer {

    /**
     * 设备配置信息
     */
    private final ServerApplicationProperties properties;

    /**
     * 服务端入口
     */
    public static TioServer tioServer;


    /**
     * 一组连接共用的上下文对象
     */
    public static ServerTioConfig serverTioConfig;

    public MachineTioServer(ServerApplicationProperties properties) {
        this.properties = properties;
    }


    @Bean
    public ServerAioHandler serverAioHandler(MachinePacketFactory machinePacketFactory) {
        return new MachineHandler(machinePacketFactory);
    }

    @Bean
    public ServerTioConfig serverTioConfig(ServerAioHandler serverAioHandler) {
        // 一组连接共用的上下文对象
        serverTioConfig = new ServerTioConfig("MachineHandler Server", serverAioHandler, new TioMachIneAioListener());
        return serverTioConfig;
    }

    /**
     * 启动程序入口
     */
    @SneakyThrows
    public void start() {
        // 心跳超时时间
        serverTioConfig.setHeartbeatTimeout(60 * 1000);
        tioServer = new TioServer(serverTioConfig);
        // 启动服务
        tioServer.start(properties.getHost(), properties.getPort());
    }
}