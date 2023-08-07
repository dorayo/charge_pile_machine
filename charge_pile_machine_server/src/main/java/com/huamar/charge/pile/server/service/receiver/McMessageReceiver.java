//package com.huamar.charge.pile.server.service.receiver;
//
//
//import com.google.common.collect.ImmutableMap;
//import com.rabbitmq.client.Channel;
//import lombok.RequiredArgsConstructor;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.core.AcknowledgeMode;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
//import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
//import org.springframework.beans.factory.DisposableBean;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.dao.DuplicateKeyException;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.Map;
//
//
///**
// * 推房任务队列
// *
// * @author TiAmo
// * @version 1.0.0
// * @since 1.0.0 create 2020/9/2
// **/
//@Component
//@RequiredArgsConstructor
//public class PlatformPushHouseReceiverV2 implements ChannelAwareMessageListener, DisposableBean {
//
//    private final transient Logger logger = LoggerFactory.getLogger(PlatformPushHouseReceiverV2.class);
//
//    private SimpleMessageListenerContainer container;
//
//    @SuppressWarnings("DuplicatedCode")
//    @Bean
//    public SimpleMessageListenerContainer taskQueueMessageContainerV2(ConnectionFactory connectionFactory) {
//        logger.info("PlatformPushHouseReceiverV2 init start...");
//        container = new SimpleMessageListenerContainer(connectionFactory);
//        container.setConnectionFactory(connectionFactory);
//        container.setQueueNames("charge_pile_machine");
//        container.setExposeListenerChannel(true);
//        container.setPrefetchCount(1);
//        container.setConcurrentConsumers(5);
//        container.setMaxConcurrentConsumers(5);
//        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
//        container.setMessageListener(this);
//        return container;
//    }
//
//    @Override
//    public void onMessage(Message message, Channel channel) {
//        String lock = null;
//        Boolean ifAbsent = false;
//        PlatformPushHouseTask task = null;
//        try {
//            /*
//            锁定当前用户的任务，同一时刻，一个用户只能并行一条房源任务
//            * */
//            task = JSONParser.parseObject(message.getBody(), PlatformPushHouseTask.class);
//
//            String messageId = message.getMessageProperties().getMessageId();
//
//            MessageIdPo messageIdPo = new MessageIdPo();
//            messageIdPo.setId(messageId);
//            messageIdService.save(messageIdPo);
//
//            Thread.currentThread().setName(task.getId());
//            lock = String.format("%s%s", CacheContent.PUSH_HOUSE_ACTION_TASK_LOCK, task.getOriginId());
//            logger.info("onMessage task:{}", JSONParser.toJSON(task));
//
//            ifAbsent = redisCacheClient.setStringIfAbsent(lock, lock, CacheContent.PUSH_HOUSE_ACTION_TASK_LOCK_EXP);
//            if (ifAbsent) {
//                PlatformPushHouseTaskDao houseTaskDao = SpringUtils.getBean(PlatformPushHouseTaskDao.class);
//                PlatformPushHouseTask updatePo = new PlatformPushHouseTask();
//                updatePo.setStatus(TaskStatus.T.getCode());
//                task.setStatus(TaskStatus.T.getCode());
//                UpdateWrapper<PlatformPushHouseTask> updateWrapper = new UpdateWrapper<>();
//                updateWrapper.eq("id", task.getId());
//                updateWrapper.eq(!TaskSourceCode.ERFCK.getCode().equals(task.getSourceCode()), "status", TaskStatus.P.getCode());
//                updateWrapper.eq(TaskSourceCode.ERFCK.getCode().equals(task.getSourceCode()), "status", TaskStatus.W.getCode());
//                int update = houseTaskDao.update(updatePo, updateWrapper);
//                if (update != 1) {
//                    return;
//                }
//                @SuppressWarnings("unchecked")
//                PlatformPushHouseAction ajkPushRentHouseAction = (PlatformPushHouseAction) SpringUtils.getBean(TaskSourceCode.getExec(task.getSourceCode()));
//                ajkPushRentHouseAction.exec(task);
//                logger.info("task 消费成功 taskId:{} ", task.getId());
//            } else {
//                logger.info("task lock fial lockId:{} is run", task.getOriginId());
//            }
//        } catch (DuplicateKeyException e) {
//            if (null == task) {
//                logger.info("task is null");
//                return;
//            }
//            logger.info("已经消费了,taskId: {}", task.getId());
//        } catch (Exception e) {
//            logger.error("PlatformPushHouseReceiverV2 error e ==> ", e);
//            Map<String, Object> map = ImmutableMap.of("serviceCode", "PlatformPushHouseReceiverV2", "body", message.getBody());
//            errorLogMqProduce.send(map);
//        } finally {
//            try {
//                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//            } catch (IOException e) {
//                logger.error("basicAck error ==>", e);
//            }
//            if (ifAbsent && StringUtils.isNotBlank(lock)) {
//                redisCacheClient.del(lock);
//            }
//        }
//    }
//
//    @Override
//    public void destroy() {
//        container.destroy();
//    }
//}
