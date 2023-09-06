package com.huamar.charge.machine.client.starter;

import com.huamar.charge.machine.client.handle.MachineHandler;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.tio.client.ClientChannelContext;
import org.tio.client.ClientTioConfig;
import org.tio.client.ReconnConf;
import org.tio.client.TioClient;
import org.tio.client.intf.ClientAioHandler;
import org.tio.client.intf.ClientAioListener;
import org.tio.core.Node;

/**
 * 客户端启动类
 *
 * @author lizhou
 */
@Data
@Component
public class MachineClient implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * 构建服务器节点
     */
    public Node serverNode;

    /**
     * handler, 包括编码、解码、消息处理
     */
    public ClientAioHandler clientAioHandler;

    /**
     * 事件监听器，可以为null，但建议自己实现该接口，可以参考showcase了解些接口
     */
    public ClientAioListener clientAioListener;


    /**
     * 一组连接共用的上下文对象
     */
    public ClientTioConfig clientTioConfig;

    /**
     * 客户端入口
     */
    public TioClient tioClient = null;

    /**
     * 客户端TCP连接上下文
     */
    public ClientChannelContext clientChannelContext = null;

    /**
     * 启动程序入口
     */
    public void connect() throws Exception {
        tioClient = new TioClient(clientTioConfig);
        // 连接到服务器
        clientChannelContext = tioClient.connect(serverNode);
    }

    @Override
    public void afterPropertiesSet() {
        serverNode = new Node("127.0.0.1", 8886);

        // 事件监听器，可以为null，但建议自己实现该接口，可以参考showcase了解些接口
        clientAioHandler = applicationContext.getBean(MachineHandler.class);
        clientAioListener = applicationContext.getBean(ClientAioListener.class);

        // 一组连接共用的上下文对象
        clientTioConfig = new ClientTioConfig(clientAioHandler, clientAioListener, new ReconnConf(5 * 1000, 5));

        // 服务名称
        clientTioConfig.setName("T-io Client");

        // 心跳超时时间
        clientTioConfig.setHeartbeatTimeout(30 * 1000);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}