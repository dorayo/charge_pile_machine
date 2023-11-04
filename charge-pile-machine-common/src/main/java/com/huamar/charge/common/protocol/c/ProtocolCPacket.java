package com.huamar.charge.common.protocol.c;

import com.huamar.charge.pile.utils.ProtocolChecks;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import org.springframework.util.Assert;

import java.nio.ByteBuffer;

@Data
public class ProtocolCPacket {
    String id;
    int bufLen;
    byte sign;
    byte bodyLen;
    int orderV;
    boolean encryptState;
    byte bodyType;
    ByteBuf body;
    int remoteFrameCheckBit;
    int localRealCheckBit;
    ByteBuf idBody;

    private ProtocolCPacket(ByteBuf byteBuf) {
        bufLen = byteBuf.readableBytes();
        sign = byteBuf.readByte();
        bodyLen = byteBuf.readByte();
        byteBuf.markReaderIndex();
        localRealCheckBit = ProtocolChecks.modbusCRC(byteBuf.readBytes(bodyLen));
        byteBuf.resetReaderIndex();
        orderV = byteBuf.readUnsignedShortLE();
        encryptState = byteBuf.readBoolean();
        bodyType = byteBuf.readByte();
        byteBuf.markReaderIndex();
        idBody = byteBuf.readBytes(7);
        byteBuf.resetReaderIndex();
        body = byteBuf.readBytes(bodyLen - 4);
        remoteFrameCheckBit = byteBuf.readUnsignedShortLE();
    }

    static public ProtocolCPacket createFromNettyBuf(ByteBuf byteBuf) {
        return new ProtocolCPacket(byteBuf);
    }
}
