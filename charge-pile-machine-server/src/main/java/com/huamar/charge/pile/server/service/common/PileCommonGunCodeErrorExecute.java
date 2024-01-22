package com.huamar.charge.pile.server.service.common;

import com.huamar.charge.pile.entity.dto.McCommonReq;
import com.huamar.charge.pile.enums.PileCommonResultEnum;
import com.huamar.charge.common.util.JSONParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通用应答结果处理
 * 远程控制应答-枪号错误 (0x0007)
 * 2023-08
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
public class PileCommonGunCodeErrorExecute implements McCommonResultExecute<McCommonReq> {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public PileCommonResultEnum getCode() {
        return PileCommonResultEnum.DEGREE_GUN_CODE_ERROR;
    }

    /**
     * 执行方法
     *
     * @param command command
     */
    @Override
    public void execute(McCommonReq command) {
        log.info("通用应答-执行结果 result:{}:{} ==> command：{}", PileCommonResultEnum.SUCCESS.name(), getCode().getDesc(),JSONParser.jsonString(command));
    }
}
