package com.huamar.charge.pile.server.service.handler;

import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.entity.dto.BaseReqDTO;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;

/**
 * 业务执行工厂
 * date 2023/06/11
 *
 * @author TiAmo(13721682347@163.com)
 */
public interface MachinePacketHandler<T> {


    /**
     * 协议编码
     * @return ProtocolCodeEnum
     */
    ProtocolCodeEnum getCode();

    /**
     * 执行器
     * @param packet packet
     * @param sessionChannel sessionChannel
     */
    void handler(T packet, SessionChannel sessionChannel);

    /**
     * 读取参数
     * @param packet packet
     * @return McBaseParameterDTO
     */
    default BaseReqDTO reader(DataPacket packet){
        return null;
    }
}
