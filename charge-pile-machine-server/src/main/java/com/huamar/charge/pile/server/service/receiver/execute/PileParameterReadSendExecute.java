package com.huamar.charge.pile.server.service.receiver.execute;

import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.parameter.PileParameterReadDTO;
import com.huamar.charge.pile.enums.McParameterEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.server.service.factory.McParameterFactory;
import com.huamar.charge.pile.server.service.receiver.PileMessageExecute;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 远程参数下发
 * date: 2023/08
 *
 * @author TiAmo(13721682347 @ 163.com)
 **/
@Component("PileMessageParameterReadSendExecute")
@RequiredArgsConstructor
public class PileParameterReadSendExecute implements PileMessageExecute {

    private final McParameterFactory mcParameterFactory;

    @Override
    public MessageCodeEnum getCode() {
        return MessageCodeEnum.PILE_PARAMETER_READ_SEND;
    }


    @SuppressWarnings("DuplicatedCode")
    @Override
    public void execute(MessageData<String> body) {
        PileParameterReadDTO parameterReadDTO = JSONParser.parseObject(body.getData(), PileParameterReadDTO.class);
        parameterReadDTO.setTime(BCDUtils.bcdTime());
        parameterReadDTO.setRetain(0);
        mcParameterFactory.getExecute(McParameterEnum.READ).execute(parameterReadDTO);
    }

}
