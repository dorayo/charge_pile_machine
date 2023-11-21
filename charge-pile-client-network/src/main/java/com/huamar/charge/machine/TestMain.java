package com.huamar.charge.machine;


import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.time.*;
import java.util.Date;
import java.util.Objects;


@Slf4j
public class TestMain {

    @SneakyThrows
    public static void main(String[] args) {
        Thread.currentThread().setName("main");
        log.info("data:{}", StringUtils.join("1",1,1,2,3));
        log.info("data:{}", String.valueOf(-111111));


        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime startTime = LocalDateTime.now().plusMinutes(10);
        LocalDateTime endTime = LocalDateTime.now();

        Date start = Date.from(startTime.atZone(zoneId).toInstant());
        Date end = Date.from(endTime.atZone(zoneId).toInstant());


        long between = DateUtil.between(start, end, DateUnit.MINUTE, false);

        log.info("between:{}", between);


        BigDecimal bigDecimal = new BigDecimal("20.20000000");
        BigDecimal multiply = bigDecimal.multiply(BigDecimal.valueOf(10000));
        log.info("bigDecimal:{}", multiply.intValue());


    }

}