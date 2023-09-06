package com.huamar.charge.pile;

import cn.hutool.core.convert.Convert;
import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.pile.entity.dto.event.PileChargeArgConfigDTO;
import com.huamar.charge.pile.entity.dto.platform.event.PileChargeArgConfigPushDTO;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.common.util.JSONParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

import java.nio.charset.Charset;
import java.util.Arrays;

@Slf4j
public class DataPacketTest {


    @DisplayName("test4")
    @Test
    public void test4() {
        String number = "123456789012345678";
        byte[] bytes = number.getBytes();
        String string = Arrays.toString(bytes);
        log.info("code:{}", string);
        log.info("code:{}", new String(bytes));
    }

    @DisplayName("test4")
    @Test
    public void testShort() {
        short code = 0x000A;
        log.info("code:{}", code);
        log.info("code:{}", Short.toString(code));
        log.info("code:{}", HexExtUtil.decodeHex(Short.toString(code)));

        byte[] bytes = Convert.shortToBytes(code);
        log.info("bytes:{}", HexExtUtil.encodeHexStr(bytes));
        log.info("Convert.toHex():{}", Convert.toHex(Short.toString(code), Charset.defaultCharset()));
        log.info("Convert.toHex():{}", Convert.toHex(bytes));
        log.info("Convert.toHex():{}", Long.toHexString(code));
        log.info("String.format.toHex():{}", String.format("%04x", code));

        log.info("{}",Short.parseShort("ffffff"));

    }

    @DisplayName("test4")
    @Test
    public void testShort1() {

        String hexString = Integer.toHexString(65535);
        String unsignedString = Integer.toUnsignedString(65535);
        log.info("{}", hexString);
        log.info("{}", unsignedString);
        log.info("{}", Convert.toShort(hexString));

        int parseInt = Integer.parseInt(hexString, 16);
        log.info("{}", parseInt);

        Short big = (short) Integer.parseUnsignedInt(hexString, 16);
        log.info("{}", big);
        log.info("{}", Integer.toHexString(-1));

        String encodeHexStr = HexExtUtil.encodeHexStr("ff");
        log.info(encodeHexStr);

        log.info("Short.parseShort：{}", Short.parseShort("00A0", 16));


        log.info("str {}", Integer.toHexString(-1));
        log.info("str {}", String.format("%04X", (short) -1));


    }

    @Test
    public void test5() {
        PileChargeArgConfigDTO argConfigDTO = new PileChargeArgConfigDTO();
        argConfigDTO.setSynTime(BCDUtils.bcdTime());
        argConfigDTO.setGunSort((byte) 1);
        PileChargeArgConfigPushDTO pushDTO = new PileChargeArgConfigPushDTO();
        BeanUtils.copyProperties(argConfigDTO, pushDTO);
        log.info("data:{}", JSONParser.jsonString(argConfigDTO));
    }
}
