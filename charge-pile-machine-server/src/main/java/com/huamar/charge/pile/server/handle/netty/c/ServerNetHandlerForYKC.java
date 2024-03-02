package com.huamar.charge.pile.server.handle.netty.c;

import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.LoggerEnum;
import com.huamar.charge.pile.enums.NAttrKeys;
import com.huamar.charge.pile.server.service.handler.c.MachineCAuthenticationHandler;
import com.huamar.charge.pile.server.service.handler.c.MachineCHandlers;
import com.huamar.charge.pile.server.service.handler.c.MachineCHeartbeatHandler;
import com.huamar.charge.pile.server.session.SessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * The type Server net handler for mc.
 */
@Slf4j
@ChannelHandler.Sharable
@Component
@RequiredArgsConstructor
public class ServerNetHandlerForYKC extends SimpleChannelInboundHandler<ProtocolCPacket> {

    private final MachineCAuthenticationHandler machineCAuthenticationHandler;

    private final MachineCHeartbeatHandler machineCHeartbeatHandler;

    private final MachineCHandlers machineCHandlers;

    private final Logger authLog = LoggerFactory.getLogger(LoggerEnum.PILE_AUTH_LOGGER.getCode());


    /**
     * 消息生产者
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolCPacket cPacket) {
        if(log.isTraceEnabled()){
            log.trace("InboundHandler:{}", this.getClass().getSimpleName());
        }

        AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
        SessionChannel session = SessionManager.get(ctx.channel().attr(machineId).get());
        ctx.channel().attr(NAttrKeys.PROTOCOL_C_LATEST_PACKET).set(cPacket);

        Integer latestOrderV = ctx.channel().attr(NAttrKeys.PROTOCOL_C_LATEST_ORDER_V).get();
        if (latestOrderV == null) {
            latestOrderV = 0;
        }
        int currentPacketOrderV = cPacket.getOrderV();
        if (latestOrderV <= currentPacketOrderV) {
            ctx.channel().attr(NAttrKeys.PROTOCOL_C_LATEST_ORDER_V).set(currentPacketOrderV);
        }
        switch (cPacket.getBodyType()) {

            //login
            case 0x01:
                machineCAuthenticationHandler.handler(cPacket, session, ctx);
                break;

            //heartbeat
            case 0x03:
                machineCHeartbeatHandler.handler(cPacket, session, ctx);
                break;

            //verify price model
            case 0x05:
                machineCHandlers.handler0x05(cPacket, ctx);
                break;

            // request price model
            case 0x09:
                machineCHandlers.handler0x09(cPacket, ctx);
                break;

            //上送充电枪实时数据，周期上送时，待机 5 分钟、充电 15 秒
            case 0x13:
                machineCHandlers.handler0x13(cPacket, ctx);
                break;

            // start charge response
            case 0x33:
                machineCHandlers.handler0x33(cPacket, ctx);
                break;

            // stop charge response
            case 0x35:
                machineCHandlers.handler0x35(cPacket, ctx);
                break;

            //  qrcode set response
            case 0x55:
                machineCHandlers.handler0x55(cPacket, ctx);
                break;

            case 0x3b:
                machineCHandlers.handler0x3b(cPacket, ctx);
                break;

            case (byte) 0x9B:
                machineCHandlers.handler0x9B(cPacket, ctx);
                break;

            case (byte) 0x59:
                machineCHandlers.handler0x59(cPacket, ctx);
                break;

            case (byte) 0xF1:
                machineCHandlers.handler0xF1(cPacket, ctx);
                break;

            case (byte) 0x63:
            case (byte) 0x91:
            case (byte) 0x92:
            case (byte) 0x93:
            case (byte) 0x94:
                log.error("YCK 不明协议类型，请关注 type={}", HexExtUtil.encodeHexStr(cPacket.getBodyType()));
                break;
            default:
                log.error("YCK 不明协议，请关注 type={}", HexExtUtil.encodeHexStr(cPacket.getBodyType()));
                break;
        }
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
        try {
            authLog.error("YKC BsExceptionCaught,{}", "连接出现异常，请关注");
            authLog.error("YKC BsExceptionCaught {} error:{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
            log.error("YKC BsExceptionCaught {} error:{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
            String idCode = SessionManager.setMDCParam(ctx);
            if (StringUtils.isNotBlank(idCode)) {
                SessionManager.remove(idCode);
            }
        } catch (Exception ignored) {
            authLog.error("YKC BsExceptionCaught,{}", "连接出现异常，请关注");
            authLog.error("YKC BsExceptionCaught {} error:{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
            log.error("YKC BsExceptionCaught {} error:{}", ctx.channel().remoteAddress(), cause.getMessage(), cause);
        } finally {
            SessionManager.closeCtx(ctx, "YKC");
        }
    }

}
