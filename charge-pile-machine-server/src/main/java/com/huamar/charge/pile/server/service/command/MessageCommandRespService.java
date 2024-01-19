package com.huamar.charge.pile.server.service.command;

import com.huamar.charge.pile.entity.dto.command.MessageCommonRespDTO;

/**
 * 消息控制命令响应结果
 * 2023/08
 *
 * @author TiAmo(13721682347@163.com)
 */
public interface MessageCommandRespService {

    /**
     * 更新命令响应信息
     * @param commonRespDTO commonRespDTO
     */
    void put(MessageCommonRespDTO commonRespDTO);

    /**
     * 获取命令流水号
     *
     * @param idCode idCode
     * @param msgNum msgNum
     * @return MessageCommonRespDTO
     */
    MessageCommonRespDTO get(String idCode, Short msgNum);

    /**
     * 发送远程命令响应
     * @param commonRespDTO commonRespDTO
     */
    void sendCommonResp(MessageCommonRespDTO commonRespDTO);
}
