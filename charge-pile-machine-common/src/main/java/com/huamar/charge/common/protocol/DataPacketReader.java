package com.huamar.charge.common.protocol;

import com.huamar.charge.common.common.codec.BCD;
import com.huamar.charge.common.util.BCCUtil;
import com.huamar.charge.common.util.HexExtUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * DataPacketWriter
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@SuppressWarnings("unused")
@Getter
@Slf4j
public class DataPacketReader {

    private final ByteBuffer buffer;

    /**
     * 消息头的长度
     */
    public static final int HEADER_LENGTH = 7;

    public final Charset charset = StandardCharsets.UTF_8;

    public final Charset GBK = Charset.forName("GBK");

    public DataPacketReader(byte[] bytes) {
        buffer = ByteBuffer.allocate(bytes.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(bytes);
        buffer.flip();
    }

    public byte readByte() {
        try {
            return buffer.get();
        } catch (Exception e) {
            log.error("readByte", e);
        }
        return 0;
    }

    public short readShort() {
        return buffer.getShort();
    }

    public int readInt() {
        try {
            return buffer.getInt();
        } catch (Exception e) {
            log.error("readInt error", e);
        }
        return 0;
    }

    public byte[] readBytes(int len) {
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * 读取剩余的字节
     * @return byte[]
     */
    public byte[] readRemainBytes() {
        byte[] bytes = new byte[buffer.limit() - buffer.position()];
        buffer.get(bytes);
        return bytes;
    }

    /**
     * 读取定长字符串
     * @param len len
     * @return String
     */
    public String readFixLenString(int len) {
        return HexExtUtil.encodeHexStr(this.readBytes(len));
    }

    /**
     * @return  BCD
     */
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public BCD readBCD() {
        byte[] bytes = readBytes(6);
        return new BCD(bytes);
    }

    public String readString(int len) {
        byte[] bytes = readBytes(len);
        return new String(bytes, GBK);
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
     * 校验数据完整性
     *
     * @param packet packet
     * @param bytes  bytes
     * @return Boolean
     */
    public Boolean readPacket(DataPacket packet, byte[] bytes) {
        packet.setMsgNumber(this.readShort());
        packet.setIdCode(this.readBytes(18));
        short bodyLen = packet.getMsgBodyLen();
        packet.setMsgBody(this.readBytes(bodyLen));
        packet.setCheckTag(this.readByte());
        packet.setTagEnd(this.readByte());
        // 数据校验 采用 BCC（异或校验）法，校验消息头的第一个字节开始，同后一字节异或，直到校验码前一字节为止，校验码占用一个字节
        String bcc = BCCUtil.bcc(bytes, 1, packet.getMsgBodyLen() + 24 + 1);
        String checkTagHex = HexExtUtil.encodeHexStr(packet.getCheckTag());
        this.reset();
        if (!StringUtils.equalsAnyIgnoreCase(bcc, checkTagHex)) {
            log.debug("BCC校验不通过 源串BCC={} 计算BCC={}", checkTagHex, bcc);
            return false;
        }
        return true;
    }
}
