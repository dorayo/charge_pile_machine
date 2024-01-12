package com.huamar.charge.pile.server.service.common;

import com.huamar.charge.pile.entity.dto.McCommonReq;
import com.huamar.charge.pile.enums.PileCommonResultEnum;
import com.huamar.charge.common.util.JSONParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通用应答结果处理
 * 远程控制应答-电表故障
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
public class McEnergyMeterFailExecute implements McCommonResultExecute<McCommonReq> {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public PileCommonResultEnum getCode() {
        return PileCommonResultEnum.ELECTRIC_METER_FAULT;
    }

    /**
     * 执行方法
     *
     * @param command command
     */
    @Override
    public void execute(McCommonReq command) {
        log.info("通用应答结果处理 CommonResul:{}-{} start ==> command：{}", PileCommonResultEnum.SUCCESS.name(), getCode().getDesc(),JSONParser.jsonString(command));
    }
}
