package com.huamar.charge.pile.server;

import cn.hutool.core.util.IdUtil;
import com.huamar.charge.common.protocol.BasePacket;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.config.ServerApplicationProperties;
import com.huamar.charge.pile.server.handle.netty.ServerNetHandler;
import com.huamar.charge.pile.server.handle.netty.SessionManagerNetHandler;
import com.huamar.charge.pile.server.protocol.ProtocolCodecFactory;
import com.huamar.charge.pile.server.service.factory.MachinePacketFactory;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.context.SimpleSessionContext;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextClosedEvent;

import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SLX(硕立信协议)
 * netty 服务端
 */
@Slf4j
@ConditionalOnProperty(name = "machine.server.net-socket-model", havingValue = "netty")
@Configuration
@Import(SimpleSessionContext.class)
public class MachineNetServer implements NetServer {

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
     * @param properties           properties
     * @param machinePacketFactory machinePacketFactory
     */
    public MachineNetServer(ServerApplicationProperties properties, MachinePacketFactory machinePacketFactory) {
        this.properties = properties;
        this.machinePacketFactory = machinePacketFactory;
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> serverStart() {
        return event -> {
            MachineNetServer netServer = event.getApplicationContext().getBean(this.getClass());
            netServer.start();
            log.info("Server Net start ...{}", netServer.getClass().getSimpleName());
        };
    }

    @Bean
    public ApplicationListener<ContextClosedEvent> stopApplicationListener() {
        return event -> {
            event.getApplicationContext().getBean(this.getClass()).close();
            log.info("MachineNetServer close ...");
        };
    }


    /**
     * 服务端启动
     */
    @SneakyThrows
    @Override
    public void start() {
        if (isRun.get()) {
            return;
        }
        this.init();
        isRun.getAndSet(Boolean.TRUE);
        channelFuture = serverBootstrap.bind(properties.getPort()).sync();
    }


    /**
     * 初始化
     */
    private void init() {
        if (isRun.get()) {
            return;
        }

        boosGroup = new NioEventLoopGroup(properties.getBoss());
        workerGroup = new NioEventLoopGroup(properties.getWorker());
        serverBootstrap.group(boosGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                //用于客户端参数
                //.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) Duration.ofSeconds(60).toMillis())
                // 没有空闲链接将请求暂存在缓冲队列
                .option(ChannelOption.SO_BACKLOG, 4096)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        ServerNetHandler serverNetHandler = new ServerNetHandler(machinePacketFactory);
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast("InboundFirst", new ChannelInboundHandlerAdapter(){

                            @SuppressWarnings("DuplicatedCode")
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                                Thread.currentThread().setName(IdUtil.getSnowflakeNextIdStr());
                                try {
                                    SocketAddress remotedAddress = ctx.channel().remoteAddress();
                                    SessionChannel session = null;
                                    String idCode = SessionManager.setMDCParam(ctx);
                                    if (StringUtils.isNotBlank(idCode)) {
                                        session = SessionManager.get(idCode);
                                    }

                                    if (log.isDebugEnabled()) {
                                        log.info("SLX channelRead into >>>>>>>>>>>>>>>>>> idCode:{} session:{} address:{}", idCode, Optional.ofNullable(session).isPresent(), remotedAddress);
                                    }

                                    ctx.fireChannelRead(msg);

                                    if (log.isDebugEnabled()) {
                                        log.info("SLX channelRead end <<<<<<<<<<<<<<<<<<< idCode:{} session address:{} end", idCode, remotedAddress);
                                    }
                                }finally {
                                    MDC.clear();
                                }

                            }

                            @Override
                            public void channelReadComplete(ChannelHandlerContext ctx) {
                                try {
                                    SessionManager.setMDCParam(ctx);
                                    log.info("SLX channelReadComplete end <<<<<<<<<<<<<<<<<<");
                                    super.channelReadComplete(ctx);
                                }catch (Exception e){
                                    log.error("SLX channelReadComplete error");
                                }finally {
                                    MDC.clear();
                                }
                            }

                        });

                        pipeline.addLast("decoder", new MessageDecodeHandler());
                        pipeline.addLast("encoder", new MessageEncodeHandler());

                        // IdleStateHandler 下一个 handler 必须实现 userEventTriggered 方法处理对应事件
                        pipeline.addLast(new IdleStateHandler(properties.getTimeout().getSeconds(), 0, 0, TimeUnit.SECONDS));
                        pipeline.addLast("sessionManager", new SessionManagerNetHandler());
                        pipeline.addLast("serverNetHandler", serverNetHandler);
                    }
                });
    }


    /**
     * 停止
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public void close() {
        if (!isRun.get()) {
            return;
        }
        log.info("MachineNetServer start close...");
        if (channelFuture != null) {
            try {
                channelFuture.channel().close().sync();
            } catch (InterruptedException exception) {
                log.error("MachineNetServer channelFuture close error e:{}", exception.getMessage(), exception);
            }
        }
        boosGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    /**
     * 协议编码
     */
    static class MessageEncodeHandler extends MessageToByteEncoder<BasePacket> {

        @SuppressWarnings("unused")
        private final ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

        @SneakyThrows
        @Override
        protected void encode(ChannelHandlerContext ctx, BasePacket packet, ByteBuf byteBuf) {
            //byteBuf.order(byteOrder); 不推荐使用，已经废弃
            ProtocolCodecFactory.encode(packet, byteBuf);
            SessionManager.setMDCParam(ctx);

            if(packet instanceof DataPacket){
                DataPacket var = (DataPacket) packet;
                byte[] bytes = ByteBufUtil.getBytes(byteBuf);
                String hexStrFormat = HexExtUtil.encodeHexStrFormat(bytes, " ");
                log.info("SLX write byteBuf:{} packet msgId:{} msgNumber:{} hex:{}", byteBuf, HexExtUtil.encodeHexStr(var.getMsgId()), var.getMsgNumber(), hexStrFormat);
            }
        }
    }

    /**
     * 协议解码
     */
    static class MessageDecodeHandler extends ByteToMessageDecoder {

        @SuppressWarnings("unused")
        private final ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
            //byteBuf.order(byteOrder); 不推荐使用，已经废弃
            BasePacket basePacket = ProtocolCodecFactory.decode(byteBuf);
            if (Objects.isNull(basePacket)) {
                return;
            }
            list.add(basePacket);
        }
    }

}
