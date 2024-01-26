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
}
