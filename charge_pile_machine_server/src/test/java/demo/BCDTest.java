package demo;

import cn.hutool.core.util.HexUtil;
import com.huamar.charge.pile.protocol.DataPacketReader;
import com.huamar.charge.pile.util.BCCUtil;
import com.huamar.charge.pile.util.HexExtUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Slf4j
public class BCDTest {


    @Test
    public void testBCD() throws UnsupportedEncodingException {
        byte[] b = new byte[]{0x00, 0x01, 0x01};
        DataPacketReader ces=new DataPacketReader(b);
        byte[] bytes = ces.readBytes(3);
        String str =new String(bytes,"GBK");
        String bcd2Str = cn.hutool.core.codec.BCD.bcdToStr(b);
        System.out.println(bcd2Str);
    }


    @DisplayName("测试NIO")
    @Test
    public void testNio() {
        ByteBuffer buf =  ByteBuffer.allocate(10);
        buf.put(new byte[]{1, 1, 3, 4, 5, 6, 7, 8, 9});
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.rewind();
        buf.mark();
        System.out.println(buf.get());
        System.out.println(buf.get());
        buf.reset();
        log.info("去除尾巴-->>");
        System.out.println(buf.get());
        log.info("--------------------");
        System.out.println(buf.get());
        System.out.println(buf.get());
        System.out.println(buf.get());
        System.out.println(buf.get());
        System.out.println(buf.get());
        System.out.println(buf.get());
        System.out.println(buf.get());
    }


    @DisplayName("BCC_Test")
    @Test
    public void test1(){
        byte[] b = {23, 36, 0, 38, 0, 0, 0, 35, 35, 30, 36, 30, 34, 31, 31, 36, 39, 36, 31, 36, 33, 37, 36, 33, 32, 2, 18, 18, 7, 20, 11, 15, 37, 18, 07, 20, 11, 15, 37, 00, 18, 07, 00, 01, 00, 01, 00, 01, 00, 00, 00, 00, 00, 02, 35, 31, 38, 30, 37, 32, 30, 33, 39, 38, 34, 39, 34, 30, 35, 32, 39, 38, 36, 31, 36, 33, 32, 30, 30, 30, 30, 30, 30, 30, 30, 30, 00, 23};
        String bcc = BCCUtil.bcc(b, 1, 87);
        System.out.println(bcc);
        String checkTagHex = HexExtUtil.encodeHexStr(new byte[30]);
        log.info(checkTagHex);
    }

    @DisplayName("BCC_Test")
    @Test
    public void test2(){
        String str = "23220223";
        byte[] transferDecode = HexExtUtil.decodeHex(str);
        System.out.println(HexExtUtil.encodeHexStrFormat(transferDecode, "-"));
    }

}
