package com.huamar.charge.pile.server.handle.netty;

import cn.hutool.core.util.IdUtil;
import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.common.protocol.BasePacket;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.FailMathPacket;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.LoggerEnum;
import com.huamar.charge.pile.enums.McTypeEnum;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.Optional;

/**
 * 服务端监听器 ChannelInboundHandlerAdapter 区别，SimpleChannelInboundHandler.channelRead0 可是实现释放数据
 *
 * @author TiAmo
 */
@Slf4j
public class SessionManagerNetHandler extends SimpleChannelInboundHandler<BasePacket> {

    // 设备认证日志
    private final Logger authLog = LoggerFactory.getLogger(LoggerEnum.PILE_AUTH_LOGGER.getCode());

    McTypeEnum type = McTypeEnum.A;

    public SessionManagerNetHandler(McTypeEnum type) {
        this.type = type;
    }

    public SessionManagerNetHandler() {

    }

    /**
     * 服务端上线的时候调用
     *
     * @param ctx ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        MDC.clear();
        Thread.currentThread().setName(IdUtil.getSnowflakeNextIdStr());
        AttributeKey<String> sessionKey = AttributeKey.valueOf(ConstEnum.X_SESSION_ID.getCode());
        String sessionId = ctx.channel().attr(sessionKey).get();
        if(Objects.isNull(sessionId)){
            sessionId = IdUtil.simpleUUID();
            ctx.channel().attr(sessionKey).set(sessionId);
        }
        MDC.put(ConstEnum.X_SESSION_ID.getCode(), sessionId);
        log.info("{} 连上了服务器", ctx.channel().remoteAddress());
        authLog.info("{} 连上了服务器", ctx.channel().remoteAddress());
        MDC.clear();
    }


    /**
     * 服务端掉线的时候调用
     *
     * @param ctx ctx
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        MDC.clear();
        try {
            Thread.currentThread().setName(IdUtil.getSnowflakeNextIdStr());
            AttributeKey<String> sessionKey = AttributeKey.valueOf(ConstEnum.X_SESSION_ID.getCode());
            String sessionId = ctx.channel().attr(sessionKey).get();
            MDC.put(ConstEnum.X_SESSION_ID.getCode(), sessionId);

            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            String bsId = ctx.channel().attr(machineId).get();
            MDC.put(ConstEnum.ID_CODE.getCode(), bsId);

            log.info("channelInactive  连接不活跃 idCode:{} remoteAddress:{}", bsId, ctx.channel().remoteAddress());
            authLog.info("channelInactive 连接不活跃 idCode:{} remoteAddress:{}", bsId, ctx.channel().remoteAddress());
        }catch (Exception e){
            log.error("channelInactive error:{}", e.getMessage(), e);
            authLog.error("channelInactive error:{}", e.getMessage(), e);
        }
        super.channelInactive(ctx);
    }

    /**
     * 退出移除 session
     *
     * @param ctx ctx
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        try {
            AttributeKey<String> sessionKey = AttributeKey.valueOf(ConstEnum.X_SESSION_ID.getCode());
            String sessionId = ctx.channel().attr(sessionKey).get();
            MDC.put(ConstEnum.X_SESSION_ID.getCode(), sessionId);

            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            String idCode = ctx.channel().attr(machineId).get();
            log.warn("handlerRemoved, idCode:{}, remoteAddress:{}", idCode, ctx.channel().remoteAddress());
            authLog.warn("handlerRemoved, idCode:{}, remoteAddress:{}", idCode, ctx.channel().remoteAddress());

            if (StringUtils.isNotBlank(idCode)) {
                MDC.put(ConstEnum.ID_CODE.getCode(), idCode);
                SessionManager.remove(idCode);
            }

        } catch (Exception cause) {
            log.error("handlerRemoved error, idCode:{}, remoteAddress:{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
            authLog.error("handlerRemoved error, idCode:{}, remoteAddress:{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
        } finally {
            // 防止session 关闭不执行，始终执行一次
            this.close(ctx);
        }
        super.handlerRemoved(ctx);
    }

    /**
     * 服务端读取数据
     *
     * @param channelHandlerContext channelHandlerContext
     * @param packet                dataPacket
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BasePacket packet) {
        Thread.currentThread().setName(IdUtil.getSnowflakeNextIdStr());
        String idCode = "";
        SocketAddress remotedAddress = channelHandlerContext.channel().remoteAddress();

        AttributeKey<String> sessionKey = AttributeKey.valueOf(ConstEnum.X_SESSION_ID.getCode());
        String sessionId = channelHandlerContext.channel().attr(sessionKey).get();
        MDC.put(ConstEnum.X_SESSION_ID.getCode(), sessionId);

        if (packet instanceof DataPacket) {
            DataPacket dataPacket = (DataPacket) packet;
            String bsId = new String((dataPacket).getIdCode());
            idCode = bsId;
            MDC.put(ConstEnum.ID_CODE.getCode(), new String((dataPacket).getIdCode()));
            SessionChannel session = SessionManager.get(bsId);
            log.info("channelRead0 start session:{} address:{} idCode:{} >>>>>>>>>>>>>>>>>>", Optional.ofNullable(session).isPresent(), remotedAddress, idCode);
            if (Objects.isNull(session)) {
                SimpleSessionChannel sessionChannelNew = new SimpleSessionChannel(channelHandlerContext);
                sessionChannelNew.setId(bsId);
                sessionChannelNew.setType(type);
                SessionManager.put(bsId, sessionChannelNew);
            }

            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            channelHandlerContext.channel().attr(machineId).set(bsId);
        }

        if (packet instanceof FailMathPacket) {
            MDC.put(ConstEnum.ID_CODE.getCode(), "fail_packet");
            FailMathPacket dataPacket = (FailMathPacket) packet;
            log.info("FailMathPacket data:{}", HexExtUtil.encodeHexStrFormat(dataPacket.getBody(), StringPool.SPACE));
            log.info("FailMathPacket close session...");
            this.close(channelHandlerContext);
            MDC.clear();
            return;
        }

        channelHandlerContext.fireChannelRead(packet);
        log.info("channelRead0 end session address:{} end idCode:{} <<<<<<<<<<<<<<<<<<<", remotedAddress, idCode);
        MDC.clear();
    }

    /**
     * 异常发生时候调用
     *
     * @param ctx   ctx
     * @param cause cause
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        MDC.clear();
        AttributeKey<String> sessionKey = AttributeKey.valueOf(ConstEnum.X_SESSION_ID.getCode());
        String sessionId = ctx.channel().attr(sessionKey).get();
        MDC.put(ConstEnum.X_SESSION_ID.getCode(), sessionId);

        authLog.error("{} 连接出异常了,{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
        log.error("{} 连接出异常了,{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
        try {
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            String bsId = ctx.channel().attr(machineId).get();
            if (StringUtils.isNotBlank(bsId)) {
                MDC.put(ConstEnum.ID_CODE.getCode(), bsId);
                SessionManager.remove(bsId);
            }
        } catch (Exception ignored) {
            authLog.error("{} exceptionCaught error,{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
            log.error("{} exceptionCaught error,{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
        } finally {
            this.close(ctx);
        }
    }

    /**
     * 心跳检测事件处理
     *
     * @param ctx ctx
     * @param evt evt
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        authLog.info("userEventTriggered evt class:{}", evt.getClass().getSimpleName());
        log.info("userEventTriggered evt class:{}", evt.getClass().getSimpleName());
        // 入站的消息就是 IdleStateEvent 具体的事件
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            String bsId = ctx.channel().attr(machineId).get();
            switch (event.state()) {

                case READER_IDLE:
                    authLog.warn("IdCode:{} READER_IDLE 读取数据空闲 心跳链接超时 关闭连接", bsId);
                    log.warn("IdCode:{} READER_IDLE 读取数据空闲 心跳链接超时 关闭连接", bsId);
                    if (StringUtils.isNotBlank(bsId)) {
                        MDC.put(ConstEnum.ID_CODE.getCode(), bsId);
                    }
                    this.close(ctx);
                    break;

                case WRITER_IDLE:
                    authLog.warn("IdCode:{} WRITER_IDLE 读取数据空闲", bsId);
                    log.warn("IdCode:{} WRITER_IDLE 读取数据空闲", bsId);
                    break;

                case ALL_IDLE:
                    authLog.warn("IdCode:{} ALL_IDLE 时间超时", bsId);
                    log.warn("IdCode:{} ALL_IDLE 时间超时", bsId);
                    break;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 关闭连接
     *
     * @param ctx ctx
     */
    private void close(ChannelHandlerContext ctx) {
        ctx.channel().close().addListener(future -> {
            authLog.error("SessionManager ctx channel close:{} ", future.isSuccess(), future.cause());
            log.error("SessionManager ctx channel close:{} ", future.isSuccess(), future.cause());
        });

        ctx.close().addListener(future -> {
            authLog.error("SessionManager ctx close:{} ", future.isSuccess(), future.cause());
            log.error("SessionManager ctx close:{} ", future.isSuccess(), future.cause());
        });
    }
}
