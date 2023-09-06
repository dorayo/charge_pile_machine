package com.huamar.charge.pile;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.huamar.charge.pile.entity.dto.platform.PileChargeControlDTO;
import com.huamar.charge.common.common.constant.QueueConstant;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.parameter.McParamItemDTO;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.common.util.JSONParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 通用应答结果处理执行工厂
 * Date: 2023/07/24
 * </p>
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
public class ChargeConfigTest {

    private ConnectionFactory connectionFactory;

    @Test
    public void sendMessage() {
        McParamItemDTO mcParamItemDTO = new McParamItemDTO();
        mcParamItemDTO.setId((short) 6);
        mcParamItemDTO.setParamData("7");

        List<McParamItemDTO> data = new ArrayList<>();
        data.add(mcParamItemDTO);


        MessageData<List<McParamItemDTO>> messageData = new MessageData<>(MessageCodeEnum.PILE_PARAMETER_UPDATE, data);
        messageData.setBusinessId("471000220714302005");

        MessageProperties messageProperties = new MessageProperties();
        Snowflake snowflake = IdUtil.getSnowflake();
        messageProperties.setMessageId(snowflake.nextIdStr());

        RabbitTemplate rabbitTemplate = new RabbitTemplate(this.connectionFactory);
        rabbitTemplate.send(QueueConstant.PILE_COMMON_QUEUE, new Message(JSONParser.jsonString(messageData).getBytes(), messageProperties));
    }

    @Test
    public void chargeControl() {
        PileChargeControlDTO chargeControl = new PileChargeControlDTO();
        chargeControl.setChargeControl(0);
        chargeControl.setGunSort(1);
        chargeControl.setChargeEndType(3);
        chargeControl.setChargeEndValue(2);
        chargeControl.setOrderSerialNumber(IdUtil.simpleUUID());
        chargeControl.setBalance(new BigDecimal("5"));

        MessageData<PileChargeControlDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_START_CHARGE, chargeControl);
        messageData.setBusinessId("471000220714302005");

        MessageProperties messageProperties = new MessageProperties();
        Snowflake snowflake = IdUtil.getSnowflake();
        messageProperties.setMessageId(snowflake.nextIdStr());

        // 消息发送
        RabbitTemplate rabbitTemplate = new RabbitTemplate(this.connectionFactory);
        rabbitTemplate.send(QueueConstant.PILE_COMMON_QUEUE, new Message(JSONParser.jsonString(messageData).getBytes(), messageProperties));



        String string = IdUtil.fastUUID();
        log.info(string);

        byte toByte = Convert.intToByte(200);
        log.info("toByte:{}", toByte);
    }


    @Before
    public void defaultRabbitConnectionFactory(){
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setAddresses("rabbit.hm.com");
        cachingConnectionFactory.setHost("rabbit.hm.com");
        cachingConnectionFactory.setPort(5672);
        cachingConnectionFactory.setUsername("guest");
        cachingConnectionFactory.setPassword("guest");
        cachingConnectionFactory.setVirtualHost("/");
        connectionFactory = cachingConnectionFactory;
    }



}
