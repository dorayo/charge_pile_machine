package com.huamar.charge.pile.server.service;

import com.huamar.charge.pile.dto.parameter.McBaseParameterDTO;
import com.huamar.charge.pile.enums.McParameterEnum;
import com.huamar.charge.pile.server.service.parameter.McParameterExecute;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * 远程参数业务执行工厂
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
public class McParameterFactory implements InitializingBean, ApplicationContextAware {

    private static final Map<McParameterEnum, McParameterExecute<? extends McBaseParameterDTO>> EXECUTE_MAP = new EnumMap<>(McParameterEnum.class);

    private ApplicationContext applicationContext;

    /**
     * 获取执行策略
     *
     * @param eventEnum eventEnum
     * @return JobTicketFlowEventExec
     */
    public McParameterExecute<? extends McBaseParameterDTO> getExecute(McParameterEnum eventEnum) {
        return EXECUTE_MAP.get(eventEnum);
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void afterPropertiesSet() {
        Map<String, McParameterExecute> beans = applicationContext.getBeansOfType(McParameterExecute.class);
        beans.values().forEach(exec -> EXECUTE_MAP.put(exec.getCode(), exec));
    }


    /**
     * @param applicationContext applicationContext
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
