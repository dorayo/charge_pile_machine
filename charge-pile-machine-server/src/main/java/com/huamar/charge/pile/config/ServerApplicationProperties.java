package com.huamar.charge.pile.config;

import com.huamar.charge.pile.enums.NetSocketEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 服务端配置类
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Data
@ConfigurationProperties(prefix = "machine.server")
public class ServerApplicationProperties {
    /**
     * 服务器地址
     */
    private String host = "127.0.0.1";

    /**
     * 监听端口
     */
    private Integer port = 8886;


    /**
     * 监听端口B
     */
    private Integer portB = 8888;
    /**
     * 监听端口C
     */
    private Integer portC = 8889;

    /**
     * 备用端口
     */
    private Integer portSalve = 8887;

    /**
     * 心跳超时时间
     */
    private Duration timeout = Duration.ofSeconds(60);

    /**
     * boss
     */
    private int boss = 10;


    /**
     * worker
     */
    private int worker = 10;

    /**
     * 通讯端Socket模型
     */
    private NetSocketEnum netSocketModel = NetSocketEnum.NETTY;
}
