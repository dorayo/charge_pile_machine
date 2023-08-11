package com.huamar.charge.pile.server;


import com.huamar.charge.pile.server.handle.MachineHandler;
import com.huamar.charge.pile.server.listener.MachIneAioListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.tio.server.ServerTioConfig;
import org.tio.server.TioServer;
import org.tio.server.intf.ServerAioHandler;
import org.tio.server.intf.ServerAioListener;

import java.io.IOException;

/**
 * 服务端启动
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
public class MachineServer implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;


    /**
     * 设备配置信息
     */
    private MachineProperties properties;

    /**
     * 服务端入口
     */
    public static TioServer tioServer;

    /**
     * handler, 包括编码、解码、消息处理
     */
    public static ServerAioHandler serverAioHandler;

    /**
     * 事件监听器，可以为null，但建议自己实现该接口，可以参考showcase了解些接口
     */
    public static ServerAioListener serverAioListener;

    /**
     * 一组连接共用的上下文对象
     */
    public static ServerTioConfig serverTioConfig;

    /**
     * 启动程序入口
     */
    public void start() throws IOException {
        // 心跳超时时间
        serverTioConfig.setHeartbeatTimeout(60 * 1000);
        tioServer = new TioServer(serverTioConfig);
        // 启动服务
        tioServer.start(properties.getHost(), properties.getPort());
    }


    @Override
    public void afterPropertiesSet() {
        // 事件监听器，可以为null，但建议自己实现该接口，可以参考showcase了解些接口
        serverAioListener = applicationContext.getBean(MachIneAioListener.class);
        // handler, 包括编码、解码、消息处理
        serverAioHandler = applicationContext.getBean(MachineHandler.class);

        properties = applicationContext.getBean(MachineProperties.class);
        // 一组连接共用的上下文对象
        serverTioConfig = new ServerTioConfig("MachineHandler Server", serverAioHandler, serverAioListener);
    }


    /**
     * @param applicationContext applicationContext
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}