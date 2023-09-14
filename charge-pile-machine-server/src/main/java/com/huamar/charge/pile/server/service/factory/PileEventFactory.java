package com.huamar.charge.pile.server.service.factory;

import com.huamar.charge.pile.enums.PileEventEnum;
import com.huamar.charge.pile.server.service.event.PileEventExecute;
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
public class PileEventFactory implements InitializingBean, ApplicationContextAware {

    private static final Map<PileEventEnum, PileEventExecute> EXECUTE_MAP = new EnumMap<>(PileEventEnum.class);

    private ApplicationContext applicationContext;

    /**
     * 获取执行策略
     *
     * @param eventEnum eventEnum
     * @return JobTicketFlowEventExec
     */
    public PileEventExecute getExecute(PileEventEnum eventEnum) {
        return EXECUTE_MAP.get(eventEnum);
    }

    @Override
    public void afterPropertiesSet() {
        applicationContext
                .getBeansOfType(PileEventExecute.class).values()
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
