package com.huamar.charge.machine;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;


@Slf4j
public class TestMain {

    @SneakyThrows
    public static void main(String[] args) {
        Thread.currentThread().setName("main");

        boolean creatable = NumberUtils.isCreatable("00220");
        System.out.println("creatable" + creatable);

        System.out.println("to int" + NumberUtils.toInt("00220"));

        System.out.println(Objects.equals(null, "1"));


        ZoneId zoneId = ZoneId.systemDefault();
        Date lastUpdateTime = Date.from(LocalDateTime.now().minusMinutes(1).atZone(zoneId).toInstant());

        LocalDateTime dataLastTime = lastUpdateTime.toInstant().atZone(zoneId).toLocalDateTime();
        Duration between = Duration.between(dataLastTime, LocalDateTime.now());
        System.out.println("between:" + between.getSeconds());
    }

}