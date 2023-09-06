package com.huamar.charge.common.util;

/**
 * 16进制工具类
 * :date 2023/07
 *
 * @author TiAmo(TiAmolikecode@gmail.com)
 *
 */
public class HexExtUtil extends cn.hutool.core.util.HexUtil {


    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data data
     * @return 十六进制String
     */
    public static String encodeHexStr(byte data) {
        return encodeHexStr(new byte[]{data}, false);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *  false 传换成大写格式
     * @param data data
     * @return 十六进制String
     */
    public static String encodeHexStr(byte data, boolean toLowerCase) {
        return encodeHexStr(new byte[]{data}, toLowerCase);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data data
     * @return 十六进制String
     */
    public static String encodeHexStrFormat(byte[] data, String format) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte item: data) {
            appendHex(stringBuilder, item, false);
            stringBuilder.append(format);
        }
        return stringBuilder.toString();
    }

}