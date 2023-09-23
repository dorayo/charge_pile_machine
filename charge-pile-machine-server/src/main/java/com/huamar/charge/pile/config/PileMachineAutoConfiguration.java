package com.huamar.charge.pile.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设配端配置文件
 * 2023/08/01
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({PileMachineProperties.class})
public class PileMachineAutoConfiguration implements InitializingBean {

    private final PileMachineProperties machineProperties;

    private final RabbitAdmin rabbitAdmin;

    public PileMachineAutoConfiguration(@Autowired(required = true) PileMachineProperties machineProperties, @Autowired(required = true) RabbitAdmin rabbitAdmin) {
        this.machineProperties = machineProperties;
        this.rabbitAdmin = rabbitAdmin;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, List<String>> exchangeQueue = new HashMap<>();
        exchangeQueue.put(machineProperties.getMessageExchange(), machineProperties.getFanoutExchangeQueues());
        exchangeQueue.forEach((k,v) -> {
            FanoutExchange exchange = new FanoutExchange(k);
            rabbitAdmin.declareExchange(exchange);
            log.info("创建交换机：{}", exchange);
            v.forEach(item -> {
                Queue queue = new Queue(item);
                String string = rabbitAdmin.declareQueue(queue);
                log.info("创建队列：{}, 结果：{}", queue, string);
                rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange));
            });
        });
        Queue queue = new Queue(machineProperties.getPileControlQueue());
        String string = rabbitAdmin.declareQueue(queue);
        log.info("创建队列：{}, 结果：{}", queue, string);
    }
}
