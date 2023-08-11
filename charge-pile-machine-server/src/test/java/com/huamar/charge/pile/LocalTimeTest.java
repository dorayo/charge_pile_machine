package com.huamar.charge.pile;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 通用应答结果处理执行工厂
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
public class LocalTimeTest {

    @Test
    public void testBCD() {
        String[] timePriceBucket = new String[48];
        Arrays.fill(timePriceBucket, "0");

        int start = getTimeBucket("00:30:00");
        int end = getTimeBucket("23:50:00");
        for (int i = start; i < end; i++) {
            timePriceBucket[i] = "1";
        }

        log.info("start:{} end:{} timeBucket:{} size:{}",start, end, timePriceBucket, timePriceBucket.length);
        log.info("size:{} str:{}", timePriceBucket.length, StringUtils.join(timePriceBucket));

        //----------->>
        StringBuilder ecTime = new StringBuilder("000000000000000000000000000000000000000000000000");
		ecTime = SetElectrovalenceUtil.getEcTime("01:00:00", "23:50:00", ecTime,1);
        log.info("size:{} str:{}", ecTime.length(), ecTime);
    }

    private int getTimeBucket(String time){
        LocalTime parse = LocalTime.parse(time);
        log.info(parse.toString());
        int hour = parse.getHour();
        int minute = parse.getMinute();
        return (hour * 2 + (minute >= 30 ? 1 : 0));
    }
}
