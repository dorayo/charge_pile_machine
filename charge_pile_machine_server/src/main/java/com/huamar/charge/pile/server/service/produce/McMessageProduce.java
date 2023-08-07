package com.huamar.charge.pile.server.service.produce;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.huamar.charge.pile.util.JSONParser;
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
 * 设备消息生产者
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class McMessageProduce implements InitializingBean, RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    private final ConnectionFactory connectionFactory;

    private RabbitTemplate rabbitTemplate;


    /**
     * 发送消息
     * @param routingKey routingKey
     * @param object object
     */
    public void send(String routingKey, Object object) {
        MessageProperties messageProperties = new MessageProperties();
        Snowflake snowflake = IdUtil.getSnowflake();
        messageProperties.setMessageId(snowflake.nextIdStr());
        rabbitTemplate.send(routingKey, new Message(JSONParser.jsonStr(object).getBytes(), messageProperties));
    }


    @Override
    public void afterPropertiesSet() {
        rabbitTemplate = new RabbitTemplate(this.connectionFactory);
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    /**
     * @param correlationData correlationData
     * @param ack ack
     * @param cause case
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        log.info("消息唯一标识: {}", correlationData);
        log.info("确认状态: {}", ack);
        log.info("造成原因: {}", cause);
    }


    /**
     * @param returnedMessage returnedMessage
     */
    @Override
    public void returnedMessage(@NonNull ReturnedMessage returnedMessage) {
        log.info("returnedMessage:{}", returnedMessage);
    }
}
