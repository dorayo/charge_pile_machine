package com.huamar.charge.pile.server.service.handler;

import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.convert.McFaultConvert;
import com.huamar.charge.pile.entity.dto.fault.McFaultPutReqDTO;
import com.huamar.charge.pile.entity.dto.resp.McCommonResp;
import com.huamar.charge.pile.enums.McAnswerEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.pile.server.service.factory.McAnswerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;

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
        try {

            String ip = sessionChannel.getIp();
            StringJoiner logText = new StringJoiner(StringPool.COMMA, StringPool.EMPTY, StringPool.EMPTY);
            logText.add(MessageFormatter.format("设备故障上报，ip={}", ip).getMessage());
            McFaultPutReqDTO reqDTO = this.reader(packet);
            printInfo(logText, reqDTO);

            answerFactory.getExecute(McAnswerEnum.COMMON).execute(McCommonResp.ok(packet), sessionChannel);
        }catch (Exception e){
            answerFactory.getExecute(McAnswerEnum.COMMON).execute(McCommonResp.fail(packet), sessionChannel);
        }
    }

    /**
     * 拼接打印日志
     * @param logText logText
     */
    private void printInfo(StringJoiner logText, McFaultPutReqDTO faultPutReqDTO) {
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
    public McFaultPutReqDTO reader(DataPacket packet) {
        return McFaultConvert.INSTANCE.convert(packet);
    }
}
