package com.huamar.charge.pile.server.service;

import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.pile.server.service.handler.MachineMessageHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * 业务执行工厂
 * date 2023/06/11
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
public class MachineMessageFactory implements InitializingBean, ApplicationContextAware {

    private static final Map<ProtocolCodeEnum, MachineMessageHandler<DataPacket>> HANDLER_MAP = new EnumMap<>(ProtocolCodeEnum.class);

    private ApplicationContext applicationContext;

    /**
     * 获取执行策略
     *
     * @param handlerEnum handlerEnum
     * @return JobTicketFlowEventExec
     */
    public MachineMessageHandler<DataPacket> getHandler(ProtocolCodeEnum handlerEnum) {
        return HANDLER_MAP.get(handlerEnum);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() {
        applicationContext
                .getBeansOfType(MachineMessageHandler.class).values()
                .forEach(exec -> HANDLER_MAP.put(exec.getCode(), exec));
    }


    /**
     * @param applicationContext applicationContext
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
