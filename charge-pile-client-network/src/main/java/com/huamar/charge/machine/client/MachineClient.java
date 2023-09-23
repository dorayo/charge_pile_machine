package com.huamar.charge.machine.client;

import cn.hutool.core.convert.Convert;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.tio.client.ClientChannelContext;
import org.tio.client.ClientTioConfig;
import org.tio.client.ReconnConf;
import org.tio.client.TioClient;
import org.tio.client.intf.ClientAioHandler;
import org.tio.client.intf.ClientAioListener;
import org.tio.core.ChannelContext;
import org.tio.core.Node;
import org.tio.core.intf.Packet;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 客户端启动类
 *
 * @author lizhou
 */
@Data
@Configuration
public class MachineClient implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final AtomicInteger messageNumber = new AtomicInteger(0);

    private Integer clientId;

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
        clientAioListener = new ClientListener();

        // 一组连接共用的上下文对象
        clientTioConfig = new ClientTioConfig(clientAioHandler, clientAioListener, new ReconnConf(5 * 1000, 5));

        // 服务名称
        clientTioConfig.setName("T-io Client");

        // 心跳超时时间
        clientTioConfig.setHeartbeatTimeout(TimeUnit.MINUTES.toMillis(1));
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    /**
     * 获取消息流水号
     *
     * @return Short
     */
    public Short getMessageNumber() {
        int andIncrement = messageNumber.incrementAndGet();
        if (Objects.equals(andIncrement, 65535)) {
            messageNumber.set(0);
            return Convert.toShort(messageNumber.getAndIncrement());
        }
        return Convert.toShort(andIncrement);
    }


    @Slf4j
    static class ClientListener implements org.tio.client.intf.ClientAioListener{
        /**
         * 建链后触发本方法，注：建链不一定成功，需要关注参数isConnected
         *
         * @param channelContext channelContext
         * @param isConnected    是否连接成功,true:表示连接成功，false:表示连接失败
         * @param isReconnect    是否是重连, true: 表示这是重新连接，false: 表示这是第一次连接
         * @throws Exception Exception
         * author: TiAmo
         */
        @Override
        public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect) throws Exception {
            log.info("连接到服务器：" + (isConnected ? "成功" : "失败") + "，是否重连：" + (isReconnect ? "是" : "否"));
        }

        /**
         * 原方法名：onAfterDecoded
         * 解码成功后触发本方法
         *
         * @param channelContext channelContext
         * @param packet packet
         * @param packetSize packetSize
         * @throws Exception Exception
         * author: TiAmo
         */
        @Override
        public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize) throws Exception {

        }

        /**
         * 接收到TCP层传过来的数据后
         *
         * @param channelContext channelContext
         * @param receivedBytes  本次接收了多少字节
         * @throws Exception Exception
         */
        @Override
        public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes) throws Exception {
        }

        /**
         * 消息包发送之后触发本方法
         *
         * @param channelContext channelContext
         * @param packet  packet
         * @param isSentSuccess  true:发送成功，false:发送失败
         * @throws Exception Exception
         * @author tanyaowu
         */
        @Override
        public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess) throws Exception {

        }

        /**
         * 处理一个消息包后
         *
         * @param channelContext channelContext
         * @param packet packet
         * @param cost           本次处理消息耗时，单位：毫秒
         * @throws Exception Exception
         */
        @Override
        public void onAfterHandled(ChannelContext channelContext, Packet packet, long cost) throws Exception {

        }

        /**
         * 连接关闭前触发本方法
         *
         * @param channelContext the channelContext
         * @param throwable      the throwable 有可能为空
         * @param remark         the remark 有可能为空
         * @param isRemove isRemove
         * @throws Exception Exception
         * @author tanyaowu
         */
        @Override
        public void onBeforeClose(ChannelContext channelContext, Throwable throwable, String remark, boolean isRemove) throws Exception {

        }
    }
}