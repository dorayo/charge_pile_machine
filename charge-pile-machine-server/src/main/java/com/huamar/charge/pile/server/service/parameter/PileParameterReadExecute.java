package com.huamar.charge.pile.server.service.parameter;

import cn.hutool.core.util.IdUtil;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.pile.convert.McParameterConvert;
import com.huamar.charge.pile.entity.dto.PileParameterReqDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.enums.McParameterEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 远程控制执行下发
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PileParameterReadExecute implements PileParameterExecute<PileParameterReqDTO> {

    /**
     * 消息生产者
     */
    private final PileMessageProduce pileMessageProduce;

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McParameterEnum getCode() {
        return McParameterEnum.READ_CONFIG;
    }

    /**
     * 执行方法
     *
     * @param command command
     */
    @Override
    public void execute(PileParameterReqDTO command) {
        log.info("Parameter Read idCode:{}", command.getIdCode());
        MessageData<PileParameterReqDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_PARAMETER_READ, command);
        messageData.setBusinessId(command.getIdCode());
        messageData.setMessageId(IdUtil.simpleUUID());
        messageData.setRequestId(IdUtil.simpleUUID());
        messageData.setBusinessCode(MessageCodeEnum.PILE_PARAMETER_READ.getCode());
        pileMessageProduce.send(messageData);
    }


    /**
     * 读取参数
     *
     * @param packet packet
     * @return McBaseParameterDTO
     */
    @Override
    public PileParameterReqDTO reader(DataPacket packet) {
        return McParameterConvert.INSTANCE.convert(packet);
    }

}
