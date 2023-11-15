package com.huamar.charge.pile.server.service.factory;

import com.huamar.charge.pile.entity.dto.parameter.PileBaseParameterDTO;
import com.huamar.charge.pile.enums.McParameterEnum;
import com.huamar.charge.pile.server.service.parameter.PileParameterExecute;
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

    private static final Map<McParameterEnum, PileParameterExecute<PileBaseParameterDTO>> EXECUTE_MAP = new EnumMap<>(McParameterEnum.class);

    private ApplicationContext applicationContext;

    /**
     * 获取执行策略
     *
     * @param eventEnum eventEnum
     * @return JobTicketFlowEventExec
     */
    public PileParameterExecute<PileBaseParameterDTO> getExecute(McParameterEnum eventEnum) {
        return EXECUTE_MAP.get(eventEnum);
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void afterPropertiesSet() {
        Map<String, PileParameterExecute> beans = applicationContext.getBeansOfType(PileParameterExecute.class);
        beans.values().forEach(exec -> {
            if(EXECUTE_MAP.containsKey(exec.getCode())){
                throw new RuntimeException("不允许覆盖相同执行器" + exec.getCode());
            }
            EXECUTE_MAP.put(exec.getCode(), exec);
        });
    }


    /**
     * @param applicationContext applicationContext
     */
    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
