package com.huamar.charge.common.protocol.c;

import cn.hutool.core.io.checksum.CRC16;
import cn.hutool.core.util.ByteUtil;
import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.util.ByteExtUtil;
import com.huamar.charge.common.util.netty.NUtils;
import com.huamar.charge.pile.utils.CRC16Util;
import com.huamar.charge.pile.utils.ProtocolChecks;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Data
@Slf4j
public class ProtocolCPacket {

    String id = "";

    public String getId() {
        if (id.isEmpty() && idBody.length != 0) {
            id = BCDUtils.bcdToStr(idBody);
        }
        return id;
    }

    int bufLen;

    byte sign;

    short bodyLen;

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

    int sourceCrC;

    int checkCrC;

    private ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

    private ProtocolCPacket(ByteBuf byteBuf) {
        bufLen = byteBuf.readableBytes();
        sign = byteBuf.readByte();
        bodyLen = byteBuf.readUnsignedByte();
        if (bodyLen < 4) {
            throw new RuntimeException("bodyLen < 4");
        }
        byteBuf.markReaderIndex();
        int checkLen = byteBuf.readableBytes() - 2;
        ByteBuf checkBuf = byteBuf.readBytes(checkLen);
        byte[] checkData = ByteBufUtil.getBytes(checkBuf);
        checkBuf.release();
        //localRealCheckBit = ProtocolChecks.modbusCRC(checkBuf);

        byteBuf.resetReaderIndex();

        orderVBf = NUtils.nBFToBf(byteBuf.readBytes(2));
        encryptState = byteBuf.readBoolean();
        bodyType = byteBuf.readByte();

        byteBuf.markReaderIndex();
        idBody = NUtils.nBFToBf(byteBuf.readBytes(7));
        byteBuf.resetReaderIndex();

        if (bodyLen == 0) {
            body = new byte[]{};
        } else {
            body = NUtils.nBFToBf(byteBuf.readBytes(bodyLen - 4));
        }

        byteBuf.markReaderIndex();
        remoteBitBf = NUtils.nBFToBf(byteBuf.readBytes(2));
        byteBuf.resetReaderIndex();

        byteBuf.markReaderIndex();
        remoteFrameCheckBit = NUtils.nBFToBf(byteBuf.readBytes(2));

        byteBuf.resetReaderIndex();
        sourceCrC = byteBuf.readUnsignedShortLE();

        orderV = orderVBf[1] << 8 | orderVBf[0];

        byte[] checkCrCBytes = CRC16Util.calculateLECRC(checkData);
        checkCrC = ByteExtUtil.bytesToShortUnsignedLE(checkCrCBytes);

        if(checkCrC != sourceCrC){
            checkCrCBytes = CRC16Util.calculateBECRC(checkData);
            checkCrC = ByteExtUtil.bytesToShortUnsignedLE(checkCrCBytes);
            byteOrder = ByteOrder.BIG_ENDIAN;
        }
    }

    static public ProtocolCPacket createFromNettyBuf(ByteBuf byteBuf) {
        return new ProtocolCPacket(byteBuf);
    }
}
