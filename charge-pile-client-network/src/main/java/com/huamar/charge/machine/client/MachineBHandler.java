package com.huamar.charge.machine.client;

import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.common.protocol.BasePacket;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.FailMathPacket;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.machine.client.protocol.ProtocolCodecFactory;
import com.huamar.charge.machine.client.protocol.TioPacket;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.tio.client.intf.ClientAioHandler;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.core.exception.TioDecodeException;
import org.tio.core.intf.Packet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 设备业务拦截器
 * date 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
public class MachineBHandler implements ClientAioHandler {


    /**
     * 根据ByteBuffer解码成业务需要的Packet对象.
     * 如果收到的数据不全，导致解码失败，请返回null，在下次消息来时框架层会自动续上前面的收到的数据
     *
     * @param buffer         参与本次希望解码的ByteBuffer
     * @param limit          ByteBuffer的limit
     * @param position       ByteBuffer的position，不一定是0哦
     * @param readableLength ByteBuffer参与本次解码的有效数据（= limit - position）
     * @param channelContext channelContext
     * @return Packet
     * @throws TioDecodeException TioDecodeException
     */
    @Override
    public Packet decode(ByteBuffer buffer, int limit, int position, int readableLength, ChannelContext channelContext) throws TioDecodeException {
        BasePacket basePacket = ProtocolCodecFactory.decode(buffer);
        return new TioPacket(basePacket);
    }

    /**
     * 编码
     *
     * @param packet packet
     * @param tioConfig tioConfig
     * @param channelContext  channelContext
     * @return ByteBuffer
     */
    @Override
    public ByteBuffer encode(Packet packet, TioConfig tioConfig, ChannelContext channelContext) {
        TioPacket encode = (TioPacket) packet;
        return ProtocolCodecFactory.encode(encode.getBasePacket());
    }

    /**
     * 处理消息
     */
    @Override
    @SneakyThrows
    public void handler(Packet packet, ChannelContext channelContext) {
        try {
            TioPacket tioPacket = (TioPacket) packet;
            BasePacket basePacket = tioPacket.getBasePacket();

            if(basePacket instanceof DataPacket){
                DataPacket dataPacket = (DataPacket) basePacket;
                String code = HexExtUtil.encodeHexStr(dataPacket.getMsgId());
                if(code.equals("30")){
                    return;
                }

                log.info("dataPacket messageId:{} data:{}", HexExtUtil.encodeHexStr(dataPacket.getMsgId()), HexExtUtil.encodeHexStrFormat(dataPacket.getMsgBody(), StringPool.SPACE));
                return;
            }

            if(basePacket instanceof FailMathPacket){
                FailMathPacket dataPacket = (FailMathPacket) basePacket;
                log.info("FailMathPacket data:{}", HexExtUtil.encodeHexStrFormat(dataPacket.getBody(), StringPool.SPACE));
            }

        }catch (Exception e){
            log.error("error ==> e:{}", e.getMessage(), e);
        }finally {
            MDC.clear();
        }

    }

    /**
     * 创建心跳包
     *
     * @param channelContext channelContext
     * @return Packet
     * @author tanyaowu
     */
    @Override
    public Packet heartbeatPacket(ChannelContext channelContext) {

        ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(HexExtUtil.decodeHex("23 09 01 12 12 22"));
//        byteBuffer.put(BCDUtils.bcdTime().getData());
        byteBuffer.put((byte) 1);
        byteBuffer.put((byte) 1);
        byteBuffer.put((byte) 2);
        byteBuffer.put((byte) 3);
        byteBuffer.flip();

        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);

        DataPacket dataPacket = new DataPacket();
        dataPacket.setTag((byte) Integer.parseInt("23", 16));
        dataPacket.setMsgId((byte) Integer.parseInt("31", 16));
        dataPacket.setMsgBodyAttr((byte) Integer.parseInt("0", 16));
        dataPacket.setMsgBodyLen((short) byteBuffer.limit());
        dataPacket.setMsgNumber((short) 1);
        dataPacket.setIdCode("123456789012345670".getBytes());
        dataPacket.setMsgBody(bytes);
        dataPacket.setTagEnd((byte) Integer.parseInt("23", 16));
        return new TioPacket(dataPacket);
    }
}
