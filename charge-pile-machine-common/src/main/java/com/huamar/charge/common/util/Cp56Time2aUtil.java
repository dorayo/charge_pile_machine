package com.huamar.charge.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.HexUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * @ClassName Cp56Time2a
 * @Description Cp56Time2a 解析与生成
 * @Author liangbo
 * @Date 2023/2/22 21:36
 * @Version 1.0
 **/
public class Cp56Time2aUtil {

    /**
     * Cp56Time2a转时间字符串
     *
     * @param bytes 字符数组
     * @return 时间字符串
     */
    public static String toDateString(byte[] bytes) {
        int milliseconds1 = bytes[0] < 0 ? 256 + bytes[0] : bytes[0];
        int milliseconds2 = bytes[1] < 0 ? 256 + bytes[1] : bytes[1];
        int milliseconds = milliseconds2 * 256 + milliseconds1;
        // 位于 0011 1111
        int minutes = bytes[2] & 0x3F;
        // 位于 0001 1111
        int hours = bytes[3] & 0x1F;
        // 位于 0001 1111
        int days = bytes[4] & 0x1F;
        // 位于 0000 1111
        int months = bytes[5] & 0x0F;
        // 位于 0111 1111
        int years = bytes[6] & 0x7F;
        return "20" + String.format("%02d", years) + "-" + String.format("%02d", months) + "-" + String.format("%02d", days) +
                " " + String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" +
                String.format("%02d", milliseconds / 1000);
    }

    /**
     * 转为时间格式
     *
     * @param bytes 字符数组
     * @return 时间
     */
    public static Date toDate(byte[] bytes) {
        String dateString = toDateString(bytes);
        return DateUtil.parse(dateString, "yyyy-MM-dd HH:mm:ss");
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

    public static void main(String[] args) {
        String hexString = date2HexStr(DateUtil.parse("2020-03-16 17:14:47", "yyyy-MM-dd HH:mm:ss"));
        byte[] bf = dateToByte(DateUtil.parse("2020-03-16 17:14:47", "yyyy-MM-dd HH:mm:ss"));
        System.out.println("新Cp56Time2a: " + hexString);
//        String decodeStr = "A7232E0FB80217";
//        String originStr = "2020-03-16 17:14:47";
//        byte[] byteTimes = HexUtil.decodeHex(decodeStr);
//        System.out.println("Cp56Time2a原值: " + originStr);
//        System.out.println("解析" + decodeStr + ": " + toDateString(byteTimes));
//        Date time = toDate(byteTimes);
//        String hexString = date2HexStr(new Date());
//        System.out.println("新Cp56Time2a: " + hexString);
//        System.out.println("新Cp56Time2a原值: " + toDateString(HexUtil.decodeHex(hexString)));
    }

}