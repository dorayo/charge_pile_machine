package com.huamar.charge.pile;

import com.alibaba.fastjson.JSONObject;
import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.ByteExtUtil;
import com.huamar.charge.common.util.Cp56Time2aUtil;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;
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



        String code = "68a20000003b20240302163317638451480873697280037131403715040198b72110020318a08c3a100203184cfd010000000000000000000000000098570100ec920300ec9203004c240300d4d00000000000000000000000000000000000000000000000000000000000007cd49073007c67947300ec920300ec9203004c240300ffffffffffffffffffffffffffffffffff01a08c3a10020318410000000000000000a122";
        //String code = "68A20000003B202403020155176362409791081676800371314037150401C05D370102031810A42A020203184CFD010000000000000000000000000098570100000000000000000000000000D4D000005E0C04005E0C04008429020000000000000000000000000000000000FC2786730082348A73005E0C04005E0C04008429020000000000000000000000000000000000000110A42A02020318400000000000000000FF3A";
        //String code = "68a42500003b20240302161517638404677209784320186303710010070188900f10020318a00f101002031800710200000000000000000000000000c0d40100e8030000e80300004c0400008038010000000000000000000000000000000000000000000000000000000000d0944b2c00b8984b2c00e8030000e80300004c0400004c4758434534434333503034363533333301a00f10900203184000000000000000000000b034";
        byte[] bytes1 = HexExtUtil.decodeHex(code);

        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.heapBuffer(256);
        byteBuf.writeBytes(bytes1);

        ProtocolCPacket packet = ProtocolCPacket.createFromNettyBuf(byteBuf);
        byte[] oldBody = packet.getBody();
        short bodyLen = packet.getBodyLen();


        JSONObject jsonLog = new JSONObject();
        // 流水号
        byte[] orderNumberBytes = new byte[16];
        System.arraycopy(oldBody, 0, orderNumberBytes, 0, 16);
        String orderNumber = BCDUtils.bcdToStr(orderNumberBytes);
        jsonLog.put("orderNumber", orderNumber);

        long firstPrice = BinaryViews.intViewLe(oldBody, 50);
        jsonLog.put("priceJ", firstPrice);

        long secondPrice = BinaryViews.intViewLe(oldBody, 50 + 16);
        jsonLog.put("priceF", secondPrice);

        long thirdPrice = BinaryViews.intViewLe(oldBody, 50 + 16 * 2);
        jsonLog.put("priceP", thirdPrice);

        long forthPrice = BinaryViews.intViewLe(oldBody, 50 + 16 * 3);
        jsonLog.put("priceG", forthPrice);

        // 旧版本金额读取
        int totalPriceStartIndex = bodyLen - 8 - 1 - 7 - 1 - 17 - 4;
        long powerCount = BinaryViews.intViewLe(oldBody, totalPriceStartIndex - 8);
        long total = BinaryViews.intViewLe(oldBody, totalPriceStartIndex);

        int bytesToInt = ByteExtUtil.bytesToInt(oldBody, totalPriceStartIndex, ByteOrder.LITTLE_ENDIAN);
        log.info("end");
    }





}
