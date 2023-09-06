package com.huamar.charge.pile.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 设配端配置文件
 * 2023/08/01
 *
 * @author TiAmo(13721682347@163.com)
 */
@ConfigurationProperties(
        prefix = "pile.machine"
)
@Data
public class PileMachineProperties {

    /**
     * App控制设备端下发消息队列
     */
    private String pileControlQueue;

    /**
     * 设备端推送消息队列名字
     */
    private String pileMessageQueue;

    /**
     * 消费队列线程开关
     */
    private Boolean enableConsume = Boolean.TRUE;

}
