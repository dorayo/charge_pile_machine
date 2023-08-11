package com.huamar.charge.pile.server.service.common;

import com.huamar.charge.pile.entity.dto.McCommonReq;
import com.huamar.charge.pile.enums.McCommonResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通用应答结果处理
 * 远程控制应答-枪状态错误
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
public class McCommonGunStatusFailExecute implements McCommonResultExecute<McCommonReq> {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McCommonResultEnum getCode() {
        return McCommonResultEnum.GUN_STATE_ERROR;
    }

    /**
     * 执行方法
     *
     * @param command command
     */
    @Override
    public void execute(McCommonReq command) {
        log.info("远程控制应答-枪状态错误 start ==>");
    }
}
