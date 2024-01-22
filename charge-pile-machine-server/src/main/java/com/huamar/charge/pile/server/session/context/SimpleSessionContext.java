package com.huamar.charge.pile.server.session.context;

import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.enums.McTypeEnum;
import com.huamar.charge.pile.server.session.MachineSessionContext;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.MDC;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Objects;

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
    @SuppressWarnings("DuplicatedCode")
    @Override
    public boolean writePacket(DataPacket packet, SessionChannel channel) {
        try {
            SimpleSessionChannel sessionChannel = (SimpleSessionChannel) channel;
            Assert.notNull(sessionChannel, "session is null");
            ChannelHandlerContext ctx = sessionChannel.channel();
            String sessionId = SessionManager.getSessionId(ctx);
            ctx.writeAndFlush(packet).addListener(future -> {
                log.info("SLX writePacket session:{} idCode:{} success:{} case:{}", sessionId, new String(packet.getIdCode()), future.isSuccess(), ExceptionUtils.getMessage(future.cause()));
            });
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
    @SuppressWarnings("DuplicatedCode")
    @Override
    public boolean writePacket(DataPacket packet) {
        try {
            SimpleSessionChannel sessionChannel = (SimpleSessionChannel) SessionManager.get(new String(packet.getIdCode()));
            Assert.notNull(sessionChannel, "session is null");
            ChannelHandlerContext ctx = sessionChannel.channel();
            if (sessionChannel.getType() == McTypeEnum.C) {
                return true;
            }
            String sessionId = SessionManager.getSessionId(ctx);
            ctx.writeAndFlush(packet).addListener(future -> {
                log.info("SLX writePacket:{} idCode:{} success:{} case:{}", sessionId, new String(packet.getIdCode()), future.isSuccess(), ExceptionUtils.getMessage(future.cause()));
            });
        }
        catch (IllegalArgumentException e){
            String[] stackFrames = ExceptionUtils.getStackFrames(e);
            log.warn("writePacket ERROR:{}", ExceptionUtils.getMessage(e));
            log.warn("writePacket stackTrace:{}", StringUtils.join(stackFrames, ",", 0, Math.min(stackFrames.length, 3)));
        }
        catch (Exception e) {
            log.error("writePacket error", e);
            return false;
        }
        return true;
    }

}
