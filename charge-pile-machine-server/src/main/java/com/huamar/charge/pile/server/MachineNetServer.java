package com.huamar.charge.pile.server;

import com.huamar.charge.common.protocol.BasePacket;
import com.huamar.charge.pile.config.ServerApplicationProperties;
import com.huamar.charge.pile.server.handle.netty.ServerNetHandler;
import com.huamar.charge.pile.server.handle.netty.SessionManagerNetHandler;
import com.huamar.charge.pile.server.protocol.ProtocolCodecFactory;
import com.huamar.charge.pile.server.service.factory.MachinePacketFactory;
import com.huamar.charge.pile.server.session.context.SimpleSessionContext;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * netty 服务端
 */
@Slf4j
//@ConditionalOnProperty(name = "machine.server.net-socket-model", havingValue = "NETTY")
@Configuration
@Import(SimpleSessionContext.class)
public class MachineNetServer {

    /**
     * 设备端通信配置
     */
    private final ServerApplicationProperties properties;

    private final MachinePacketFactory machinePacketFactory;

    /**
     * 接收线程
     */
    private NioEventLoopGroup boosGroup;

    /**
     * 工作线程
     */
    private NioEventLoopGroup  workerGroup;

    /**
     * 服务端
     */
    private final ServerBootstrap serverBootstrap = new ServerBootstrap();


    private ChannelFuture channelFuture;


    /**
     * 是否启动
     */
    private final AtomicBoolean isRun = new AtomicBoolean(Boolean.FALSE);

    /**
     * @param properties           properties
     * @param machinePacketFactory machinePacketFactory
     */
    public MachineNetServer(ServerApplicationProperties properties, MachinePacketFactory machinePacketFactory) {
        this.properties = properties;
        this.machinePacketFactory = machinePacketFactory;
    }

    @Bean
    public ServerNetHandler serverNetHandler(MachinePacketFactory machinePacketFactory){
        return new ServerNetHandler(machinePacketFactory);
    }


    /**
     * 服务端启动
     */
    @SneakyThrows
    public void start(){
        if(isRun.get()){
            return;
        }
        this.init();
        isRun.getAndSet(Boolean.TRUE);
        channelFuture = serverBootstrap.bind(properties.getPort()).sync();
        //serverBootstrap.bind(properties.getPortSalve()).sync();
        //channelFuture.channel().closeFuture().sync(); 阻塞等待服务器 socket 关闭
    }


    /**
     * 初始化
     */
    private void init(){
        if(isRun.get()){
            return;
        }
        boosGroup = new NioEventLoopGroup(properties.getBoss());
        workerGroup = new NioEventLoopGroup(properties.getWorker());
        serverBootstrap.group(boosGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) properties.getTimeout().getSeconds())
                // 没有空闲链接将请求暂存在缓冲队列
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new MessageEncodeHandler());
                        pipeline.addLast(new MessageDecodeHandler());
                        pipeline.addLast(new SessionManagerNetHandler());
                        pipeline.addLast(serverNetHandler(machinePacketFactory));
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
        log.info("MachineNetServer start close...");
        if (channelFuture != null) {
            try {
                channelFuture.channel().close().sync();
            } catch (InterruptedException exception) {
                log.info("MachineNetServer channelFuture close error e:{}", exception.getMessage(), exception);
            }
        }
        boosGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    /**
     * 协议编码
     */
    static class MessageEncodeHandler extends MessageToByteEncoder<BasePacket> {

        @SneakyThrows
        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, BasePacket packet, ByteBuf byteBuf) {
            ProtocolCodecFactory.encode(packet, byteBuf);
        }
    }

    /**
     * 协议解码
     */
    static class MessageDecodeHandler extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
            BasePacket basePacket = ProtocolCodecFactory.decode(byteBuf);
            list.add(basePacket);
        }
    }

}
