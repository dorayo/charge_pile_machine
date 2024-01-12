package com.huamar.charge.common.util;

import cn.hutool.core.util.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;

import java.nio.ByteOrder;

/**
 * Byte校验工具
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public class ByteExtUtil extends ByteUtil {

    /**
     * 通过byte数组取到short
     *
     * @param data  data
     * @param index index
     *              第几位开始取
     * @return short
     */
    public static short getShort(byte[] data, int index) {
        return (short) (((data[index + 1] << 8) | data[index] & 0xff));
    }

    /**
     * 转换 Int
     *
     * @param bytes bytes
     * @return short
     */
    public static int bytesToShortUnsignedLE(byte[] bytes) {
        ByteBuf checkCrcBuf = ByteBufAllocator.DEFAULT.buffer(2);
        checkCrcBuf.writeBytes(bytes);
        return checkCrcBuf.readUnsignedShortLE();
    }


    /**
     *
     * @param var var
     * @return static
     */
    public static byte[] unsignedShortToBytes(int var) {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(4);
        byteBuf.writeShortLE(var);
        return ByteBufUtil.getBytes(byteBuf);
    }


}
