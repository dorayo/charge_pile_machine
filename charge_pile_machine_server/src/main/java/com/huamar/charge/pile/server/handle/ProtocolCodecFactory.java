package com.huamar.charge.pile.server.handle;

import com.huamar.charge.pile.protocol.DataPacket;
import com.huamar.charge.pile.protocol.ProtocolCodec;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * 协议转换类工厂
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public class ProtocolCodecFactory {

    private static final Logger logger = LoggerFactory.getLogger(ProtocolCodecFactory.class);


    public static ProtocolCodec encode(Class<?> clazz){
        return ProtocolCodecEnum.getCodec(clazz);
    }


    /**
     * 获取对应的解码器
     */
    public static ProtocolCodec decode(ByteBuffer buffer){
        for (ProtocolCodecEnum value : ProtocolCodecEnum.values()) {

        }
    }


    @Getter
    enum ProtocolCodecEnum{
        DATA_PACKET(DataPacket.class, new McDataPacketProtocolCodec());

        private final Class<?> clazz;
        private final ProtocolCodec protocolCodec;

        ProtocolCodecEnum(Class<?> clazz, ProtocolCodec protocolCodec) {
            this.clazz = clazz;
            this.protocolCodec = protocolCodec;
        }

        /**
         * 获取对应类型的解码器
         * @author TiAmo(13721682347@163.com)
         */
        public static ProtocolCodec getCodec(Class<?> clazz) {
            for (ProtocolCodecEnum e : values()) {
                if (Objects.equals(clazz, e.getClazz())) {
                    return e.getProtocolCodec();
                }
            }
            return null;
        }
    }

}
