package com.huamar.charge.pile;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.huamar.charge.pile.entity.dto.platform.PileChargeControlDTO;
import com.huamar.charge.pile.common.constant.QueueConstant;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.ChargPriceDTO;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.util.JSONParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * <p>
 * 通用应答结果处理执行工厂
 * Date: 2023/07/24
 * </p>
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ServerApplication.class)
public class ChargeControllerTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void test() {

        RAtomicLong atomicLong = redissonClient.getAtomicLong("atomicLong:1000");
        atomicLong.set(5);
        long value = atomicLong.get();
        log.info("value:{}", value);

        CyclicBarrier cyclicBarrier = new CyclicBarrier(1005);
        Executor executor = Executors.newFixedThreadPool(1005);
        for (int i = 0; i < 1000; i++) {
            String name = "id:" + i;
            executor.execute(() -> {
                try {
                    RAtomicLong atomicLong1 = redissonClient.getAtomicLong("atomicLong:1000");
                    log.info("线程就位：{}", name);
                    cyclicBarrier.await();
                    long andGet = atomicLong1.decrementAndGet();
                    if(andGet >= 0){
                        log.info("抽奖成功 name:{}, getValue:{}", name, andGet);
                    }
                } catch (Exception e) {
                    log.error("e");
                }
            });
        }

        for (int i = 0; i < 5; i++) {
            String name = "id:" + i;
            executor.execute(() -> {
                try {
                    RAtomicLong rAtomicLong = redissonClient.getAtomicLong("atomicLong:1000");
                    log.info("线程就位：{}", name);
                    cyclicBarrier.await();
                    long andGet = rAtomicLong.incrementAndGet();
                    log.info("添加 name:{}, getValue:{}", name, andGet);
                } catch (Exception e) {
                    log.error("e");
                }
            });
        }
    }


    @Test
    public void sendMessage() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(this.connectionFactory);
        ChargPriceDTO dto = new ChargPriceDTO();
        dto.setSortNum(0);
        dto.setStartTime("00:00:00");
        dto.setEndTime("22:30:00");
        dto.setCharge(new BigDecimal("0.5"));
        dto.setServiceCharge(new BigDecimal("0.5"));

        List<ChargPriceDTO> list = new ArrayList<>();
        list.add(dto);
        MessageData<List<ChargPriceDTO>> messageData = new MessageData<>(MessageCodeEnum.ELECTRICITY_PRICE, list);
        messageData.setBusinessId("471000220714302005");

        MessageProperties messageProperties = new MessageProperties();
        Snowflake snowflake = IdUtil.getSnowflake();
        messageProperties.setMessageId(snowflake.nextIdStr());
        rabbitTemplate.send(QueueConstant.PILE_COMMON_QUEUE, new Message(JSONParser.jsonStr(messageData).getBytes(), messageProperties));
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
        rabbitTemplate.send(QueueConstant.PILE_COMMON_QUEUE, new Message(JSONParser.jsonStr(messageData).getBytes(), messageProperties));



        String string = IdUtil.fastUUID();
        log.info(string);

        byte toByte = Convert.intToByte(200);
        log.info("toByte:{}", toByte);
    }

}
