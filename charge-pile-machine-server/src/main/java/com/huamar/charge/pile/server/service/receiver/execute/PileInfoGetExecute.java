package com.huamar.charge.pile.server.service.receiver.execute;

import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.pile.api.dto.PileDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.server.service.machine.MachineService;
import com.huamar.charge.pile.server.service.receiver.PileMessageExecute;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 远程参数下发
 * date: 2023/08
 *
 * @author TiAmo(13721682347 @ 163.com)
 **/
@Component
@RequiredArgsConstructor
public class PileInfoGetExecute implements PileMessageExecute {

    private final MachineService machineService;

    @Override
    public MessageCodeEnum getCode() {
        return MessageCodeEnum.PILE_AUTH;
    }


    @SuppressWarnings("DuplicatedCode")
    @Override
    public void execute(MessageData<String> body) {
        PileDTO data = JSONParser.parseObject(body.getData(), PileDTO.class);
        machineService.putCache(data.getPileCode(), data);
    }

}
