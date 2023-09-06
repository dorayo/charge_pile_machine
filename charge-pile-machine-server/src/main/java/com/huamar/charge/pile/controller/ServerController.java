package com.huamar.charge.pile.controller;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.common.constant.QueueConstant;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.parameter.McBaseParameterDTO;
import com.huamar.charge.pile.entity.dto.parameter.McParamItemDTO;
import com.huamar.charge.pile.entity.dto.parameter.McParameterDTO;
import com.huamar.charge.pile.entity.dto.parameter.McParameterReadDTO;
import com.huamar.charge.pile.entity.dto.platform.PileChargeControlDTO;
import com.huamar.charge.pile.enums.McParameterEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.server.service.McParameterFactory;
import com.huamar.charge.pile.server.service.parameter.McParameterExecute;
import com.huamar.charge.common.util.JSONParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 命令测试
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/server")
public class ServerController {


    private final McParameterFactory parameterFactory;

    private final ConnectionFactory connectionFactory;

    @SneakyThrows
    @PostMapping("/send")
    public Object send(@RequestParam("id") String id, @RequestParam("body") String body) {
        return "ok";
    }


    @SneakyThrows
    @PostMapping("/sendByToken")
    public Object sendByToken(@RequestParam("token") String id, @RequestParam("body") String body) {
        return "ok";
    }

    @PostMapping("/sendReadParameter")
    public Object sendReadParameter(@RequestParam("idCode") String idCode) {
        McParameterReadDTO parameterReadDTO = new McParameterReadDTO();
        parameterReadDTO.setTime(BCDUtils.bcdTime());
        parameterReadDTO.setIdCode(idCode);
        McParameterExecute<McBaseParameterDTO> execute = parameterFactory.getExecute(McParameterEnum.READ);
        execute.execute(parameterReadDTO);
        return "success";
    }

    @PostMapping("/setParameter")
    public Object setParameter(@RequestParam("idCode") String idCode) {
        McParameterDTO parameterDTO = new McParameterDTO();
        parameterDTO.setIdCode(idCode);
        McParamItemDTO mcParamItemDTO = new McParamItemDTO();
        mcParamItemDTO.setId((short) 6);
        mcParamItemDTO.setParamData("2500");

        List<McParamItemDTO> data = new ArrayList<>();
        data.add(mcParamItemDTO);
        parameterDTO.setDataList(data);
        parameterDTO.setParamNumber((byte) data.size());

        McParameterExecute<McBaseParameterDTO> execute = parameterFactory.getExecute(McParameterEnum.SEND);
        execute.execute(parameterDTO);
        return "success";
    }

    /**
     * @param idCode 设备码
     * @return Object
     */
    @PostMapping("/startCharge")
    public Object startCharge(@RequestParam("idCode") String idCode) {
        String orderNumber = IdUtil.simpleUUID();
        PileChargeControlDTO chargeControl = new PileChargeControlDTO();
        chargeControl.setGunSort(1);
        chargeControl.setChargeEndType(3);
        chargeControl.setChargeEndValue(3);
        chargeControl.setOrderSerialNumber(orderNumber);
        chargeControl.setBalance(new BigDecimal("3"));
        chargeControl.setIdCode(idCode);

        MessageData<PileChargeControlDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_START_CHARGE, chargeControl);
        messageData.setBusinessId(idCode);

        MessageProperties messageProperties = new MessageProperties();
        Snowflake snowflake = IdUtil.getSnowflake();
        messageProperties.setMessageId(snowflake.nextIdStr());

        // 消息发送
        RabbitTemplate rabbitTemplate = new RabbitTemplate(this.connectionFactory);
        rabbitTemplate.send(QueueConstant.PILE_COMMON_QUEUE, new Message(JSONParser.jsonString(messageData).getBytes(), messageProperties));

        return "success:{}" + orderNumber;
    }
    /**
     * @param idCode 设备码
     * @return Object
     */
    @PostMapping("/endCharge")
    public Object endCharge(
            @RequestParam("idCode") String idCode,
            @RequestParam("orderNumber") String orderNumber
    ) {
        PileChargeControlDTO chargeControl = new PileChargeControlDTO();
        chargeControl.setGunSort(1);
        chargeControl.setChargeEndType(3);
        chargeControl.setChargeEndValue(5);
        chargeControl.setOrderSerialNumber(orderNumber);
        chargeControl.setBalance(new BigDecimal("5"));
        chargeControl.setIdCode(idCode);

        MessageData<PileChargeControlDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_STOP_CHARGE, chargeControl);
        messageData.setBusinessId(idCode);

        MessageProperties messageProperties = new MessageProperties();
        Snowflake snowflake = IdUtil.getSnowflake();
        messageProperties.setMessageId(snowflake.nextIdStr());

        // 消息发送
        RabbitTemplate rabbitTemplate = new RabbitTemplate(this.connectionFactory);
        rabbitTemplate.send(QueueConstant.PILE_COMMON_QUEUE, new Message(JSONParser.jsonString(messageData).getBytes(), messageProperties));

        return "success" + orderNumber;
    }

}
