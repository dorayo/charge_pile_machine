package com.huamar.charge.pile.server;

import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.pile.config.ServerApplicationProperties;
import com.huamar.charge.pile.enums.McTypeEnum;
import com.huamar.charge.pile.server.handle.netty.c.ServerNetHandlerForMC;
import com.huamar.charge.pile.server.handle.netty.c.SessionManagerForProtocolCNetHandler;
import com.huamar.charge.pile.server.service.factory.b.MachineBPacketFactory;
import com.huamar.charge.pile.server.session.context.SimpleSessionContext;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextClosedEvent;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * netty 服务端
 */
@Slf4j
@ConditionalOnProperty(name = "machine.server.enable-c-server", havingValue = "true")
@Configuration
@Import(SimpleSessionContext.class)
public class MachineCNetServer {

    /**
     * 设备端通信配置
     */
    private final ServerApplicationProperties properties;


    /**
     * 接收线程
     */
    private NioEventLoopGroup boosGroup;

    /**
     * 工作线程
     */
    private NioEventLoopGroup workerGroup;

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
     * @param properties properties
     */
    public MachineCNetServer(ServerApplicationProperties properties, MachineBPacketFactory machinePacketFactory) {
        this.properties = properties;
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> serverCStart() {
        return event -> {
//            Thread thread = new Thread() {
//                @Override
//                public void run() {
//                    try {
//                        sleep(25000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                    McChargeCommandDTO a = new McChargeCommandDTO();
//                    a.setGunSort((byte) 0x01);
//                    a.setBalance(0xfffffff);
//                    a.setOrderSerialNumber("1111111111111111111111111111111111111111111111".getBytes());
//                    event.getApplicationContext().getBean(PileStartChargeExecute.class).handleProtocolC("00220323501003", a);
//                    try {
//                        sleep(15000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                    event.getApplicationContext().getBean(PileStopChargeExecute.class).handleC("00220323501003", a);
//
//                }
//            };
//            thread.start();
            MachineCNetServer netServer = event.getApplicationContext().getBean(this.getClass());
            netServer.start(event.getApplicationContext());
            log.info("Server C Net start ...{}", netServer.getClass().getSimpleName());
        };
    }

    @Bean
    public ApplicationListener<ContextClosedEvent> stopCApplicationListener() {
        return event -> {
            event.getApplicationContext().getBean(this.getClass()).close();
            log.info("MachineNetServer C close ...");
        };
    }


    /**
     * 服务端启动
     */
    @SneakyThrows
    public void start(ApplicationContext applicationContext) {
        if (isRun.get()) {
            return;
        }
        this.init(applicationContext);
        isRun.getAndSet(Boolean.TRUE);
        channelFuture = serverBootstrap.bind(properties.getPortC()).sync();
        //serverBootstrap.bind(properties.getPortSalve()).sync();
        //channelFuture.channel().closeFuture().sync(); 阻塞等待服务器 socket 关闭
    }


    /**
     * 初始化
     */
    private void init(ApplicationContext applicationContext) {
        if (isRun.get()) {
            return;
        }
        boosGroup = new NioEventLoopGroup(properties.getBoss());
        workerGroup = new NioEventLoopGroup(properties.getWorker());
        serverBootstrap.group(boosGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) Duration.ofSeconds(60).toMillis())
                // 没有空闲链接将请求暂存在缓冲队列
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
//                        ServerNetHandler serverNetHandler = new ServerNetHandler(machinePacketFactory);
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast("splitD", new MessageSplitDecodeHandler());
                        pipeline.addLast("handleD", new MessageHandleDecodeHandler());
                        pipeline.addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));
                        pipeline.addLast("sessionManager", new SessionManagerForProtocolCNetHandler(McTypeEnum.C));
                        pipeline.addLast("serverNetHandler", applicationContext.getBean(ServerNetHandlerForMC.class));
                    }
                });
    }

    /**
     * 停止
     */
    public void close() {
        if (!isRun.get()) {
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
     * split
     */
    static class MessageSplitDecodeHandler extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
            byteBuf.markReaderIndex();
            int byteLen = byteBuf.readableBytes();
            if (byteLen < 2) return;
            if (byteBuf.readByte() != 0x68) {
                channelHandlerContext.close();
                return;
            }
            short resultLen = byteBuf.readUnsignedByte();
            int pageLen = 4 + resultLen;
            byteBuf.resetReaderIndex();
            if (4 + resultLen >= pageLen) {
                if (byteLen < pageLen) {
                    return;
                }
                log.info("receive success");
                list.add(byteBuf.readBytes(pageLen));
            }
        }
    }

    /**
     * handle
     */
    static class MessageHandleDecodeHandler extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
            log.info(BinaryViews.bfToHexStr(byteBuf));
            ProtocolCPacket p = ProtocolCPacket.createFromNettyBuf(byteBuf);
            if (p.getLocalRealCheckBit()[0] != p.getRemoteFrameCheckBit()[0] || p.getLocalRealCheckBit()[1] != p.getRemoteFrameCheckBit()[1]) {
                log.error("p.getLocalRealCheckBit() != p.getRemoteFrameCheckBit()");
                ctx.close();
                return;
            }
            list.add(p);
        }
    }
}
