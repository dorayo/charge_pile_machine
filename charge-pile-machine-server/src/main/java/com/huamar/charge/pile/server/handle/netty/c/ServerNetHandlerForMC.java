package com.huamar.charge.pile.server.handle.netty.c;

import cn.hutool.core.lang.Assert;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.NAttrKeys;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.server.service.factory.MachinePacketFactory;
import com.huamar.charge.pile.server.service.handler.MachinePacketHandler;
import com.huamar.charge.pile.server.service.handler.c.MachineCAuthenticationHandler;
import com.huamar.charge.pile.server.service.handler.c.MachineCHandlers;
import com.huamar.charge.pile.server.service.handler.c.MachineCHeartbeatHandler;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import com.huamar.charge.pile.server.session.SessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;


/**
 * The type Server net handler for mc.
 */
@Slf4j
@ChannelHandler.Sharable
@Component
@RequiredArgsConstructor
public class ServerNetHandlerForMC extends SimpleChannelInboundHandler<ProtocolCPacket> {

    private final MachineCAuthenticationHandler machineCAuthenticationHandler;
    private final MachineCHeartbeatHandler machineCHeartbeatHandler;
    private final MachineCHandlers machineCHandlers;

    /**
     * 消息生产者
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProtocolCPacket cPacket) {
        AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
        SessionChannel session = SessionManager.get(ctx.channel().attr(machineId).get());
        ctx.attr(NAttrKeys.PROTOCOL_C_LATEST_PACKET).set(cPacket);
        Integer latestOrderV = ctx.attr(NAttrKeys.PROTOCOL_C_LATEST_ORDER_V).get();
        if (latestOrderV == null) {
            latestOrderV = 0;
        }
        int currentPacketOrderV = cPacket.getOrderV();
        if (latestOrderV <= currentPacketOrderV) {
            ctx.attr(NAttrKeys.PROTOCOL_C_LATEST_ORDER_V).set(currentPacketOrderV);
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
            case (byte) 0x9B:
                machineCHandlers.handler0x9B(cPacket, ctx);
                //  qrcode set response
            case (byte) 0x55:
                machineCHandlers.handler0x55(cPacket, ctx);
                break;
            case (byte) 0x3b:
                machineCHandlers.handler0x3b(cPacket, ctx);
                break;
        }
    }

}
