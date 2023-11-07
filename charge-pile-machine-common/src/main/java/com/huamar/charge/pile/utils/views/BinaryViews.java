package com.huamar.charge.pile.utils.views;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class BinaryViews {
    static public String bcdViewsLe(byte[] bf) {
        int resultLen = bf.length * 2;
        byte[] s = new byte[resultLen];
        for (int resultI = resultLen - 1, bfI = 0; resultI > 0; ) {
            byte byteV = bf[bfI++];
            s[resultI--] = (byte) ((byteV & 0x0f) + 0x30);
            s[resultI--] = (byte) ((byteV >> 4) + 0x30);
        }
        return new String(s);
    }

    static public byte[] bcdStringToByte(String bcdStr) {
        byte[] chars = bcdStr.getBytes(StandardCharsets.US_ASCII);
        byte[] result = new byte[chars.length / 2];
        for (int i = 0; i < chars.length; ) {
            int tens = chars[i++] - 0x30;
            int b = chars[i++] - 0x30;
            int v = (tens << 4) + b;
            result[(chars.length / 2) - (i / 2)] = (byte) v;
        }
        return result;
    }
}
