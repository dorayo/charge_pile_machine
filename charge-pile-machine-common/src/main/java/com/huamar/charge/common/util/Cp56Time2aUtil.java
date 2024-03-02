package com.huamar.charge.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.Date;

/**
 * Cp56Time2a
 * Date: 2023/07/24
 *
 * @author yjl，TiAmo(13721682347 @ 163.com)
 */
@Slf4j
public class Cp56Time2aUtil {

    /**
     * Cp56Time2a转时间字符串
     *
     * @param bytes 字符数组
     * @return 时间字符串
     */
    public static Date toDate(byte[] bytes) {

        int milliseconds1 = bytes[0] < 0 ? 256 + bytes[0] : bytes[0];
        int milliseconds2 = bytes[1] < 0 ? 256 + bytes[1] : bytes[1];
        int milliseconds = milliseconds1 + milliseconds2 * 256;
        // 位于 0011 1111
        int minutes = bytes[2] & 0x3f;
        // 位于 0001 1111
        int hours = bytes[3] & 0x1f;
        // 位于 0000 1111
        int days = bytes[4] & 0x0f;
        // 位于 0001 1111
        int months = bytes[5] & 0x0f;
        // 位于 0111 1111
        int years = bytes[6] & 0x7f;
        final Calendar aTime = Calendar.getInstance();
        aTime.set(Calendar.MILLISECOND, milliseconds);
        aTime.set(Calendar.MINUTE, minutes);
        aTime.set(Calendar.HOUR_OF_DAY, hours);
        aTime.set(Calendar.DAY_OF_MONTH, days);
        aTime.set(Calendar.MONTH, months);
        aTime.set(Calendar.YEAR, years + 2000);
        return aTime.getTime();
    }

    /**
     * 时间转16进制字符串
     *
     * @param date 时间
     * @return 16进制字符串
     */
    public static String date2HexStr(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        StringBuilder builder = new StringBuilder();
        String milliSecond = String.format("%04X", (calendar.get(Calendar.SECOND) * 1000) + calendar.get(Calendar.MILLISECOND));
        builder.append(milliSecond.substring(2, 4));
        builder.append(milliSecond.substring(0, 2));
        builder.append(String.format("%02X", calendar.get(Calendar.MINUTE) & 0x3F));
        builder.append(String.format("%02X", calendar.get(Calendar.HOUR_OF_DAY) & 0x1F));
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        if (week == Calendar.SUNDAY) {
            week = 7;
        } else {
            week--;
        }
        builder.append(String.format("%02X", (week << 5) + (calendar.get(Calendar.DAY_OF_MONTH) & 0x1F)));
        builder.append(String.format("%02X", calendar.get(Calendar.MONTH) + 1));
        builder.append(String.format("%02X", calendar.get(Calendar.YEAR) - 2000));
        return builder.toString();
    }

    /**
     * 时间转16进制字符串
     *
     * @param date 时间
     * @return 16进制字符串
     */
    public static byte[] dateToByte(Date date) {
        String hexString = date2HexStr(date);
        byte[] byteArray = new byte[hexString.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            int index = i * 2;
            int hexValue = Integer.parseInt(hexString.substring(index, index + 2), 16);
            byteArray[i] = (byte) hexValue;
        }
        return byteArray;
    }

}