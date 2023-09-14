package com.huamar.charge.pile.server.session.context;

import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.server.session.MachineSessionContext;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 默认的上下文管理 基于 netty
 * 2023/08
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
public class SimpleSessionContext implements MachineSessionContext {


    /**
     * 消息应答
     *
     * @param packet  packet
     * @param channel channel
     */
    @Override
    public boolean writePacket(DataPacket packet, SessionChannel channel) {
        try {
            SimpleSessionChannel sessionChannel = (SimpleSessionChannel) channel;
            sessionChannel.channel().writeAndFlush(packet).sync();
        } catch (InterruptedException e) {
            log.error("writePacket error");
            return false;
        }
        return true;
    }

    /**
     * 发送消息
     *
     * @param packet packet
     * @return boolean
     */
    @Override
    public boolean writePacket(DataPacket packet) {
        try {
            SessionChannel sessionChannel = SessionManager.get(new String(packet.getIdCode()));
            SimpleSessionChannel channel = (SimpleSessionChannel) sessionChannel.channel();
            channel.channel().writeAndFlush(packet).sync();
        } catch (InterruptedException e) {
            log.error("writePacket error");
            return false;
        }
        return true;
    }

}
