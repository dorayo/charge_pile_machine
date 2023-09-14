package com.huamar.charge.pile.server.service.handler;

import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.convert.McCommonConvert;
import com.huamar.charge.pile.entity.dto.McCommonReq;
import com.huamar.charge.pile.entity.dto.command.MessageCommonRespDTO;
import com.huamar.charge.pile.enums.PileCommonResultEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.server.service.factory.McCommonResultFactory;
import com.huamar.charge.pile.server.service.command.MessageCommandRespService;
import com.huamar.charge.pile.server.service.common.McCommonResultExecute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 通用应答处理
 * 2023/08/01
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineCommonResultHandler implements MachinePacketHandler<DataPacket> {

    /**
     * 通用应答处理工厂
     */
    private final McCommonResultFactory commonResultFactory;

    /**
     * 消息应答处理
     */
    private final MessageCommandRespService messageCommandRespService;

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public ProtocolCodeEnum getCode() {
        return ProtocolCodeEnum.COMMON_ACK;
    }

    /**
     * 执行器
     *
     * @param packet         packet
     * @param sessionChannel sessionChannel
     */
    @Override
    public void handler(DataPacket packet, SessionChannel sessionChannel) {
        McCommonReq commonReq = null;
        String resCode = null;
        try {
            String ip = sessionChannel.getIp();
            commonReq = this.reader(packet);
            resCode = String.format("%04X", commonReq.getMsgResult());
            log.info("通用应答处理，ip={}, idCode:{}, resCode:{}", ip, commonReq.getIdCode(), resCode);
            PileCommonResultEnum commonResultEnum = PileCommonResultEnum.getByCode(resCode);
            if(Objects.isNull(commonResultEnum)){
                commonResultEnum = PileCommonResultEnum.UNKNOWN;
            }
            McCommonResultExecute<McCommonReq> execute = commonResultFactory.getExecute(commonResultEnum);
            execute.execute(commonReq);

            // 发送命令执行结果
            MessageCommonRespDTO commonRespDTO = messageCommandRespService.get(commonReq.getIdCode(), commonReq.getMsgNumber());
            if(Objects.nonNull(commonRespDTO)){
                commonRespDTO.setMsgResult(resCode);
                messageCommandRespService.sendCommonResp(commonRespDTO);
            }

        } catch (Exception e) {
            log.error("handler MachineCommon error:{}, e ->", e.getMessage(), e);
            MessageCommonRespDTO commonRespDTO = messageCommandRespService.get(new String(packet.getIdCode()), packet.getMsgNumber());
            if(Objects.nonNull(commonReq)){
                commonRespDTO.setTime(commonReq.getTime().toString());
            }
            if(StringUtils.isNotBlank(resCode)){
                commonRespDTO.setMsgResult(resCode);
                messageCommandRespService.sendCommonResp(commonRespDTO);
            }
        }
    }

    /**
     * 读取参数
     *
     * @param packet packet
     * @return McBaseParameterDTO
     */
    @Override
    public McCommonReq reader(DataPacket packet) {
        McCommonReq req = McCommonConvert.INSTANCE.convert(packet);
        req.setIdCode(new String(packet.getIdCode()));
        return req;
    }
}
