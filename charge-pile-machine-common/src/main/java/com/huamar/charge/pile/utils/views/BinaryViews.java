package com.huamar.charge.pile.utils.views;

import io.netty.buffer.ByteBuf;

public class BinaryViews {
    static public String bcdViewsLe(ByteBuf bf) {
        int resultLen = bf.readableBytes() * 2;
        byte[] s = new byte[resultLen];
        for (int resultI = resultLen - 1; bf.isReadable(); ) {
            byte byteV = bf.readByte();
            s[resultI--] = (byte) ((byteV & 0x01) + 0x30);
            s[resultI--] = (byte) ((byteV >> 4) + 0x30);
        }
        return new String(s);
    }

}
