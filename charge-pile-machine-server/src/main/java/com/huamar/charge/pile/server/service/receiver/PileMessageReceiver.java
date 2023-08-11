package com.huamar.charge.pile.server.service.receiver;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.huamar.charge.pile.common.constant.QueueConstant;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * 设备端消费者
 *  DATE: 2023.08.07
 * @author TiAmo(13721682347@163.com)
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class PileMessageReceiver implements ChannelAwareMessageListener, DisposableBean {

    /**
     * 消息处理工厂
     */
    private final PileMessageExecuteFactory pileMessageExecuteFactory;

    private final RedissonClient redissonClient;

    private SimpleMessageListenerContainer container;

    @SuppressWarnings("DuplicatedCode")
    @Bean
    public SimpleMessageListenerContainer taskQueueMessageContainerV2(ConnectionFactory connectionFactory) {
        log.info("PileMessageReceiver init start...");
        container = new SimpleMessageListenerContainer(connectionFactory);
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(QueueConstant.PILE_COMMON_QUEUE);
        container.setExposeListenerChannel(true);
        container.setPrefetchCount(1);
        container.setConcurrentConsumers(5);
        container.setMaxConcurrentConsumers(5);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(this);
        return container;
    }


    /**
     * 消费者
     * lock 分布式锁控制消息幂等性
     * <p>
     * @param message message
     * @param channel channel
     */
    @Override
    public void onMessage(Message message, Channel channel) {
        @SuppressWarnings("UnusedAssignment")
        boolean lock = false;
        String lockKey = "";
        RLock clientLock = null;
        try {
            lockKey = MessageFormatter.format("lock:{}:{}",QueueConstant.PILE_COMMON_QUEUE, message.getMessageProperties().getMessageId()).toString();
            clientLock = redissonClient.getLock(lockKey);
            lock = clientLock.tryLock(QueueConstant.LOCK_TIMEOUT.toMillis(), TimeUnit.MICROSECONDS);
            MessageData<String> messageData = JSONObject.parseObject(new String(message.getBody()), new TypeReference<MessageData<String>>(){});
            if (lock) {
                MessageCodeEnum messageCodeEnum = MessageCodeEnum.getByCode(messageData.getBusinessCode());
                Assert.notNull(messageCodeEnum, "messageCodeEnum is null");
                PileMessageExecute execute = pileMessageExecuteFactory.getExecute(messageCodeEnum);
                Assert.notNull(execute, "execute is null");
                execute.execute(messageData);
                log.info("message lockKey:{} ", lockKey);
            } else {
                log.info("message lock fail lockKey:{} is run", lockKey);
            }
        } catch (DuplicateKeyException e) {
            log.info("message 已经消费了,lockKey: {}", lockKey);
        } catch (Exception e) {
            log.error("error e:{} ==> ", e.getMessage(), e);
        } finally {
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e) {
                log.error("basicAck error e:{} ==> ", e.getMessage(), e);
            }
            if (Objects.nonNull(clientLock) && StringUtils.isNotBlank(lockKey)) {
                clientLock.unlock();
            }
        }
    }

    @Override
    public void destroy() {
        container.destroy();
    }
}
