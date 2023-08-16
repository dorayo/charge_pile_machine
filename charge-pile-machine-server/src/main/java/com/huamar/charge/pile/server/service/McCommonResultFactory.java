package com.huamar.charge.pile.server.service;

import com.huamar.charge.pile.entity.dto.McCommonReq;
import com.huamar.charge.pile.enums.PileCommonResultEnum;
import com.huamar.charge.pile.server.service.common.McCommonResultExecute;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * 通用应答结果处理执行工厂
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
public class McCommonResultFactory implements InitializingBean, ApplicationContextAware {

    private static final Map<PileCommonResultEnum, McCommonResultExecute<McCommonReq>> EXECUTE_MAP = new EnumMap<>(PileCommonResultEnum.class);

    private ApplicationContext applicationContext;

    /**
     * 获取执行策略
     *
     * @param eventEnum eventEnum
     * @return JobTicketFlowEventExec
     */
    public McCommonResultExecute<McCommonReq> getExecute(PileCommonResultEnum eventEnum) {
        return EXECUTE_MAP.get(eventEnum);
    }


    @SuppressWarnings("unchecked")
    @Override
    public void afterPropertiesSet() {
       applicationContext.getBeansOfType(McCommonResultExecute.class)
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
