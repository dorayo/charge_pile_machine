package com.huamar.charge.pile.utils.binaryBuilder;

import com.huamar.charge.pile.utils.ProtocolChecks;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class BinaryBuilders {
    static public ByteBuf protocolCLeResponseBuilder(ByteBuf body, short orderV, byte type) {
        ByteBuf bfB = ByteBufAllocator.DEFAULT.heapBuffer();
        int bodyLen = body.readableBytes() + 4;
        bfB.markReaderIndex();
        bfB.writeByte(0x68);
        bfB.writeByte(bodyLen);
        bfB.writeShortLE(orderV);
        bfB.writeByte(0x00);
        bfB.writeByte(type);
        bfB.writeBytes(body);

        bfB.writeShortLE(ProtocolChecks.modbusCRC(bfB.slice(2, bodyLen)));
        return bfB;
    }
}
