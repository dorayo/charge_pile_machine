package com.huamar.charge.pile.server.handle.netty;

import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.server.service.factory.MachinePacketFactory;
import com.huamar.charge.pile.server.service.handler.MachinePacketHandler;
import com.huamar.charge.pile.server.session.SessionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务端监听器 ChannelInboundHandlerAdapter 区别，SimpleChannelInboundHandler.channelRead0 可是实现释放数据
 *
 * @author TiAmo
 */
@Slf4j
public class ServerNetHandler extends SimpleChannelInboundHandler<DataPacket> {

    private final MachinePacketFactory machinePacketFactory;

    public ServerNetHandler(MachinePacketFactory machinePacketFactory) {
        this.machinePacketFactory = machinePacketFactory;
    }

    /**
     * 服务端读取数据
     *
     * @param channelHandlerContext channelHandlerContext
     * @param dataPacket            dataPacket
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DataPacket dataPacket) {
        String bsId = new String((dataPacket).getIdCode());
        SessionChannel sessionChannel = SessionManager.get(bsId);
        String code = HexExtUtil.encodeHexStr(dataPacket.getMsgId());
        MachinePacketHandler<DataPacket> handler = machinePacketFactory.getHandler(ProtocolCodeEnum.getByCode(code));
        handler.handler(dataPacket, sessionChannel);
    }

}
