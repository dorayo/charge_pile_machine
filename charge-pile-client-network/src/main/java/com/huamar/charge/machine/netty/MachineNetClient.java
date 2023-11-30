package com.huamar.charge.machine.netty;

import cn.hutool.core.convert.Convert;
import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.protocol.BasePacket;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.machine.client.protocol.ProtocolCodecFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
public class MachineNetClient implements InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * 接收线程
     */
    private final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(1);

    /**
     * 客户端
     */
    private final Bootstrap bootstrap = new Bootstrap();


    /**
     * 是否启动
     */
    private final AtomicBoolean isRun = new AtomicBoolean(Boolean.FALSE);


    private String host = "127.0.0.1";

    private int port = 8886;

    private String clientName = "";

    private String idCode;

    @Getter
    private Channel channel;

    /**
     * 设备流水号
     */
    private final AtomicInteger messageNumber = new AtomicInteger(0);

    public MachineNetClient(String host, int port, String clientName, String idCode) {
        this.host = host;
        this.port = port;
        this.clientName = clientName;
        this.idCode = idCode;
    }

    public MachineNetClient() {
        super();
    }

    /**
     * 服务端启动
     */
    @SneakyThrows
    public void start() {
        if (isRun.get()) {
            return;
        }
        this.init();
        ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
        channel = channelFuture.channel();
        // 阻塞关闭
        //channelFuture.channel().closeFuture().sync();
    }


    /**
     * 初始化
     */
    private void init() {
        if (isRun.get()) {
            return;
        }
        isRun.getAndSet(Boolean.TRUE);

        HeartbeatHandler heartbeatHandler = new HeartbeatHandler(this);

        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        ChannelPipeline pipeline = socketChannel.pipeline();

                        pipeline.addLast("decoder", new MessageDecodeHandler());

                        pipeline.addLast("encoder", new MessageEncodeHandler());

                        // 添加IdleStateHandler，当一段时间内没有发生读或写事件时触发
                        pipeline.addLast(new IdleStateHandler(0, 60 * 60, 0, TimeUnit.SECONDS));

                        pipeline.addLast("in-heartbeat", heartbeatHandler);

                        pipeline.addLast("in-handler", new SimpleChannelInboundHandler<DataPacket>() {

                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, DataPacket dataPacket) {
                                String bsId = new String((dataPacket).getIdCode());
                                String code = HexExtUtil.encodeHexStr(dataPacket.getMsgId());
                                log.info("idCode:{} msg id:{} dataPacket:{}", bsId, code, JSONParser.jsonString(dataPacket));
                            }

                            @Override
                            public void channelActive(ChannelHandlerContext ctx) {
                                log.info("Client:{} connected to server:/{}:{}", clientName, host, port);
                            }

                        });
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

        //log.debug("Client:{} close... server:/{}:{}", clientName, host, port);
        eventLoopGroup.shutdownGracefully();
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

    @Override
    public void afterPropertiesSet() {
        log.info("applicationContext:{}", applicationContext);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 创建心跳包
     *
     * @return BasePacket
     * @author tanyaowu
     */
    public BasePacket heartbeatPacket() {

        ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(BCDUtils.bcdTime().getData());
        byteBuffer.put((byte) 1);
        byteBuffer.put((byte) 1);
        byteBuffer.put((byte) 2);
        byteBuffer.put((byte) 3);
        byteBuffer.flip();

        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);

        DataPacket dataPacket = new DataPacket();
        dataPacket.setTag((byte) Integer.parseInt("23", 16));
        dataPacket.setMsgId((byte) Integer.parseInt("31", 16));
        dataPacket.setMsgBodyAttr((byte) Integer.parseInt("0", 16));
        dataPacket.setMsgBodyLen((short) byteBuffer.limit());
        dataPacket.setMsgNumber(getMessageNumber());
        dataPacket.setIdCode(idCode.getBytes());
        dataPacket.setMsgBody(bytes);
        dataPacket.setTagEnd((byte) Integer.parseInt("23", 16));
        return dataPacket;
    }



    /**
     * 心跳发送
     */
    private static class HeartbeatHandler extends ChannelInboundHandlerAdapter {

        private final MachineNetClient machineNetClient;

        public HeartbeatHandler(MachineNetClient machineNetClient) {
            this.machineNetClient = machineNetClient;
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state() == IdleState.WRITER_IDLE) {
                    BasePacket basePacket = machineNetClient.heartbeatPacket();
                    ctx.writeAndFlush(basePacket);
                }
            }
        }

    }

    /**
     * 协议编码
     */
    static class MessageEncodeHandler extends MessageToByteEncoder<BasePacket> {

        @SuppressWarnings("unused")
        private final ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

        @SneakyThrows
        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, BasePacket packet, ByteBuf byteBuf) {
            //byteBuf.order(byteOrder); 不推荐使用，已经废弃
            ProtocolCodecFactory.encode(packet, byteBuf);
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