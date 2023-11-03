package com.huamar.charge.pile.server.service.factory.b;

import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.server.service.factory.MachinePacketFactory;
import com.huamar.charge.pile.server.service.handler.MachineBDataUploadHandler;
import com.huamar.charge.pile.server.service.handler.MachinePacketHandler;
import com.huamar.charge.pile.server.service.handler.b.MachineBAuthenticationHandler;
import com.huamar.charge.pile.server.service.handler.b.MachineBHeartbeatHandler;
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
 * @author TiAmo(13721682347 @ 163.com)
 */
@Component
public class MachineBPacketFactory extends MachinePacketFactory {

    private static final Map<ProtocolCodeEnum, MachinePacketHandler<DataPacket>> HANDLER_MAP = new EnumMap<>(ProtocolCodeEnum.class);

    private ApplicationContext applicationContext;

    /**
     * 获取执行策略
     *
     * @param handlerEnum handlerEnum
     * @return JobTicketFlowEventExec
     */
    public MachinePacketHandler<DataPacket> getHandler(ProtocolCodeEnum handlerEnum) {
        switch (handlerEnum) {
            case HEART_BEAT:
                return applicationContext.getBean(MachineBHeartbeatHandler.class);
            case AUTH:
                return applicationContext.getBean(MachineBAuthenticationHandler.class);
            case DATA_UPLOAD:
                return applicationContext.getBean(MachineBDataUploadHandler.class);
            default:
                return HANDLER_MAP.get(handlerEnum);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() {
        applicationContext
                .getBeansOfType(MachinePacketHandler.class).values()
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
