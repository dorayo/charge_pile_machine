package com.huamar.charge.pile.server.service.receiver.execute;

import com.alibaba.fastjson.JSONObject;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.pile.entity.dto.command.ADLampCommandDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.enums.McCommandEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.server.service.factory.McCommandFactory;
import com.huamar.charge.pile.server.service.receiver.PileMessageExecute;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 远程参数下发
 * date: 2023/08
 *
 * @author TiAmo(13721682347 @ 163.com)
 **/
@Component
@RequiredArgsConstructor
public class PileADLampExecute implements PileMessageExecute {


    private final McCommandFactory commandFactory;

    @Override
    public MessageCodeEnum getCode() {
        return MessageCodeEnum.PILE_AD_LAMP;
    }


    @SuppressWarnings("DuplicatedCode")
    @Override
    public void execute(MessageData<String> body) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        JSONObject jsonObject = (JSONObject) JSONParser.parseObject(body.getData());

        String on = jsonObject.getString("on");
        String off = jsonObject.getString("off");

        ADLampCommandDTO adLampCommandDTO = new ADLampCommandDTO();
        adLampCommandDTO.setIdCode(jsonObject.getString("idCode"));

        if(StringUtils.isBlank(on)){
            adLampCommandDTO = new ADLampCommandDTO();
            adLampCommandDTO.setOnHour((byte) 0xff);
            adLampCommandDTO.setOnMinute((byte) 0xff);
            adLampCommandDTO.setOffHour((byte) 0xff);
            adLampCommandDTO.setOffMinute((byte) 0xff);
        }

        if(StringUtils.isBlank(off)){
            adLampCommandDTO = new ADLampCommandDTO();
            adLampCommandDTO.setOnHour((byte) 0x00);
            adLampCommandDTO.setOnMinute((byte) 0x00);
            adLampCommandDTO.setOffHour((byte) 0x00);
            adLampCommandDTO.setOffMinute((byte) 0x00);
        }

        if(StringUtils.isNotBlank(on) && StringUtils.isNotBlank(off)){
            LocalTime onTime = LocalTime.parse(on, formatter);
            LocalTime offTime = LocalTime.parse(off, formatter);
            adLampCommandDTO.setOnHour((byte) onTime.getHour());
            adLampCommandDTO.setOnMinute((byte) onTime.getMinute());
            adLampCommandDTO.setOffHour((byte) offTime.getHour());
            adLampCommandDTO.setOffMinute((byte) offTime.getMinute());
        }

        commandFactory.getExecute(McCommandEnum.CUSTOM_AD_LAMP).execute(adLampCommandDTO);
    }

}
