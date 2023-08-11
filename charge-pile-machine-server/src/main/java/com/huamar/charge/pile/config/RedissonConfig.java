package com.huamar.charge.pile.config;

import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BaseDTO
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@Configuration
public class RedissonConfig {


    /**
     * redisson config 后置配置
     * @return RedissonAutoConfigurationCustomizer
     */
    @Bean
    public RedissonAutoConfigurationCustomizer configurationCustomizer(){
        return config -> {
            Codec codec = new JsonJacksonCodec();
            config.setCodec(codec);
        };
    }

}
