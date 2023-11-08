package com.huamar.charge.pile.utils.views;

import cn.hutool.core.text.StrBuilder;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class BinaryViews {
    static public String bcdViewsLe(byte[] bf) {
        int resultLen = bf.length * 2;
        byte[] s = new byte[resultLen];
        for (int resultI = 0, bfI = 0; resultI < resultLen; ) {
            byte byteV = bf[bfI++];
            s[resultI++] = (byte) ((byteV >> 4) + 0x30);
            s[resultI++] = (byte) ((byteV & 0x0f) + 0x30);
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

    static public String bfToHexStr(ByteBuf bf) {
        bf.markReaderIndex();
        StringBuilder b = new StringBuilder(bf.readableBytes() * 2);
        while (bf.isReadable()) {
            int a = bf.readUnsignedByte();
            String str = Integer.toHexString(a);
            if (str.length() == 1) {
                b.append('0');
            }
            b.append(str);
        }

        bf.resetReaderIndex();
        return b.toString();
    }
}
