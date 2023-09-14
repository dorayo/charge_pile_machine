package com.huamar.charge.pile.server.session.context;

import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.server.protocol.TioPacket;
import com.huamar.charge.pile.server.session.MachineSessionContext;
import com.huamar.charge.pile.server.session.TioSessionChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.tio.core.Tio;
import org.tio.server.ServerTioConfig;

/**
 * 设备业务上下文
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
public class TioSessionContext implements MachineSessionContext {


    private final ServerTioConfig serverTioConfig;

    public TioSessionContext(@Autowired(required = true) ServerTioConfig serverTioConfig) {
        this.serverTioConfig = serverTioConfig;
    }

    /**
     * 消息应答
     *
     * @param packet  packet
     * @param channel channel
     */
    @Override
    public boolean writePacket(DataPacket packet, SessionChannel channel) {
        TioSessionChannel sessionChannel = (TioSessionChannel) channel;
        return Tio.send(sessionChannel.channel(), new TioPacket(packet));
    }

    /**
     * 发送消息
     *
     * @param packet packet
     * @return boolean
     */
    @Override
    public boolean writePacket(DataPacket packet) {
        return Tio.sendToBsId(serverTioConfig, new String(packet.getIdCode()), new TioPacket(packet));
    }
}
