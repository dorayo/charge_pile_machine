package com.huamar.charge.pile.server.handle.netty.c;

import cn.hutool.core.lang.Assert;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.enums.ConstEnum;
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
        log.info("aa");
        AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
        SessionChannel session = SessionManager.get(ctx.channel().attr(machineId).get());
        Assert.notNull(session);
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
            case 0x09:
                machineCHandlers.handler0x09(cPacket, ctx);
        }
    }

}
