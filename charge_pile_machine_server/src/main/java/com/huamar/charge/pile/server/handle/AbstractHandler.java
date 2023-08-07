package com.huamar.charge.pile.server.handle;

import com.huamar.charge.pile.protocol.*;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.core.intf.AioHandler;
import org.tio.core.intf.Packet;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 消息解码、编码
 *
 * @author asurplus
 */
@Getter
@Slf4j
public abstract class AbstractHandler implements AioHandler {


    /**
     * 消息长度
     */
    protected static final int HEADER_LENGTH = 7;

    /**
     * 消息头标识符号
     */
    protected static final byte HEADER_FLAG = 35;


    protected static final int BODY_CHECK_LENGTH = 27;

    /**
     * 编码格式
     */
    protected Charset charSet = StandardCharsets.UTF_8;



    /**
     * 35 消息头
     * 根据ByteBuffer解码成业务需要的Packet对象.
     * 如果收到的数据不全，导致解码失败，请返回null，在下次消息来时框架层会自动续上前面的收到的数据
     * @param buffer 参与本次希望解码的ByteBuffer
     * @param limit ByteBuffer的limit
     * @param position ByteBuffer的position，不一定是0哦
     * @param readableLength ByteBuffer参与本次解码的有效数据（= limit - position）
     * @param channelContext channelContext
     * @return Packet
     */
    @SneakyThrows
    @Override
    public Packet decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext channelContext) {
        ProtocolCodec protocolCodec = ProtocolCodecFactory.getCodec(DataPacket.class);
        return protocolCodec.decode(buffer);
    }

    /**
     * 编码：把业务消息包编码为可以发送的ByteBuffer
     * 总的消息结构：消息头 + 消息体
     */
    @SneakyThrows
    @Override
    public ByteBuffer encode(Packet packet, TioConfig tioConfig, ChannelContext channelContext) {
        BasePacket basePacket = (BasePacket) packet;
        ProtocolCodec protocolCodec = ProtocolCodecFactory.getCodec(packet.getClass());
        return protocolCodec.encode(basePacket);
    }

}
