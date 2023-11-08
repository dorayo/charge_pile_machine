package com.huamar.charge.pile.utils.binaryBuilder;

import com.huamar.charge.pile.utils.ProtocolChecks;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class BinaryBuilders {
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
        bfB.writeBytes(ProtocolChecks.modbusCRC(bfB.slice(2, bodyLen)));
        return bfB;
    }

    static public ByteBuf protocolCLeResponseBuilder(byte[] body, int orderV, byte type) {
        byte[] orderBf = new byte[2];
        orderBf[0] = (byte) (orderV & 0xff);
        orderBf[1] = (byte) (orderV & 0xff00 >> 8);
        return protocolCLeResponseBuilder(body, orderBf, type);
    }

}
