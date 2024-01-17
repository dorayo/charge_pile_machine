package com.huamar.charge.pile.server;

import cn.hutool.core.util.IdUtil;
import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.ByteExtUtil;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.config.ServerApplicationProperties;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.McTypeEnum;
import com.huamar.charge.pile.server.handle.netty.c.ServerNetHandlerForYKC;
import com.huamar.charge.pile.server.handle.netty.c.SessionManagerForYKCNetHandler;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.context.SimpleSessionContext;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextClosedEvent;

import java.net.SocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
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

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private ServerNetHandlerForYKC serverNetHandlerForYKC;

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
    public MachineCNetServer(ServerApplicationProperties properties) {
        this.properties = properties;
    }


    @Bean
    public ApplicationListener<ApplicationReadyEvent> serverCStart() {
        return event -> {
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
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        log.info("activeProfiles:{}", StringUtils.join(activeProfiles));

        if (isRun.get()) {
            return;
        }
        this.init();
        isRun.getAndSet(Boolean.TRUE);
        channelFuture = serverBootstrap.bind(properties.getPortC()).sync();
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
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) Duration.ofSeconds(60).toMillis())
                // 没有空闲链接将请求暂存在缓冲队列
                .option(ChannelOption.SO_BACKLOG, 4096)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast("Inbound", new ChannelInboundHandlerAdapter(){


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
                                        log.info("────────────────────────────────────────────────────────");
                                        log.info("YKC channelRead into >>>>>>>>>>>>>>>>>> idCode:{} session:{} address:{}", idCode, Optional.ofNullable(session).isPresent(), remotedAddress);
                                    }

                                    ctx.fireChannelRead(msg);

                                    if (log.isDebugEnabled()) {
                                        log.info("YKC channelRead end <<<<<<<<<<<<<<<<<<< session idCode:{} address:{} end", idCode, remotedAddress);
                                    }
                                }finally {
                                    MDC.clear();
                                }
                            }

                            @Override
                            public void channelReadComplete(ChannelHandlerContext ctx) {
                                try {
                                    SessionManager.setMDCParam(ctx);
                                    log.info("YKC channelReadComplete end <<<<<<<<<<<<<<<<<<");
                                    super.channelReadComplete(ctx);
                                }catch (Exception e){
                                    log.error("YKC channelReadComplete error");
                                }finally {
                                    MDC.clear();
                                }
                            }

                        });
                        pipeline.addLast("splitD", new MessageSplitDecodeHandler());
                        pipeline.addLast("handleD", new MessageHandleDecodeHandler());
                        pipeline.addLast(new IdleStateHandler(properties.getTimeout().getSeconds(), 0, 0, TimeUnit.SECONDS));
                        pipeline.addLast("sessionManager", new SessionManagerForYKCNetHandler(McTypeEnum.C));
                        pipeline.addLast("serverNetHandler", serverNetHandlerForYKC);
                    }
                });
    }

    /**
     * 停止
     */
    @SuppressWarnings("DuplicatedCode")
    public void close() {
        if (!isRun.get()) {
            return;
        }
        log.info("MachineNetServer start close...");
        if (channelFuture != null) {
            try {
                channelFuture.channel().close().sync();
            } catch (InterruptedException exception) {
                log.warn("MachineNetServer channelFuture close error e:{}", exception.getMessage(), exception);
            }
        }
        boosGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }


    /**
     * 分包
     */
    static class MessageSplitDecodeHandler extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {

            byteBuf.markReaderIndex();
            int readableBytes = byteBuf.readableBytes();

            if (readableBytes < 2) {
                return;
            }

            if (byteBuf.readByte() != 0x68) {
                ctx.close();
                return;
            }

            short resultLen = byteBuf.readUnsignedByte();

            // 协议包分割大小
            int packetLength = 4 + resultLen;

            // 不足继续读取
            if (readableBytes < packetLength) {
                if(log.isDebugEnabled()){
                    log.info("YKC 获取半包 return:{}", byteBuf);
                }

                //重置读取
                byteBuf.resetReaderIndex();
                return;
            }

            //重置读取，上次标记从头读取
            byteBuf.resetReaderIndex();
            ByteBuf nextBuf = byteBuf.readBytes(packetLength);
            list.add(nextBuf);

            // 分割数据包
            if(log.isDebugEnabled()){
                log.debug("YKC Decode >>>>>>>>>> SplitDecode byteBuf:{}", byteBuf);
            }
        }
    }


    /**
     * handle
     */
    static class MessageHandleDecodeHandler extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {

            // 可读字节，数据包
            byteBuf.markReaderIndex();
            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(bytes);
            byteBuf.resetReaderIndex();

            // 设备idCode 线程变量
            String idCode = SessionManager.getIdCode(ctx);

            if(log.isDebugEnabled()){
                log.debug("YKC Decode hex packet:{}", HexExtUtil.encodeHexStrFormat(bytes, StringPool.SPACE));
            }

            // 协议包解析
            ProtocolCPacket p = ProtocolCPacket.createFromNettyBuf(byteBuf);

            if(log.isDebugEnabled()){
                String messageId = HexExtUtil.encodeHexStr(p.getBodyType());
                log.debug("YKC 终端号:{}, msgId:{}, sign:{}, bodyLen:{}, orderVBf:{}, encryptState:{}, bodyType:{}, ",
                        idCode, messageId, p.getSign(), p.getBodyLen(), HexExtUtil.encodeHexStr(p.getOrderVBf()), p.isEncryptState(), HexExtUtil.encodeHexStr(p.getBodyType()));
            }

            if(log.isDebugEnabled()){
                log.debug("YKC Decode CRC16 check ByteOrder:{}, source:{} check:{}", p.getByteOrder(), p.getSourceCrC(), p.getCheckCrC());
                log.debug("YKC Decode <<<<<<<<<< end hex");
            }

            boolean checkCrc = p.getSourceCrC() == p.getCheckCrC();
            if(!checkCrc){
                log.error("YKC CRC16 check error source:{} check:{}", p.getSourceCrC(), p.getCheckCrC());
                ctx.close();
                return;
            }

            list.add(p);
        }
    }
}
