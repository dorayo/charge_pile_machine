package com.huamar.charge.pile.server.service.handler;

import com.huamar.charge.pile.entity.dto.BaseReqDTO;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.protocol.DataPacket;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;

/**
 * 业务执行工厂
 * date 2023/06/11
 *
 * @author TiAmo(13721682347@163.com)
 */
public interface MachineMessageHandler<T extends Packet> {


    /**
     * 协议编码
     * @return ProtocolCodeEnum
     */
    ProtocolCodeEnum getCode();

    /**
     * 执行器
     * @param packet packet
     * @param channelContext channelContext
     */
    void handler(T packet, ChannelContext channelContext);

    /**
     * 读取参数
     * @param packet packet
     * @return McBaseParameterDTO
     */
    default BaseReqDTO reader(DataPacket packet){
        return null;
    }
}
