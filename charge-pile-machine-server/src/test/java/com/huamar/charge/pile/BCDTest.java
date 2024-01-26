package com.huamar.charge.pile;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.alibaba.fastjson.JSONObject;
import com.huamar.charge.common.common.BCDUtils;
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

}
