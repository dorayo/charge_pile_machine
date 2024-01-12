package com.huamar.charge.pile.server.service.handler.c;

import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.PileHeartbeatDTO;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import com.huamar.charge.pile.utils.binaryBuilder.BinaryBuilders;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 设备心跳包
 * 2023/08/01
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MachineCHeartbeatHandler {

    /**
     * 消息投递
     */
    private final PileMessageProduce pileMessageProduce;


    public void handler(ProtocolCPacket packet, SessionChannel sessionChannel, ChannelHandlerContext ctx) {
        AttributeKey<String> machineId = AttributeKey.valueOf(ConstEnum.MACHINE_ID.getCode());
        String idCode = ctx.channel().attr(machineId).get();
        try {
            String ip = sessionChannel.getIp();
            byte[] body = packet.getBody();
            log.info("YKC 设备心跳包，ip={}, idCode:{}, gun:{}, status:{}, msgNum:{}", ip, idCode, body[7], body[8], packet.getOrderV());

            packet.getBody()[8] = 0;
            ByteBuf response = BinaryBuilders.protocolCLeResponseBuilder(packet.getBody(), packet.getOrderVBf(), (byte) 0x04);
            ctx.channel().writeAndFlush(response).addListener((f) -> {
                if (f.isSuccess()) {
                    log.info("YKC 设备心跳包回执 write heartbeat success");
                } else {
                    log.error("YKC 设备心跳包回执 write heartbeat error：{}", ExceptionUtils.getMessage(f.cause()));
                }
            });
        } catch (Exception e) {
            log.info("YKC 设备心跳包 heartbeat Error:{}", ExceptionUtils.getMessage(e), e);
        }

        try {
            PileHeartbeatDTO pileHeartbeatDTO = new PileHeartbeatDTO();
            pileHeartbeatDTO.setIdCode(idCode);
            pileHeartbeatDTO.setDateTime(LocalDateTime.now());
            MessageData<PileHeartbeatDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_HEART_BEAT, pileHeartbeatDTO);
            pileMessageProduce.send(messageData);
        } catch (Exception e) {
            log.error("心跳包发送远程失败 mcMessageProduce send error e:{}", e.getMessage(), e);
        }
    }

}
