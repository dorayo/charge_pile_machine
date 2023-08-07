package com.huamar.charge.pile.protocol;

import org.tio.core.exception.TioDecodeException;
import org.tio.core.intf.Packet;

import java.nio.ByteBuffer;

/**
 * 协议转换类
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
public interface ProtocolCodec {


    Class<?> getClazz();

    /**
     * 协议编码
     *
     * @author TiAmo(13721682347 @ 163.com)
     */
    ByteBuffer encode(BasePacket packet) throws TioDecodeException;

    /**
     * 协议解码
     *
     * @author TiAmo(13721682347 @ 163.com)
     */
    BasePacket decode(ByteBuffer buffer) throws TioDecodeException;


}
