package com.huamar.charge.pile.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * 设配端配置文件
 * 2023/08/01
 *
 * @author TiAmo(13721682347@163.com)
 */
@ConfigurationProperties(
        prefix = "machine.pile.mq"
)
@Data
public class PileMachineProperties {


    /**
     * 消费队列线程开关
     */
    private Boolean enableConsume = Boolean.TRUE;

    /**
     * App控制设备端下发消息队列
     */
    private String pileControlQueue;


    /**
     * 设备端发送交换机
     */
    private String messageExchange;

    /**
     * 扇形交换机队列绑定 key 交换机 value 队里集合
     */
    private List<String> fanoutExchangeQueues;

    /**
     * 设备故障处理队列
     */
    private String pileFaultQueue;

    /**
     * 二维码下发
     */
    private String qrCodeUrl;
}
