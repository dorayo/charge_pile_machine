package com.huamar.charge.pile.server.service.common;

import com.huamar.charge.pile.enums.McCommonResultEnum;

/**
 * 通用应答结果处理
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public interface McCommonResultExecute<T> {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    McCommonResultEnum getCode();


    /**
     * 执行方法
     *
     * @param command command
     */
    void execute(T command);

}
