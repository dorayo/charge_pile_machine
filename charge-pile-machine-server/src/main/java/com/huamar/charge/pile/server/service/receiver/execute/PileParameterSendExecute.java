package com.huamar.charge.pile.server.service.receiver.execute;

import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.parameter.PileParamItemDTO;
import com.huamar.charge.pile.entity.dto.parameter.PileParameterDTO;
import com.huamar.charge.pile.entity.dto.platform.PileParamItemReqDTO;
import com.huamar.charge.pile.enums.McParameterEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.server.service.factory.McParameterFactory;
import com.huamar.charge.pile.server.service.receiver.PileMessageExecute;
import com.huamar.charge.common.util.JSONParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 远程参数下发
 * date: 2023/08
 *
 * @author TiAmo(13721682347 @ 163.com)
 **/
@Component("PileMessageParameterSendExecute")
@RequiredArgsConstructor
public class PileParameterSendExecute implements PileMessageExecute {

    private final McParameterFactory mcParameterFactory;

    @Override
    public MessageCodeEnum getCode() {
        return MessageCodeEnum.PILE_PARAMETER_UPDATE;
    }


    @SuppressWarnings("DuplicatedCode")
    @Override
    public void execute(MessageData<String> body) {
        PileParamItemReqDTO pileParamItemReqDTO = JSONParser.parseObject(body.getData(), PileParamItemReqDTO.class);
        List<PileParamItemDTO> data = pileParamItemReqDTO.getList();
        PileParameterDTO parameterDTO = new PileParameterDTO();
        parameterDTO.setParamNumber((byte) data.size());
        parameterDTO.setList(data);
        parameterDTO.setIdCode(pileParamItemReqDTO.getIdCode());
        mcParameterFactory.getExecute(McParameterEnum.SEND).execute(parameterDTO);
    }

}
