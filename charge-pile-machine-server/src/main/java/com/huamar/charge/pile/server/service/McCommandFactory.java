package com.huamar.charge.pile.server.service;

import com.huamar.charge.pile.entity.dto.command.McBaseCommandDTO;
import com.huamar.charge.pile.enums.McCommandEnum;
import com.huamar.charge.pile.server.service.command.McCommandExecute;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * 远程指令业务执行工厂
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
public class McCommandFactory implements InitializingBean, ApplicationContextAware {

    private static final Map<McCommandEnum, McCommandExecute<McBaseCommandDTO>> EXECUTE_MAP = new EnumMap<>(McCommandEnum.class);

    private ApplicationContext applicationContext;

    /**
     * 获取执行策略
     *
     * @param eventEnum eventEnum
     * @return JobTicketFlowEventExec
     */
    public McCommandExecute<McBaseCommandDTO> getExecute(McCommandEnum eventEnum) {
        return EXECUTE_MAP.get(eventEnum);
    }


    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() {
       applicationContext.getBeansOfType(McCommandExecute.class)
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
