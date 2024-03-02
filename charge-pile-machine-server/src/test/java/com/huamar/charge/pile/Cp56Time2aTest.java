package com.huamar.charge.pile;

import com.huamar.charge.common.util.ByteExtUtil;
import com.huamar.charge.common.util.Cp56Time2aUtil;
import com.huamar.charge.common.util.HexExtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

@Slf4j
public class Cp56Time2aTest {

    @DisplayName("Cp56Time2aTest")
    @Test
    public void cp56Time2aTest(){
        byte[] bytes = HexExtUtil.decodeHex("0a2639043a0218");
        Date cp56Time = Cp56Time2aUtil.toDate(bytes);
        String dateStr = DateFormatUtils.format(cp56Time, "yyyy-MM-dd HH:mm:ss");
        log.info("dateStr:{}", dateStr);

        int unsignedLE = ByteExtUtil.bytesToShortUnsignedLE(HexExtUtil.decodeHex("0400"));
        System.out.println(unsignedLE);

        String code = "68681000001234567890810A901F00123456702C00A6341600000000000000000000000000000000000000000000000000000000000000000000000000000089860412101840430922433141315F304230334E425F484C303030300000016E3436303131313137333935353333303836393636323033303232303530301FB916";
        byte[] bytes1 = HexExtUtil.decodeHex(code);
        String string = HexExtUtil.encodeHexStrFormat(bytes1, " ");
        log.info("code:{}", string);

    }



}
