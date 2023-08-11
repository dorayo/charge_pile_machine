package com.huamar.charge.pile.server;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

/**
 * 服务端配置类
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Configuration
@ConfigurationProperties(prefix = "machine.server")
@Data
public class MachineProperties {
    /**
     * 服务器地址
     */
    private String host;

    /**
     * 监听端口
     */
    private int port = 8886;

    /**
     * 心跳超时时间
     */
    private Duration timeout = Duration.ofSeconds(60);
}
