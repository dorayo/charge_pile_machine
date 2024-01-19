package com.huamar.charge.pile;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.fastjson.JSONObject;
import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.common.codec.BCD;
import com.huamar.charge.common.util.Cp56Time2aUtil;
import com.huamar.charge.common.util.DatePattern;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class BCDTest {


    @DisplayName("test01")
    @Test
    public void test01(){
        Short aShort = Convert.toShort(65500);
        System.out.println(aShort);
    }


    @DisplayName("test00")
    @Test
    public void test00(){
        LocalTime startTime = LocalTime.parse("23:00:00");
        LocalTime endTime = LocalTime.parse("24:00:00");

        int startPeriodIndex = startTime.toSecondOfDay() / 1800;
        int endPeriodIndex = endTime.toSecondOfDay() / 1800;

        Map<BigDecimal, Byte> jfpgIndexMap = new HashMap<>();
        byte[] priceBucketJFPG = new byte[48];
        Byte index = jfpgIndexMap.getOrDefault(new BigDecimal("0"), (byte) 0);
        Arrays.fill(priceBucketJFPG, startPeriodIndex, endPeriodIndex, (byte) 3);
        //锁定在一个时间段 Arrays.fill无法填充
        if(startPeriodIndex == endPeriodIndex){
            priceBucketJFPG[startPeriodIndex] = index;
        }
    }

    @Data
    static class Demo {
        Short a;
    }

    @Data
    static class Demo1 {
        int a = 3000;
    }


    public static int readUnsignedShort(ByteBuffer buffer) {
        short signedShort = buffer.getShort();
        return Short.toUnsignedInt(signedShort);
    }




    public static void main(String[] args) {
        String string = JSONParser.jsonString(new Demo1());
        Demo d = JSONParser.parseObject(string, Demo.class);


        log.info("{}--{}", d, "");

        short typeCode = Short.parseShort("9999");
        log.info("{}--      {}", d, typeCode);
    }



    @DisplayName("timeBucket48")
    @Test
    public void timeBucket48() {

        byte[] timePriceBucket = new byte[48];
        Arrays.fill(timePriceBucket, (byte) 0);

        LocalTime startTime = LocalTime.of(8, 0, 0); // 给定的开始时间
        LocalTime endTime = LocalTime.of(23, 59, 59); // 给定的结束时间

        int startPeriodIndex = startTime.toSecondOfDay() / 1800;
        int endPeriodIndex = endTime.toSecondOfDay() / 1800;

        System.out.println("开始时间索引位置：" + startPeriodIndex);
        System.out.println("结束时间索引位置：" + endPeriodIndex);

        int a = 0;
        System.out.println("-" + a++ +"-"+ a);



        List<BigDecimal> numbers = new ArrayList<>();
        numbers.add(new BigDecimal("9.99"));
        numbers.add(new BigDecimal("1.23"));
        numbers.add(new BigDecimal("5.67"));
        numbers.add(new BigDecimal("3.45"));


        numbers.add(new BigDecimal("3.45"));
        numbers.add(new BigDecimal("9.99"));
        numbers.add(new BigDecimal("1.23"));
        numbers.add(new BigDecimal("5.60"));




        List<BigDecimal> distinctNumbers = numbers.stream()
                .distinct().sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        // 使用自定义比较器排序

        System.out.println(distinctNumbers.toString());





    }

    @DisplayName("BCD test")
    @Test
    public void testBCD() {
//        String number = "MA9GGNXU3";
//        byte[] idBytes = BCDUtils.strToBcd(number);
//        byte[] idBytes2 = BinaryViews.bcdStringToByte(number);
//        log.info("idBytes:{}", idBytes);
//        log.info("idBytes2:{}", idBytes2);
//
//
//        String id = BinaryViews.bcdViewsLe(idBytes2);
//        String id2 = BCDUtils.bcdToStr(idBytes2);
//
//        log.info("id:{} , idCode:{}", id, id2);
//
//
//
//
//        ByteBuf bodyBuf = ByteBufAllocator.DEFAULT.buffer(256).order(ByteOrder.LITTLE_ENDIAN);;
//        bodyBuf.writeBytes(new byte[]{0,1,2,3,4,5});
//
//        log.info("readIndex:{} writerIndex:{}", bodyBuf.readerIndex(), bodyBuf.writerIndex());


        JSONObject params = new JSONObject();
        String hex = "68 A2 02 00 00 3B 20 24 01 08 18 52 17 44 31 11 01 95 30 60 86 40 00 22 03 23 50 10 03 01 48 71 34 12 08 01 18 38 4A 38 12 08 01 18 90 5F 01 00 00 00 00 00 00 00 00 00 00 00 00 00 D0 FB 01 00 26 11 00 00 26 11 00 00 44 16 00 00 10 98 02 00 00 00 00 00 00 00 00 00 00 00 00 00 D0 FB 01 00 00 00 00 00 00 00 00 00 00 00 00 00 6E 38 90 00 00 94 49 90 00 00 26 11 00 00 26 11 00 00 44 16 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 38 4A 38 12 08 01 18 40 00 00 00 00 00 00 00 00 A3 3A";
        byte[] bytes = HexExtUtil.decodeHex(hex);
        ByteBuf bodyBufNew = ByteBufAllocator.DEFAULT.buffer(256);
        bodyBufNew.writeBytes(bytes);
        bodyBufNew.readBytes(6);
        byte[] idCode = ByteBufUtil.getBytes(bodyBufNew, 22, 7);
        String string = BCDUtils.bcdToStr(idCode);
        log.info("idCode:{}", string);

        int startIndex = (6) + 16 + 7 + 1 + 7 + 7;
        bodyBufNew.readerIndex(startIndex);
        long jUnit = bodyBufNew.readUnsignedIntLE();
        long jPower = bodyBufNew.readUnsignedIntLE();
        long jLoss = bodyBufNew.readUnsignedIntLE();
        long jPrice = bodyBufNew.readUnsignedIntLE();
        log.info("jUnit:{}, jPower:{}, jLoss:{}, jPrice:{}", jUnit, jPower, jLoss, jPrice);


        long fUnit = bodyBufNew.readUnsignedIntLE();
        long fPower = bodyBufNew.readUnsignedIntLE();
        long fLoss = bodyBufNew.readUnsignedIntLE();
        long fPrice = bodyBufNew.readUnsignedIntLE();
        log.info("fUnit:{}, fPower:{}, fLoss:{}, fPrice:{}", fUnit, fPower, fLoss, fPrice);

        long pUnit = bodyBufNew.readUnsignedIntLE();
        long pPower = bodyBufNew.readUnsignedIntLE();
        long pLoss = bodyBufNew.readUnsignedIntLE();
        long pPrice = bodyBufNew.readUnsignedIntLE();
        log.info("pUnit:{}, pPower:{}, pLoss:{}, pPrice:{}", pUnit, pPower, pLoss, pPrice);

        long gUnit = bodyBufNew.readUnsignedIntLE();
        long gPower = bodyBufNew.readUnsignedIntLE();
        long gLoss = bodyBufNew.readUnsignedIntLE();
        long gPrice = bodyBufNew.readUnsignedIntLE();
        log.info("gUnit:{}, gPower:{}, gLoss:{}, gPrice:{}", gUnit, gPower, gLoss, gPrice);

        byte[] e1 = new byte[5];
        bodyBufNew.readBytes(e1);

        byte[] e2 = new byte[5];
        bodyBufNew.readBytes(e2);
        long power = bodyBufNew.readUnsignedIntLE();
        long loss = bodyBufNew.readUnsignedIntLE();
        long countPrice = bodyBufNew.readUnsignedIntLE();
        log.info("power:{} loss:{} countPrice:{}", power, loss, countPrice);

        byte[] vin = new byte[17];
        bodyBufNew.readBytes(vin);
        String asciiVin = new String(vin, StandardCharsets.US_ASCII);
        log.info("asciiVin:{}", asciiVin);






        int priceStartIndex = (6) + 16 + 7 + 14 + 1;
        long thirdPrice = BinaryViews.intViewLe(bytes, priceStartIndex + 12 + 16 * 2);
        log.info("thirdPrice:{}", thirdPrice);

        long secondPrice = BinaryViews.intViewLe(bytes, priceStartIndex + 12 + 16);
        params.put("secondPrice", secondPrice);

        log.info("params:{}", params);



        // 开始结束时间
        byte[] startTimeBt = new byte[7];
        byte[] endTimeBt = new byte[7];
        System.arraycopy(bytes, 6 + 16 + 7 + 1, startTimeBt, 0, 7);
        System.arraycopy(bytes, 6 + 16 + 7 + 1 + 7, endTimeBt, 0, 7);

        Date startTD = Cp56Time2aUtil.toDate(startTimeBt);
        Date endTD = Cp56Time2aUtil.toDate(endTimeBt);
        LocalDateTime startT = LocalDateTime.ofInstant(startTD.toInstant(), ZoneId.systemDefault());
        LocalDateTime endT = LocalDateTime.ofInstant(endTD.toInstant(), ZoneId.systemDefault());

        byte[] startTimeBt2 = ByteBufUtil.getBytes(bodyBufNew, 6 + 24, 7);
        log.info("bodyBufNew:{}", bodyBufNew);
        byte[] endTimeBt2 = ByteBufUtil.getBytes(bodyBufNew, 6 + 31, 7);
        log.info("bodyBufNew:{}", bodyBufNew);

        Date startTD2 = Cp56Time2aUtil.toDate(startTimeBt2);
        Date endTD2 = Cp56Time2aUtil.toDate(endTimeBt2);
        LocalDateTime startT2 = LocalDateTime.ofInstant(startTD2.toInstant(), ZoneId.systemDefault());
        LocalDateTime endT2 = LocalDateTime.ofInstant(endTD2.toInstant(), ZoneId.systemDefault());

        log.info("startT:{} endT:{}", LocalDateTimeUtil.format(startT, DatePattern.NORM_DATETIME_PATTERN), LocalDateTimeUtil.format(endT, DatePattern.NORM_DATETIME_PATTERN));
        log.info("startT:{} endT:{}", LocalDateTimeUtil.format(startT2, DatePattern.NORM_DATETIME_PATTERN), LocalDateTimeUtil.format(endT2, DatePattern.NORM_DATETIME_PATTERN));
        log.info("订单充值时长：{}", Duration.between(startT, endT).getSeconds());
        log.info("bodyBufNew:{}", bodyBufNew);


        bodyBufNew.readerIndex(0);
        byte[] oldBody = ByteBufUtil.getBytes(bodyBufNew);
        int totalPriceStartIndex = oldBody.length - 2 - 8 - 1 - 7 - 1 - 17 - 4;
        long powerCount = BinaryViews.intViewLe(oldBody, totalPriceStartIndex - 8);
        long total = BinaryViews.intViewLe(oldBody, totalPriceStartIndex);

        log.info("powerCount:{}, total:{}", powerCount, total);

        bodyBufNew.readerIndex(102 + 10);
        long powerCountLE = bodyBufNew.readUnsignedIntLE();

        bodyBufNew.readerIndex(102 + 18);
        long totalLE = bodyBufNew.readUnsignedIntLE();

        log.info("powerCountLE:{}, totalLE:{}", powerCount, total);

    }


    @Test
    @DisplayName("test Vin")
    public void testVin(){
        String vin = "      JX6MA000422";

        System.out.printf(vin);

        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(256);
        byteBuf.writeBytes(HexExtUtil.decodeHex("80 3E"));

        short readUnsignedShort = byteBuf.readShortLE();

    }
}
