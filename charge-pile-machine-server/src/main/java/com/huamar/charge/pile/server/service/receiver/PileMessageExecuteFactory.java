package com.huamar.charge.pile.server.service.receiver;

import com.huamar.charge.pile.enums.MessageCodeEnum;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * 设备消息执行工厂
 * tag_date: 2023.08.07
 *
 * @author TiAmo(13721682347 @ 163.com)
 **/
@Component
public class PileMessageExecuteFactory implements InitializingBean, ApplicationContextAware {

    private static final Map<MessageCodeEnum, PileMessageExecute> EXECUTE_MAP = new EnumMap<>(MessageCodeEnum.class);

    private ApplicationContext applicationContext;

    /**
     * 获取执行策略
     *
     * @param eventEnum eventEnum
     * @return JobTicketFlowEventExec
     */
    public PileMessageExecute getExecute(MessageCodeEnum eventEnum) {
        return EXECUTE_MAP.get(eventEnum);
    }


    @Override
    public void afterPropertiesSet() {
       applicationContext.getBeansOfType(PileMessageExecute.class)
               .values().forEach(exec -> EXECUTE_MAP.put(exec.getCode(), exec));
    }


    /**
     * @param applicationContext applicationContext
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
