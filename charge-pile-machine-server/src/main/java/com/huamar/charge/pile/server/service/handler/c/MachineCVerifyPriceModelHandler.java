package com.huamar.charge.pile.server.service.handler.c;

import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.config.PileMachineProperties;
import com.huamar.charge.pile.convert.McHeartbeatConvert;
import com.huamar.charge.pile.entity.dto.fault.McHeartbeatReqDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.PileHeartbeatDTO;
import com.huamar.charge.pile.entity.dto.resp.McCommonResp;
import com.huamar.charge.pile.enums.LoggerEnum;
import com.huamar.charge.pile.enums.McAnswerEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.server.service.factory.McAnswerFactory;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import com.huamar.charge.pile.utils.binaryBuilder.BinaryBuilders;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

/**
 * The type Machine c verify price model handler.
 */
@Service
@RequiredArgsConstructor
public class MachineCVerifyPriceModelHandler {

    private final Logger log = LoggerFactory.getLogger(LoggerEnum.HEARTBEAT_LOGGER.getCode());

    private final McAnswerFactory answerFactory;


    private final PileMessageProduce pileMessageProduce;

    private final PileMachineProperties pileMachineProperties;

    /**
     * Gets code.
     *
     * @return the code
     */
    public ProtocolCodeEnum getCode() {
        return ProtocolCodeEnum.HEART_BEAT;
    }

    /**
     * Handler.
     *
     * @param packet         the packet
     * @param sessionChannel the session channel
     */
    public void handler(DataPacket packet, SessionChannel sessionChannel) {
        McHeartbeatReqDTO reqDTO = null;
        try {
            String ip = sessionChannel.getIp();
            log.info("设备心跳包，ip={}, idCode:{}, msgNum:{}", ip, new String(packet.getIdCode()), packet.getMsgNumber());
            reqDTO = this.reader(packet);
            reqDTO.setIdCode(new String(packet.getIdCode()));
            log.info("设备心跳包，data:{}", JSONParser.jsonString(reqDTO));
            // 通用应答
            answerFactory.getExecute(McAnswerEnum.COMMON).execute(McCommonResp.ok(packet), sessionChannel);
        } catch (Exception e) {
            answerFactory.getExecute(McAnswerEnum.COMMON).execute(McCommonResp.fail(packet), sessionChannel);
        }

        try {
            Assert.notNull(reqDTO, "McHeartbeatReqDTO noNull");
            PileHeartbeatDTO pileHeartbeatDTO = new PileHeartbeatDTO();
            pileHeartbeatDTO.setProtocolNumber(reqDTO.getProtocolNumber());
            pileHeartbeatDTO.setIdCode(reqDTO.getIdCode());
            pileHeartbeatDTO.setDateTime(LocalDateTime.now());
            pileHeartbeatDTO.setTime(reqDTO.getTime().toString());
            MessageData<PileHeartbeatDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_HEART_BEAT, pileHeartbeatDTO);
            pileMessageProduce.send(messageData);
        } catch (Exception e) {
            log.error("心跳包发送远程失败 mcMessageProduce send error e:{}", e.getMessage(), e);
        }
    }

    /**
     * Handler.
     *
     * @param packet         the packet
     * @param sessionChannel the session channel
     * @param ctx            the ctx
     */
    public void handler(ProtocolCPacket packet, SessionChannel sessionChannel, ChannelHandlerContext ctx) {
        McHeartbeatReqDTO reqDTO = null;
        try {
            packet.getBody()[8] = 0;
            ByteBuf response = BinaryBuilders.protocolCLeResponseBuilder(packet.getBody(), packet.getOrderVBf(), (byte) 0x04);
            ctx.channel().writeAndFlush(response).addListener((f) -> {
                if (f.isSuccess()) {
                    log.info("write heartbeat success");
                } else {
                    log.error("write heartbeat error");
                    f.cause().printStackTrace();
                }
            });
            //            log.info("设备心跳包，ip={}, idCode:{}, msgNum:{}", ip, new String(packet.getIdCode()), packet.getMsgNumber());
//            reqDTO = this.reader(packet);
//            reqDTO.setIdCode(new String(packet.getIdCode()));
//            log.info("设备心跳包，data:{}", JSONParser.jsonString(reqDTO));
            // 通用应答
//            answerFactory.getExecute(McAnswerEnum.COMMON).execute(McCommonResp.ok(packet), sessionChannel);
        } catch (Exception e) {
//            answerFactory.getExecute(McAnswerEnum.COMMON).execute(McCommonResp.fail(packet), sessionChannel);
        }

        try {
            Assert.notNull(reqDTO, "McHeartbeatReqDTO noNull");
            PileHeartbeatDTO pileHeartbeatDTO = new PileHeartbeatDTO();
            pileHeartbeatDTO.setProtocolNumber(reqDTO.getProtocolNumber());
            pileHeartbeatDTO.setIdCode(reqDTO.getIdCode());
            pileHeartbeatDTO.setDateTime(LocalDateTime.now());
            pileHeartbeatDTO.setTime(reqDTO.getTime().toString());
            MessageData<PileHeartbeatDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_HEART_BEAT, pileHeartbeatDTO);
            pileMessageProduce.send(messageData);
        } catch (Exception e) {
            log.error("心跳包发送远程失败 mcMessageProduce send error e:{}", e.getMessage(), e);
        }
    }


    /**
     * Reader mc heartbeat req dto.
     *
     * @param packet the packet
     * @return the mc heartbeat req dto
     */
    public McHeartbeatReqDTO reader(DataPacket packet) {
        return McHeartbeatConvert.INSTANCE.convert(packet);
    }

//    @PostConstruct
//    public void logTest() {
//        Runnable runnable = new Runnable() {
//            final String idCode = "123456789012345678";
//            @Override
//            public void run() {
//                MDC.put(ConstEnum.ID_CODE.getCode(), idCode);
//                log.info("日志测试-设备心跳包，idCode:{}，ip={}", idCode, "0.0.0.0");
//                MDC.clear();
//            }
//        };
//        new Thread(runnable).start();
//    }
}
