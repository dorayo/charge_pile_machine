package com.huamar.charge.machine.client.protocol;

import com.huamar.charge.common.exception.ProtocolCodecException;
import com.huamar.charge.common.protocol.BasePacket;
import com.huamar.charge.common.protocol.DataPacket;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.exception.TioDecodeException;

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

    /**
     * 获取对应的解码器，
     *  成功解码返回BasePacket
     *  不可组包返回null
     *  不是当前解码器抛出异常 ProtocolCodecException
     * 返回系统
     */
    public static BasePacket decode(ByteBuffer buffer) {
        for (ProtocolCodecEnum codec : ProtocolCodecEnum.values()) {
            try {
                return codec.getProtocolCodec().decode(buffer);
            }catch (ProtocolCodecException ignored){
                logger.warn("协议不匹配 codec:{}", codec.getProtocolCodec().getClazz());
            }
        }
        return null;
    }


    /**
     * 编码协议对象
     */
    public static ByteBuffer encode(BasePacket packet) {
        PacketCodec protocolCodec = ProtocolCodecEnum.getCodec(packet.getClass());
        if(Objects.isNull(protocolCodec)){
            return null;
        }
        return protocolCodec.encode(packet);
    }



    @Getter
    enum ProtocolCodecEnum{
        DATA_PACKET(DataPacket.class, new PacketCodec());

        private final Class<?> clazz;
        private final PacketCodec protocolCodec;

        ProtocolCodecEnum(Class<?> clazz, PacketCodec protocolCodec) {
            this.clazz = clazz;
            this.protocolCodec = protocolCodec;
        }

        /**
         * 获取对应类型的解码器
         * @author TiAmo(13721682347@163.com)
         */
        public static PacketCodec getCodec(Class<?> clazz) {
            for (ProtocolCodecEnum e : values()) {
                if (Objects.equals(clazz, e.getClazz())) {
                    return e.getProtocolCodec();
                }
            }
            return null;
        }
    }

}
