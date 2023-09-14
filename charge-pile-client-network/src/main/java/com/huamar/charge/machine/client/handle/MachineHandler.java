package com.huamar.charge.machine.client.handle;

import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.common.protocol.BasePacket;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.FailMathPacket;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.machine.client.protocol.TioPacket;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.tio.client.intf.ClientAioHandler;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;

import java.nio.ByteBuffer;

/**
 * 设备业务拦截器
 * date 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MachineHandler extends AbstractHandler implements ClientAioHandler {

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
                log.info("dataPacket data:{}", HexExtUtil.encodeHexStrFormat(dataPacket.getMsgBody(), StringPool.SPACE));
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

        ByteBuffer byteBuffer = ByteBuffer.allocate(20480);
        byteBuffer.put(BCDUtils.bcdTime().getData());
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
        dataPacket.setIdCode("123456789012345678".getBytes());
        dataPacket.setMsgBody(bytes);
        dataPacket.setCheckTag((byte) Integer.parseInt("23", 16));

        return new TioPacket(dataPacket);
    }
}
