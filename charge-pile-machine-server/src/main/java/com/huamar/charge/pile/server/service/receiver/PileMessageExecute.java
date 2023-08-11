package com.huamar.charge.pile.server.service.receiver;


import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.enums.MessageCodeEnum;

/**
 * 消息执行器
 *  DATE: 2023.08.07
 * @author TiAmo(13721682347@163.com)
 **/
public interface PileMessageExecute {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    MessageCodeEnum getCode();


    /**
     * 执行方法
     *
     * @param body body
     */
    void execute(MessageData<String> body);
}
