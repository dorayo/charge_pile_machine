package com.huamar.charge.common.protocol;

import com.huamar.charge.common.common.codec.BCD;
import com.huamar.charge.common.util.BCCUtil;
import com.huamar.charge.common.util.HexExtUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * DataPacketWriter
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@SuppressWarnings("unused")
@Getter
public class DataPacketReader {

    private static final Logger log = LoggerFactory.getLogger(DataPacketReader.class);

    private final ByteBuffer buffer;

    private final ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;

    /**
     * 消息头的长度
     */
    public static final int HEADER_LENGTH = 7;

    public final Charset charset = StandardCharsets.UTF_8;

    public final Charset GBK = Charset.forName("GBK");

    public DataPacketReader(byte[] bytes) {
        buffer = ByteBuffer.allocate(bytes.length).order(byteOrder);
        buffer.put(bytes);
        buffer.flip();
    }

    public byte readByte() {
        return buffer.get();
    }

    public short readShort() {
        return buffer.getShort();
    }

    public int readInt() {
        return buffer.getInt();
    }

    public byte[] readBytes(int len) {
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * 读取剩余的字节
     *
     * @return byte[]
     */
    public byte[] readRemainBytes() {
        byte[] bytes = new byte[buffer.limit() - buffer.position()];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * 读取定长字符串
     *
     * @param len len
     * @return String
     */
    public String readFixLenString(int len) {
        return HexExtUtil.encodeHexStr(this.readBytes(len));
    }

    /**
     * @return BCD
     */
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public BCD readBCD() {
        byte[] bytes = readBytes(6);
        return new BCD(bytes);
    }

    public String readString(int len) {
        byte[] bytes = readBytes(len);
        return new String(bytes, charset);
    }

    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public BCD readBCD3() {
        byte[] bytes = readBytes(3);
        return new BCD(bytes);
    }

    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public BCD readBCD8() {
        byte[] bytes = readBytes(8);
        return new BCD(bytes);
    }


    public void reset() {
        buffer.flip();
    }

    public boolean isEnd() {
        return buffer.position() >= buffer.array().length;
    }


    public void full(byte[] bytes) {
        buffer.clear();
        buffer.put(bytes);
        buffer.flip();
    }


    /**
     * bcc 校验码校验
     * 数据校验 采用 BCC（异或校验）法，
     * 校验消息头的第一个字节开始，同后一个字节异或，直到校验码前一个字节为止，校验码占用一个字节
     * <p>
     *
     * @param bytes bytes
     * @param check check
     * @param start start
     * @param end   end
     * @return Boolean
     */
    public Boolean bccCheck(byte[] bytes, byte check, int start, int end) {
        byte bccByte = BCCUtil.calculateBCC(bytes, start, end);
        String bcc = HexExtUtil.encodeHexStr(bccByte);
        String checkTagHex = HexExtUtil.encodeHexStr(check);
        if (StringUtils.equalsAnyIgnoreCase(bcc, checkTagHex)) {
            return true;
        }
        if(log.isDebugEnabled()){
            log.debug("BCC校验不通过 源串BCC={} 计算BCC={}", checkTagHex, bcc);
        }
        return false;

    }
}
