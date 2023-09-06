package com.huamar.charge.pile.server.service.common;

import com.huamar.charge.pile.entity.dto.McCommonReq;
import com.huamar.charge.pile.enums.PileCommonResultEnum;
import com.huamar.charge.common.util.JSONParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通用应答结果处理
 * 命令处理成功
 * 2023-08
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
public class PileCommonUnknownErrorExecute implements McCommonResultExecute<McCommonReq> {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public PileCommonResultEnum getCode() {
        return PileCommonResultEnum.UNKNOWN;
    }

    /**
     * 执行方法
     *
     * @param command command
     */
    @Override
    public void execute(McCommonReq command) {
        String hexCodeStr = String.format("%04X", command.getMsgResult());
        log.info("通用应答结果处理-{} start ==> code:{}, command：{}", getCode().getDesc(), hexCodeStr, JSONParser.jsonString(command));
    }
}
