package com.huamar.charge.common.util;

/**
 * Byte校验工具
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public class ByteExtUtil {

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

}
