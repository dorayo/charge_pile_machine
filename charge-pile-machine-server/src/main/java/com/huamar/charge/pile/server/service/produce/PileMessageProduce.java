package com.huamar.charge.pile.server.service.produce;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.pile.config.PileMachineProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;


/**
 * 消息生产者
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PileMessageProduce implements InitializingBean, RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    private final ConnectionFactory connectionFactory;

    /**
     * 设备参数配置
     */
    @Getter
    private final PileMachineProperties pileMachineProperties;

    private RabbitTemplate rabbitTemplate;

    private static final String DEFAULT_EXCHANGE = "";

    private static final String DEFAULT_ROUTING_KEY = "";


    /**
     * 下发消息
     *
     * @param object object
     */
    public void send(Object object){
        this.send(pileMachineProperties.getMessageExchange(), DEFAULT_ROUTING_KEY, object);
    }


    /**
     * 发送消息
     *
     * @param exchange exchange
     * @param routingKey routingKey
     * @param object   object
     */
    public void send(final String exchange, final String routingKey, Object object) {
        MessageProperties messageProperties = new MessageProperties();
        Snowflake snowflake = IdUtil.getSnowflake();
        messageProperties.setMessageId(snowflake.nextIdStr());
        Message message = new Message(JSONParser.jsonString(object).getBytes(), messageProperties);
        rabbitTemplate.send(exchange, routingKey, message);
    }


    @Override
    public void afterPropertiesSet() {
        rabbitTemplate = new RabbitTemplate(this.connectionFactory);
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    /**
     * @param correlationData correlationData
     * @param ack             ack
     * @param cause           case
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        log.debug("消息唯一标识: {}，确认状态: {}，造成原因: {}", correlationData, ack, cause);
    }


    /**
     * @param returnedMessage returnedMessage
     */
    @Override
    public void returnedMessage(@NonNull ReturnedMessage returnedMessage) {
        log.info("returnedMessage:{}", returnedMessage);
    }

}
