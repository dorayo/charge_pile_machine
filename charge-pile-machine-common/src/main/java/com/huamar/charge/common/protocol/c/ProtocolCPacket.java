package com.huamar.charge.common.protocol.c;

import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.util.ByteExtUtil;
import com.huamar.charge.common.util.netty.NUtils;
import com.huamar.charge.pile.utils.CRC16Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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

//    public static void main(String[] args) {
//        String code = "68 A0 00 00 00 3B 20 24 03 01 16 30 17 63 48 19 14 91 37 83 80 80 32 01 06 00 10 13 10 01 78 69 1E 10 01 03 18 80 3E 1F 10 01 03 18 D0 7E 01 00 00 00 00 00 00 00 00 00 00 00 00 00 A8 5B 01 00 00 00 00 00 00 00 00 00 00 00 00 00 F8 24 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 A4 D3 2C 10 A4 D3 2C 10 00 00 00 00 00 00 00 00 00 00 00 00 4C 47 42 36 31 59 45 41 35 4D 53 30 30 35 35 36 31 01 80 3E 1F 10 01 03 18 41 00 00 00 00 00 00 00 00 6D E3";
//        byte[] bytes = HexExtUtil.decodeHex(code);
//        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.heapBuffer(256);
//        byteBuf.writeBytes(bytes);
//        ProtocolCPacket fromNettyBuf = ProtocolCPacket.createFromNettyBuf(byteBuf);
//    }
}
