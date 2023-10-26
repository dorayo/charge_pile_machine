package com.huamar.charge.pile.server.service.handler;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.convert.McFaultConvert;
import com.huamar.charge.pile.entity.dto.fault.PileFaultPutReqDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.resp.McCommonResp;
import com.huamar.charge.pile.enums.McAnswerEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.pile.server.service.factory.McAnswerFactory;
import com.huamar.charge.pile.server.service.produce.PileFaultMessageProduce;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.StringJoiner;

/**
 * 设备故障上报
 * 2023/08/01
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineFaultHandler implements MachinePacketHandler<DataPacket> {

    /**
     * 设备终端上下文
     */
    private final McAnswerFactory answerFactory;

    /**
     * 设备故障消息发送
     */
    private final PileFaultMessageProduce faultMessageProduce;

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public ProtocolCodeEnum getCode() {
        return ProtocolCodeEnum.FAULT;
    }

    /**
     * 执行器
     *
     * @param packet         packet
     * @param sessionChannel sessionChannel
     */
    @Override
    public void handler(DataPacket packet, SessionChannel sessionChannel) {
        String idCode = new String(packet.getIdCode());
        try {
            answerFactory.getExecute(McAnswerEnum.COMMON).execute(McCommonResp.ok(packet), sessionChannel);

            String ip = sessionChannel.getIp();
            StringJoiner logText = new StringJoiner(StringPool.COMMA, StringPool.EMPTY, StringPool.EMPTY);
            logText.add(MessageFormatter.format("设备故障上报，ip={}，idCode:{}", ip, idCode).getMessage());
            PileFaultPutReqDTO reqDTO = this.reader(packet);
            printInfo(logText, reqDTO);

            faultMessageProduce.send(this.buildMessageData(reqDTO));
        }catch (Exception e){
            log.error("设备故障上报失败 ==> message:{}", e.getMessage(), e);
            answerFactory.getExecute(McAnswerEnum.COMMON).execute(McCommonResp.fail(packet), sessionChannel);
            if(StringUtils.isNotBlank(idCode)){
                PileFaultPutReqDTO faultPutReqDTO = new PileFaultPutReqDTO();
                faultMessageProduce.send(this.buildMessageData(faultPutReqDTO));
            }
        }
    }


    /**
     * 构建消息体
     *
     * @param faultPutReqDTO faultPutReqDTO
     * @return MessageData<PileFaultPutReqDTO>
     */
    private MessageData<PileFaultPutReqDTO> buildMessageData(PileFaultPutReqDTO faultPutReqDTO){
        MessageData<PileFaultPutReqDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_FAULT, faultPutReqDTO);
        Snowflake snowflake = IdUtil.getSnowflake();
        String string = snowflake.nextIdStr();
        messageData.setMessageId(string);
        messageData.setBusinessId(faultPutReqDTO.getIdCode());
        messageData.setDateTime(LocalDateTime.now());
        messageData.setRequestId(string);
        return messageData;
    }


    /**
     * 拼接打印日志
     * @param logText logText
     */
    private void printInfo(StringJoiner logText, PileFaultPutReqDTO faultPutReqDTO) {
        if (!log.isInfoEnabled()) {
            return;
        }

        try {
            logText.add(MessageFormatter.format("data:{}", JSONParser.jsonString(faultPutReqDTO)).getMessage());
            log.info(logText.toString());
        } catch (Exception ignored) {
        }
    }

    /**
     * 读取参数
     *
     * @param packet packet
     * @return McBaseParameterDTO
     */
    @Override
    public PileFaultPutReqDTO reader(DataPacket packet) {
        return McFaultConvert.INSTANCE.convert(packet);
    }
}
