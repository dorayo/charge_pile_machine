package com.huamar.charge.machine;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
        log.info("data:{}", StringUtils.join("1",1,1,2,3));
        log.info("data:{}", String.valueOf(-111111));
    }

}