package com.huamar.charge.pile.server.service.receiver;


import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.Assert;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;


/**
 * 设备端消费者
 * date: 2023.08.07
 *
 * @author TiAmo(13721682347 @ 163.com)
 **/
@Slf4j
@RequiredArgsConstructor
public class PileMessageReceiver implements ChannelAwareMessageListener {

    /**
     * 消息处理工厂
     */
    private final PileMessageExecuteFactory pileMessageExecuteFactory;

    /**
     * redisson client
     */
    private final RedissonClient redissonClient;


    /**
     * 消费者
     * lock 分布式锁控制消息幂等性
     * <p>
     *
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
            Thread.currentThread().setName(IdUtil.getSnowflakeNextIdStr());
            MessageProperties properties = message.getMessageProperties();
            String idCode = properties.getHeader(ConstEnum.ID_CODE.getCode());
            MDC.put(ConstEnum.ID_CODE.getCode(), idCode);
        }catch (Exception ignored){

        }

        try {
            lockKey = MessageFormatter
                    .format(
                            "charge:lock:{}:{}",
                            "mq",
                            message.getMessageProperties().getMessageId()
                    )
                    .getMessage();
            clientLock = redissonClient.getLock(lockKey);
            lock = clientLock.tryLock(Duration.ofSeconds(15).toMillis(), TimeUnit.MICROSECONDS);
            MessageData<String> messageData = JSONObject.parseObject(new String(message.getBody()), new TypeReference<MessageData<String>>() {
            });
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
        } catch (IllegalArgumentException e) {
            StringJoiner joiner = new StringJoiner(StringPool.COMMA, StringPool.EMPTY, StringPool.EMPTY);
            StackTraceElement traceElement = e.getStackTrace()[0];
            joiner.add("methName:").add(traceElement.getMethodName());
            joiner.add("line:").add(String.valueOf(traceElement.getLineNumber()));
            joiner.add("message:").add(e.getMessage());
            log.error("error e:{} ==> ", joiner);
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
            MDC.clear();
        }
    }
}
