package com.huamar.charge.common.protocol.c;

import com.huamar.charge.common.util.netty.NUtils;
import com.huamar.charge.pile.utils.ProtocolChecks;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.buffer.ByteBuf;
import lombok.Data;
import org.springframework.util.Assert;

import java.nio.ByteBuffer;

@Data
public class ProtocolCPacket {
    String id = "";

    public String getId() {
        if (id.length() == 0 && idBody.length != 0) {
            id = BinaryViews.bcdViewsLe(idBody);
        }
        return id;
    }

    int bufLen;
    byte sign;
    byte bodyLen;
    byte[] orderVBf;
    int orderV;
    boolean encryptState;
    byte bodyType;
    byte[] body;
    byte[] remoteFrameCheckBit;
    byte[] localRealCheckBit;
    byte[] localBitBf;
    byte[] remoteBitBf;
    byte[] idBody;

    private ProtocolCPacket(ByteBuf byteBuf) {
        bufLen = byteBuf.readableBytes();
        sign = byteBuf.readByte();
        bodyLen = byteBuf.readByte();
        byteBuf.markReaderIndex();
        int checkLen = byteBuf.readableBytes() - 2;
        localRealCheckBit = ProtocolChecks.modbusCRC(byteBuf.readBytes(checkLen));
        byteBuf.resetReaderIndex();
        orderVBf = NUtils.nBFToBf(byteBuf.readBytes(2));
        encryptState = byteBuf.readBoolean();
        bodyType = byteBuf.readByte();
        byteBuf.markReaderIndex();
        idBody = NUtils.nBFToBf(byteBuf.readBytes(7));
        byteBuf.resetReaderIndex();
        body = NUtils.nBFToBf(byteBuf.readBytes(bodyLen - 4));
        byteBuf.markReaderIndex();
        remoteBitBf = NUtils.nBFToBf(byteBuf.readBytes(2));
        byteBuf.resetReaderIndex();
        remoteFrameCheckBit = NUtils.nBFToBf(byteBuf.readBytes(2));
        orderV = orderVBf[1] << 8 | orderVBf[0];
    }

    static public ProtocolCPacket createFromNettyBuf(ByteBuf byteBuf) {
        return new ProtocolCPacket(byteBuf);
    }
}
