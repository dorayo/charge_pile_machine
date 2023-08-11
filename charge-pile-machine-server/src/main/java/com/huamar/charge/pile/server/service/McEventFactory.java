package com.huamar.charge.pile.server.service;

import com.huamar.charge.pile.enums.McEventEnum;
import com.huamar.charge.pile.server.service.event.McEventExecute;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * 事件汇报业务执行工厂
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
public class McEventFactory implements InitializingBean, ApplicationContextAware {

    private static final Map<McEventEnum, McEventExecute> EXECUTE_MAP = new EnumMap<>(McEventEnum.class);

    private ApplicationContext applicationContext;

    /**
     * 获取执行策略
     *
     * @param eventEnum eventEnum
     * @return JobTicketFlowEventExec
     */
    public McEventExecute getExecute(McEventEnum eventEnum) {
        return EXECUTE_MAP.get(eventEnum);
    }

    @Override
    public void afterPropertiesSet() {
        applicationContext
                .getBeansOfType(McEventExecute.class).values()
                .forEach(exec -> EXECUTE_MAP.put(exec.getCode(), exec));
    }


    /**
     * @param applicationContext applicationContext
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
