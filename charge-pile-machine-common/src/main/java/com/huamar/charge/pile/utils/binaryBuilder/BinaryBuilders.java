package com.huamar.charge.pile.utils.binaryBuilder;

import com.huamar.charge.common.util.ByteExtUtil;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.pile.utils.CRC16Util;
import com.huamar.charge.pile.utils.ProtocolChecks;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Binary builders.
 */
@Slf4j
public class BinaryBuilders {
    /**
     * Protocol c le response builder byte buf.
     *  description receive content body build finally buffer
     *
     * @param body   the body
     * @param orderV the order v
     * @param type   the type
     * @return the byte buf
     *
     */
    static public ByteBuf protocolCLeResponseBuilder(byte[] body, byte[] orderV, byte type) {
        ByteBuf bfB = ByteBufAllocator.DEFAULT.heapBuffer();
        int bodyLen = body.length + 4;
        bfB.markReaderIndex();
        bfB.writeByte(0x68);
        bfB.writeByte(bodyLen);
        bfB.writeBytes(orderV);
        bfB.writeByte(0x00);
        bfB.writeByte(type);
        bfB.writeBytes(body);
        //bfB.writeBytes(ProtocolChecks.modbusCRC(bfB.readerIndex(2).readBytes(bodyLen)));

        byte[] bytes1 = ProtocolChecks.modbusCRC(bfB.readerIndex(2).readBytes(bodyLen));
        int checkVar1 = ByteExtUtil.bytesToShortUnsignedLE(bytes1);

        byte[] bytes2 = CRC16Util.calculateLECRC(ByteBufUtil.getBytes(bfB.readerIndex(2).readBytes(bodyLen)));
        int checkVar2 = ByteExtUtil.bytesToShortUnsignedLE(bytes2);

        boolean crc16 = checkVar1 == checkVar2;

        if(log.isDebugEnabled()){
            log.info("YKC CRC16 Response packet check:{} hex One:{} hex Two:{}", crc16, HexExtUtil.encodeHexStr(bytes1), HexExtUtil.encodeHexStr(bytes2));
        }

        bfB.writeShortLE(checkVar1);
        bfB.resetReaderIndex();
        return bfB;
    }

    /**
     * Protocol c le response builder byte buf.
     *
     * @param body   the body
     * @param orderV the order v
     * @param type   the type
     * @return the byte buf
     */
    static public ByteBuf protocolCLeResponseBuilder(byte[] body, int orderV, byte type) {
        byte[] orderBf = new byte[2];
        orderBf[0] = (byte) (orderV & 0xff);
        orderBf[1] = (byte) (orderV & 0xff00 >> 8);
        return protocolCLeResponseBuilder(body, orderBf, type);
    }

}
