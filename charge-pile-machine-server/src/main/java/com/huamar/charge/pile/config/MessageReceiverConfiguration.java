package com.huamar.charge.pile.config;

import com.huamar.charge.pile.server.service.receiver.PileMessageExecuteFactory;
import com.huamar.charge.pile.server.service.receiver.PileMessageReceiver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 设备端消费者
 * date: 2023.08.07
 *
 * @author TiAmo(13721682347 @ 163.com)
 **/
@Slf4j
@Configuration
@RequiredArgsConstructor
@Import(PileMachineAutoConfiguration.class)
public class MessageReceiverConfiguration implements DisposableBean, ApplicationListener<ApplicationReadyEvent> {

    /**
     * 监听容器集合
     */
    private final Map<String, SimpleMessageListenerContainer> containerMap = new HashMap<>();

    /**
     * 设备端的配置
     */
    private final PileMachineProperties pileMachineProperties;

    private final PileMessageExecuteFactory pileMessageExecuteFactory;

    private final RedissonClient redissonClient;

    private final ConnectionFactory connectionFactory;


    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        ConfigurableListableBeanFactory beanFactory = event.getApplicationContext().getBeanFactory();
        SimpleMessageListenerContainer pileMessageListenerContainer = getListenerContainer();
        containerMap.put("pileMessageListenerContainer", pileMessageListenerContainer);
        containerMap.forEach((k,v) -> {
            log.info("{} init...", k);
            beanFactory.registerSingleton(k, v);
            if(pileMachineProperties.getEnableConsume()){
                v.start();
            }
        });
    }

    /**
     * 构建消费者
     * @return SimpleMessageListenerContainer
     */
    private SimpleMessageListenerContainer getListenerContainer() {
        SimpleMessageListenerContainer pileMessageListenerContainer = new SimpleMessageListenerContainer(this.connectionFactory);
        pileMessageListenerContainer.setQueueNames(pileMachineProperties.getPileControlQueue());
        pileMessageListenerContainer.setExposeListenerChannel(true);
        pileMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        pileMessageListenerContainer.setMessageListener(new PileMessageReceiver(pileMessageExecuteFactory, redissonClient));
        pileMessageListenerContainer.setPrefetchCount(1);
        pileMessageListenerContainer.setConcurrentConsumers(1);
        pileMessageListenerContainer.setMaxConcurrentConsumers(1);
        return pileMessageListenerContainer;
    }

    @Override
    public void destroy() {
        containerMap.forEach((k,v) -> v.destroy());
    }
}
