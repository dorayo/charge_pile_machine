package com.huamar.charge.pile.server.service.handler;

import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.config.PileMachineProperties;
import com.huamar.charge.pile.convert.McHeartbeatConvert;
import com.huamar.charge.pile.entity.dto.fault.McHeartbeatReqDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.PileHeartbeatDTO;
import com.huamar.charge.pile.entity.dto.resp.McCommonResp;
import com.huamar.charge.pile.enums.*;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.pile.server.service.factory.McAnswerFactory;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import com.huamar.charge.common.util.JSONParser;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

/**
 * 设备心跳包
 * 2023/08/01
 *
 * @author TiAmo(13721682347@163.com)
 */
@Service
@RequiredArgsConstructor
public class MachineHeartbeatHandler implements MachinePacketHandler<DataPacket> {

    private final Logger log = LoggerFactory.getLogger(LoggerEnum.HEARTBEAT_LOGGER.getCode());

    /**
     * 设备终端上下文
     */
    private final McAnswerFactory answerFactory;


    /**
     * 消息投递
     */
    private final PileMessageProduce pileMessageProduce;

    /**
     * 设备参数配置
     */
    private final PileMachineProperties pileMachineProperties;

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public ProtocolCodeEnum getCode() {
        return ProtocolCodeEnum.HEART_BEAT;
    }

    /**
     * 执行器
     *
     * @param packet         packet
     * @param sessionChannel sessionChannel
     */
    @Override
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
        }catch (Exception e){
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
        }catch (Exception e){
            log.error("心跳包发送远程失败 mcMessageProduce send error e:{}", e.getMessage(), e);
        }
    }


    /**
     * 读取参数
     *
     * @param packet packet
     * @return McBaseParameterDTO
     */
    @Override
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
