package com.huamar.charge.pile.server.service.factory;

import com.huamar.charge.pile.enums.McDataUploadEnum;
import com.huamar.charge.pile.server.service.upload.McDataUploadExecute;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * 数据汇报业务执行工厂
 * date 2023/06/11
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
public class McDataUploadFactory implements InitializingBean, ApplicationContextAware {

    private static final Map<McDataUploadEnum, McDataUploadExecute> HANDLER_MAP = new EnumMap<>(McDataUploadEnum.class);

    private ApplicationContext applicationContext;

    /**
     * 获取执行策略
     *
     * @param handlerEnum handlerEnum
     * @return JobTicketFlowEventExec
     */
    public McDataUploadExecute getExecute(McDataUploadEnum handlerEnum) {
        return HANDLER_MAP.get(handlerEnum);
    }

    @Override
    public void afterPropertiesSet() {
        applicationContext
                .getBeansOfType(McDataUploadExecute.class).values()
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
