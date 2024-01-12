package com.huamar.charge.pile.utils;

import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.common.util.netty.NUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class ProtocolChecks {
    static public byte[] modbusCRC(ByteBuf bf) {
        return modbusCRC(NUtils.nBFToBf(bf));
    }

    private static final int POLYNOMIAL = 0xA001;

    public static byte[] modbusCRC(byte[] data) {
        byte[] result = new byte[2];
        int crc = 0xFFFF;
        for (byte b : data) {
            crc ^= b & 0xFF;
            for (int i = 0; i < 8; i++) {
                if ((crc & 1) == 1) {
                    crc = (crc >>> 1) ^ POLYNOMIAL;
                } else {
                    crc = crc >>> 1;
                }
            }
        }
        result[0] = (byte) (crc & 0xff);
        result[1] = (byte) ((crc & 0xff00) >> 8);
        return result;
    }

    public static void main(String[] args) {
        String hexString = "68226300000147112020200920000210315e00000000000000000000000000000000000075ac";

        byte[] bf = {
                0x55, 0x11, 0x41, (byte) 0x88
        };
        byte[] re = modbusCRC(bf);
        System.out.println(Integer.toHexString(re[0]) + Integer.toHexString(re[1]));


        byte[] checkBytes = HexExtUtil.decodeHex(hexString);
        byte[] res = modbusCRC(HexExtUtil.decodeHex("6300000147112020200920000210315e000000000000000000000000000000000000"));
        System.out.println(Integer.toHexString(res[0]) + Integer.toHexString(res[1]));

        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(256);
        byteBuf.writeBytes(checkBytes);

        byte[] lastTwoBytes = new byte[2];
        byteBuf.getBytes(byteBuf.readableBytes() - 2, lastTwoBytes);
        System.out.println(Integer.toHexString(lastTwoBytes[0]) + Integer.toHexString(lastTwoBytes[1]));

    }
}
