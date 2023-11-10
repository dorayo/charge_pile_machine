package com.huamar.charge.pile.server.session.context;

import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.enums.McTypeEnum;
import com.huamar.charge.pile.server.session.MachineSessionContext;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

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
            Assert.notNull(sessionChannel, "session is null");
            sessionChannel.channel().writeAndFlush(packet);
        } catch (Exception e) {
            log.error("writePacket error", e);
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
            SimpleSessionChannel sessionChannel = (SimpleSessionChannel) SessionManager.get(new String(packet.getIdCode()));
            ChannelHandlerContext channelHandlerContext = sessionChannel.channel();
            if (sessionChannel.getType() == McTypeEnum.C) {
                return true;
            }
            Assert.notNull(sessionChannel, "session is null");
            channelHandlerContext.writeAndFlush(packet);
        } catch (Exception e) {
            log.error("writePacket error", e);
            return false;
        }
        return true;
    }

}
