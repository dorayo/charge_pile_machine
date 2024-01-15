package com.huamar.charge.pile.server.handle.netty.c;

import cn.hutool.core.util.IdUtil;
import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.LoggerEnum;
import com.huamar.charge.pile.enums.McTypeEnum;
import com.huamar.charge.pile.enums.NAttrKeys;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
@ChannelHandler.Sharable
@Slf4j
public class SessionManagerForYKCNetHandler extends SimpleChannelInboundHandler<ProtocolCPacket> {

    // 设备认证日志
    private final Logger authLog = LoggerFactory.getLogger(LoggerEnum.PILE_AUTH_LOGGER.getCode());

    private McTypeEnum type = McTypeEnum.C;

    public SessionManagerForYKCNetHandler(McTypeEnum type) {
        this.type = type;
    }

    /**
     * 服务端上线的时候调用
     *
     * @param ctx ctx
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //v240106 修改日志优化
        try {
            MDC.clear();
            Thread.currentThread().setName(IdUtil.getSnowflakeNextIdStr());
            AttributeKey<String> sessionKey = AttributeKey.valueOf(ConstEnum.X_SESSION_ID.getCode());
            String sessionId = ctx.channel().attr(sessionKey).get();
            if (Objects.isNull(sessionId)) {
                sessionId = IdUtil.getSnowflakeNextIdStr();
                ctx.channel().attr(sessionKey).set(sessionId);
            }
            MDC.put(ConstEnum.X_SESSION_ID.getCode(), sessionId);
            log.info("YKC {} 连上了服务器", ctx.channel().remoteAddress());
            authLog.info("YKC {} 连上了服务器", ctx.channel().remoteAddress());
        } catch (Exception e) {
            log.error("YKC channelActive error:{}", ExceptionUtils.getMessage(e), e);
        } finally {
            MDC.clear();
        }
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
            String idCode = ctx.channel().attr(machineId).get();
            MDC.put(ConstEnum.ID_CODE.getCode(), idCode);

            log.warn("YKC channelInactive  连接不活跃 idCode:{} remoteAddress:{}", idCode, ctx.channel().remoteAddress());
            authLog.warn("YKC channelInactive 连接不活跃 idCode:{} remoteAddress:{}", idCode, ctx.channel().remoteAddress());
        }catch (Exception e){
            log.error("YKC channelInactive error:{}", e.getMessage(), e);
            authLog.error("YKC channelInactive error:{}", e.getMessage(), e);
        }
        super.channelInactive(ctx);
    }

    /**
     * 服务端读取数据
     *
     * @param channelHandlerContext channelHandlerContext
     * @param packet                dataPacket
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ProtocolCPacket packet) {
        if(log.isDebugEnabled()){
            log.debug("InboundHandler:{}", this.getClass().getSimpleName());
        }

        String idCode;
        SessionChannel session = null;
        SocketAddress remotedAddress = channelHandlerContext.channel().remoteAddress();
        AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
        idCode = channelHandlerContext.channel().attr(machineId).get();
        if(Objects.nonNull(idCode)){
            session = SessionManager.get(idCode);
        }

        log.info("YKC channelRead0 >>>>>>>>>>>>>>>>>> start idCode:{} session:{} address:{} ", idCode,Optional.ofNullable(session).isPresent(), remotedAddress);
        // 设备认证
        if (packet.getBodyType() == 0x01) {
            channelHandlerContext.channel().attr(NAttrKeys.ID_BODY).set(packet.getIdBody());
            ByteBuf body = ByteBufAllocator.DEFAULT.heapBuffer();
            body.writeBytes(packet.getBody());
            byte[] idBytes = new byte[7];
            body.readBytes(idBytes);
            // 旧方式解析
            String id = BinaryViews.bcdViewsLe(idBytes);
            //新方式解析
            String idCodeBcdString = "4710"  + BCDUtils.bcdToStr(idBytes);
            String code = "4710" + id;
            channelHandlerContext.channel().attr(machineId).set(code);
            if(StringUtils.isNotBlank(code)){
                MDC.put(ConstEnum.ID_CODE.getCode(), code);
            }
            SimpleSessionChannel sessionChannelNew = new SimpleSessionChannel(channelHandlerContext);
            sessionChannelNew.setId(code);
            sessionChannelNew.setType(this.type);
            SessionManager.put(code, sessionChannelNew);

            if (log.isDebugEnabled()) {
                log.info("YKC 设备认证 存放会话 idCode:{} pileCode:{}", code, idCodeBcdString);
            }

        }

        channelHandlerContext.fireChannelRead(packet);
        log.info("YKC channelRead0 <<<<<<<<<<<<<<<<<<< end session idCode:{} address:{} end ", idCode, remotedAddress);
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
        log.error("{} 连接出异常了,{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
        try {
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            String bsId = ctx.channel().attr(machineId).get();
            if (StringUtils.isNotBlank(bsId)) {
                MDC.put(ConstEnum.ID_CODE.getCode(), bsId);
                SessionManager.remove(bsId);
            }
        } catch (Exception ignored) {
            log.error("{} exceptionCaught error,{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
        } finally {
            ctx.close();
        }
    }


    /**
     * 心跳检测事件处理
     *  V230107 TiAmo 修复事件判断
     *
     * @param ctx ctx
     * @param evt evt
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        authLog.info("YKC userEventTriggered evt class:{}", evt.getClass().getSimpleName());
        log.info("YKC userEventTriggered evt class:{}", evt.getClass().getSimpleName());
        // 入站的消息就是 IdleStateEvent 具体的事件
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
            String idCode = ctx.channel().attr(machineId).get();
            if (StringUtils.isNotBlank(idCode)) {
                MDC.put(ConstEnum.ID_CODE.getCode(), idCode);
            }
            switch (event.state()) {

                case READER_IDLE:
                    authLog.warn("YKC IdCode:{} READER_IDLE 读取数据空闲 心跳链接超时 关闭连接", idCode);
                    log.warn("YKC IdCode:{} READER_IDLE 读取数据空闲 心跳链接超时 关闭连接", idCode);
                    this.close(ctx);
                    break;

                case WRITER_IDLE:
                    authLog.warn("YKC IdCode:{} WRITER_IDLE 读取数据空闲", idCode);
                    log.warn("YKC IdCode:{} WRITER_IDLE 读取数据空闲", idCode);
                    break;

                case ALL_IDLE:
                    authLog.warn("YKC IdCode:{} ALL_IDLE 时间超时", idCode);
                    log.warn("YKC IdCode:{} ALL_IDLE 时间超时", idCode);
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
    @SuppressWarnings("DuplicatedCode")
    private void close(ChannelHandlerContext ctx) {
        ctx.channel().close().addListener(future -> {
            authLog.error("YKC SessionManager ctx channel close:{} ", future.isSuccess(), future.cause());
            log.error("YKC SessionManager ctx channel close:{} ", future.isSuccess(), future.cause());
        });

        ctx.close().addListener(future -> {
            authLog.error("YKC SessionManager ctx close:{} ", future.isSuccess(), future.cause());
            log.error("YKC SessionManager ctx close:{} ", future.isSuccess(), future.cause());
        });
    }
}
