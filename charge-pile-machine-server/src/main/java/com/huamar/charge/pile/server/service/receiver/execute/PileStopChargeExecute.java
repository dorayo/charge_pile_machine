package com.huamar.charge.pile.server.service.receiver.execute;

import com.huamar.charge.pile.entity.dto.platform.PileChargeControlDTO;
import com.huamar.charge.pile.entity.dto.command.McChargeCommandDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.enums.McCommandEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.server.service.McCommandFactory;
import com.huamar.charge.pile.server.service.receiver.PileMessageExecute;
import com.huamar.charge.pile.util.JSONParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 充电控制
 * tag_date: 2023.08.07
 *
 * @author TiAmo(13721682347 @ 163.com)
 **/
@Component
@RequiredArgsConstructor
public class PileStopChargeExecute implements PileMessageExecute {

    private final McCommandFactory mcCommandFactory;

    @Override
    public MessageCodeEnum getCode() {
        return MessageCodeEnum.PILE_STOP_CHARGE;
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void execute(MessageData<String> body) {
        BigDecimal multiply = new BigDecimal("100");
        PileChargeControlDTO chargeControl = JSONParser.parseObject(body.getData(), PileChargeControlDTO.class);
        McChargeCommandDTO chargeCommand = new McChargeCommandDTO();
        chargeCommand.setChargeEndType((byte) 0);
        chargeCommand.setChargeControl(chargeControl.getChargeControl().byteValue());
        chargeCommand.setGunSort(chargeControl.getGunSort().byteValue());
        chargeCommand.setChargeEndType(chargeControl.getChargeEndType().byteValue());
        chargeCommand.setChargeEndValue(chargeControl.getChargeEndValue());
        chargeCommand.setOrderSerialNumber(chargeControl.getOrderSerialNumber().getBytes());
        chargeCommand.setBalance(chargeControl.getBalance().multiply(multiply).intValue());
        chargeCommand.setIdCode(body.getBusinessId());
        mcCommandFactory.getExecute(McCommandEnum.CHARGE).execute(chargeCommand);
    }

}
