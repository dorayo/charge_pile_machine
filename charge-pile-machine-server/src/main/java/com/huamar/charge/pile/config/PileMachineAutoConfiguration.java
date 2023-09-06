package com.huamar.charge.pile.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 设配端配置文件
 * 2023/08/01
 *
 * @author TiAmo(13721682347@163.com)
 */
@Configuration
@EnableConfigurationProperties({PileMachineProperties.class})
@ComponentScan(basePackageClasses = PileMachineAutoConfiguration.class)
public class PileMachineAutoConfiguration {

}
