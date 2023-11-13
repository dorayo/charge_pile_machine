package com.huamar.charge.pile.utils.views;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.StandardCharsets;

/**
 * The type Binary views.
 */
public class BinaryViews {
    /**
     * Bcd views le string.
     *
     * @param bf the bf
     * @return the string
     */
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

    /**
     * Bcd string to byte byte [ ].
     *
     * @param bcdStr the bcd str
     * @return the byte [ ]
     */
    static public byte[] bcdStringToByte(String bcdStr) {
        byte[] chars = bcdStr.getBytes(StandardCharsets.US_ASCII);
        byte[] result = new byte[chars.length / 2];
        for (int resultI = result.length - 1, cLen = chars.length - 1; cLen > 0; ) {
            int b = chars[cLen--] - 0x30;
            int tens = chars[cLen--] - 0x30;
            int v = ((tens << 4) | b);
            result[resultI--] = (byte) v;
        }
        return result;
    }

    /**
     * Bf to hex str string.
     *
     * @param bf the bf
     * @return the string
     */
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

    /**
     * Int view le int.
     *
     * @param bf    the bf
     * @param start the start
     * @return the int
     */
    static public int intViewLe(byte[] bf, int start) {
        return (bf[start++] & 0xff) | ((bf[start++] & 0xff) << 8) | ((bf[start++] & 0xff) << 16) | ((bf[start] & 0xff) << 24);
    }

    /**
     * Short view le long.
     *
     * @param bf    the bf
     * @param start the start
     * @return the long
     */
    static public long shortViewLe(byte[] bf, int start) {
        return (bf[start++] & 0xff) | ((bf[start] & 0xff) << 8);
    }

    /**
     * Number str to bcd byte [ ].
     *
     * @param bf the bf
     * @return the byte [ ]
     */
    static public byte[] numberStrToBcd(byte[] bf) {
        int resultLen = bf.length / 2;
        byte[] result = new byte[resultLen];
        for (int i = 0; i < resultLen; i++) {
            byte a = (byte) (bf[i * 2] - 0x30);
            byte b = (byte) (bf[i * 2 + 1] - 0x30);
            result[i] = (byte) (a << 4 | b);
        }
        return result;
    }

    /**
     * Bcd to number str byte [ ].
     *
     * @param bf the bf
     * @return the byte [ ]
     */
    static public byte[] bcdToNumberStr(byte[] bf) {
        int resultLen = bf.length * 2;
        byte[] result = new byte[resultLen];
        for (int i = 0; i < resultLen; i++) {
            int v = bf[i];
            result[i * 2] = (byte) (v & 0xf);
            result[i * 2 + 1] = (byte) ((v & 0xf0 >> 4) + 3);
        }
        return result;
    }

    /**
     * Bf to hex str string.
     *
     * @param bf the bf
     * @return the string
     */
    static public String bfToHexStr(byte[] bf) {
        ByteBuf a = ByteBufAllocator.DEFAULT.heapBuffer();
        a.writeBytes(bf);
        String result = bfToHexStr(a);
        a.release();
        return result;
    }
}
