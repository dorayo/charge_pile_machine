package com.huamar.charge.machine.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Configuration
public class MachineNetClient implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * 接收线程
     */
    private EventLoopGroup eventLoopGroup;

    /**
     * 客户端
     */
    private final Bootstrap bootstrap = new Bootstrap();


    /**
     * 是否启动
     */
    private final AtomicBoolean isRun = new AtomicBoolean(Boolean.FALSE);


    /**
     * 服务端启动
     */
    @SneakyThrows
    public void start(){
        if(isRun.get()){
            return;
        }

        isRun.getAndSet(Boolean.FALSE);
        ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8886);
        channelFuture.sync();

        // 阻塞关闭
        channelFuture.channel().closeFuture().sync();
    }


    /**
     * 初始化
     */
    private void init(){
        if(isRun.get()){
            return;
        }
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {

                    }
                });
    }

    /**
     * 停止
     */
    public void close() {
        if(!isRun.get()){
            return;
        }

        log.info("client start close...");
        eventLoopGroup.shutdownGracefully();
    }

    @Override
    public void afterPropertiesSet() {
        log.info("applicationContext:{}", applicationContext);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}