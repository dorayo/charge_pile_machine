package com.huamar.charge.pile.server.handle.netty.b;

import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.server.service.factory.b.MachineBPacketFactory;
import com.huamar.charge.pile.server.service.handler.MachinePacketHandler;
import com.huamar.charge.pile.server.session.SessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 服务端监听器 ChannelInboundHandlerAdapter 区别，SimpleChannelInboundHandler.channelRead0 可是实现释放数据
 *
 * @author TiAmo
 */
@Slf4j
@ChannelHandler.Sharable
public class ServerNetBHandler extends SimpleChannelInboundHandler<DataPacket> {

    /**
     * 协议code前缀
     */
    private final static String protocolCodePrefix = "b:";

    private final MachineBPacketFactory packetFactory;

    public ServerNetBHandler(MachineBPacketFactory packetFactory) {
        this.packetFactory = packetFactory;
    }

    /**
     * 服务端读取数据
     *
     * @param channelHandlerContext channelHandlerContext
     * @param dataPacket            dataPacket
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DataPacket dataPacket) {
        if(log.isTraceEnabled()){
            log.trace("InboundHandler:{}", this.getClass().getSimpleName());
        }

        String bsId = new String((dataPacket).getIdCode());
        SessionChannel sessionChannel = SessionManager.get(bsId);
        String code = HexExtUtil.encodeHexStr(dataPacket.getMsgId());
        String format_code = String.format("%s%s", protocolCodePrefix, code);
        ProtocolCodeEnum codeEnum = ProtocolCodeEnum.getByCode(format_code);
        if(Objects.isNull(codeEnum)){
            codeEnum = ProtocolCodeEnum.getByCode(code);
        }

        //noinspection DuplicatedCode
        if(Objects.isNull(codeEnum)){
            channelHandlerContext.fireChannelRead(dataPacket);
            return;
        }

        MachinePacketHandler<DataPacket> handler = packetFactory.getHandler(codeEnum);
        if(Objects.isNull(handler)){
            channelHandlerContext.fireChannelRead(dataPacket);
            return;
        }

        handler.handler(dataPacket, sessionChannel);
    }

}
