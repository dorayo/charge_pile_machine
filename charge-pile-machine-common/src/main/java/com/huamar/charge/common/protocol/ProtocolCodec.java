package com.huamar.charge.common.protocol;

import com.huamar.charge.common.exception.ProtocolCodecException;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * 协议转换类
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
public interface ProtocolCodec {

    /**
     * TiAmo
     *
     * @author TiAmo(13721682347 @ 163.com)
     */
    Class<?> getClazz();

    /**
     * 协议编码
     *
     * @author TiAmo(13721682347 @ 163.com)
     */
    ByteBuffer encode(BasePacket packet);

    /**
     * 协议解码
     *
     * @author TiAmo(13721682347 @ 163.com)
     */
    BasePacket decode(ByteBuffer buffer) throws ProtocolCodecException;


    /**
     * 编码
     *
     * @param packet packet
     * @param byteBuf byteBuf
     */
    boolean encode(BasePacket packet, ByteBuf byteBuf);

    /**
     * 协议解码
     *
     * @author TiAmo(13721682347 @ 163.com)
     */
    BasePacket decode(ByteBuf byteBuf) throws ProtocolCodecException;

}
