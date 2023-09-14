package com.huamar.charge.pile.server.session;

import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.net.core.SessionChannel;

/**
 * 设备业务上下文 接口
 * 2023/08
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
public interface MachineSessionContext {


    /**
     * 消息应答
     *
     * @param packet         packet
     * @param sessionChannel sessionChannel
     */
    boolean writePacket(DataPacket packet, SessionChannel sessionChannel);


    /**
     * 发送消息
     *
     * @param packet packet
     * @return boolean
     */
    boolean writePacket(DataPacket packet);

}
