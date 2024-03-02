package com.huamar.charge.pile.server.handle.netty;

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

import java.util.Objects;

/**
 * 服务端监听器 ChannelInboundHandlerAdapter 区别，SimpleChannelInboundHandler.channelRead0 可是实现释放数据
 *
 * @author TiAmo
 */
@Slf4j
public class SessionManagerNetHandler extends SimpleChannelInboundHandler<BasePacket> {

    // 设备认证日志
    private final Logger authLog = LoggerFactory.getLogger(LoggerEnum.PILE_AUTH_LOGGER.getCode());

    private final McTypeEnum type = McTypeEnum.A;


    /**
     * 服务端上线的时候调用
     *
     * @param ctx ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        SessionManager.channelActive(ctx, "SLX");
        ctx.fireChannelActive();
    }


    /**
     * 服务端掉线的时候调用
     *
     * @param ctx ctx
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        SessionManager.channelInactive(ctx, "SLX");
        ctx.fireChannelInactive();
    }


    /**
     * 退出移除 session
     *
     * @param ctx ctx
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        SessionManager.handlerRemoved(ctx, "SLX");
    }

    /**
     * 服务端读取数据
     *
     * @param channelHandlerContext channelHandlerContext
     * @param packet                dataPacket
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BasePacket packet) {
        //noinspection DuplicatedCode
        if(log.isTraceEnabled()){
            log.trace("InboundHandler:{}", this.getClass().getSimpleName());
        }
        String idCode = "";
        if (packet instanceof DataPacket) {
            DataPacket dataPacket = (DataPacket) packet;
            String bsId = new String((dataPacket).getIdCode());
            idCode = bsId;
            MDC.put(ConstEnum.ID_CODE.getCode(), new String((dataPacket).getIdCode()));
            SessionChannel session = SessionManager.get(bsId);
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
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            String sessionCode = channelHandlerContext.channel().attr(machineId).get();
            MDC.put(ConstEnum.ID_CODE.getCode(), "fail_packet");
            FailMathPacket dataPacket = (FailMathPacket) packet;
            log.info("SLX FailMathPacket idCode:{} data:{}", sessionCode,HexExtUtil.encodeHexStrFormat(dataPacket.getBody(), StringPool.SPACE));
            log.info("SLX FailMathPacket close session...");
            SessionManager.closeCtx(channelHandlerContext, "SLX");
            SessionManager.remove(idCode);
            return;
        }

        channelHandlerContext.fireChannelRead(packet);
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

        authLog.error("SLX {} 连接出异常了,{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
        log.error("SLX {} 连接出异常了,{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
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
            SessionManager.closeCtx(ctx, "SLX");
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
            if (StringUtils.isNotBlank(bsId)) {
                MDC.put(ConstEnum.ID_CODE.getCode(), bsId);
            }
            switch (event.state()) {

                case READER_IDLE:
                    authLog.warn("SLX IdCode:{} READER_IDLE 读取数据空闲 心跳链接超时 关闭连接", bsId);
                    log.warn("SLX IdCode:{} READER_IDLE 读取数据空闲 心跳链接超时 关闭连接", bsId);
                    SessionManager.closeCtx(ctx, "SLX");
                    break;

                case WRITER_IDLE:
                    authLog.warn("SLX IdCode:{} WRITER_IDLE 读取数据空闲", bsId);
                    log.warn("SLX IdCode:{} WRITER_IDLE 读取数据空闲", bsId);
                    break;

                case ALL_IDLE:
                    authLog.warn("SLX IdCode:{} ALL_IDLE 时间超时", bsId);
                    log.warn("SLX IdCode:{} ALL_IDLE 时间超时", bsId);
                    break;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
