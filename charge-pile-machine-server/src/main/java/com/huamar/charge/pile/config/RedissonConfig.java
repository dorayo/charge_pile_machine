package com.huamar.charge.pile.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RedissonConfig
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@Configuration
public class RedissonConfig {


    /**
     * redisson config 后置配置
     *
     * @return RedissonAutoConfigurationCustomizer
     */
    @Bean
    public RedissonAutoConfigurationCustomizer configurationCustomizer(@Autowired ObjectMapper objectMapper){
        return config -> {
            Codec codec = new JsonJacksonCodec(objectMapper);
            config.setCodec(codec);
        };
    }

}
