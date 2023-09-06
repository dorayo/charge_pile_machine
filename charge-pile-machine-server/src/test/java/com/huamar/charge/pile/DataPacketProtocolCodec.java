package com.huamar.charge.pile;

import cn.hutool.core.util.HexUtil;
import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.common.protocol.*;
import com.huamar.charge.pile.protocol.*;
import com.huamar.charge.common.util.BCCUtil;
import com.huamar.charge.common.util.HexExtUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.tio.core.exception.TioDecodeException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * 协议转换类
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
public class DataPacketProtocolCodec implements ProtocolCodec {

    /**
     * 最小包长度
     */
    protected static final int HEADER_LENGTH = 7;

    /**
     * 数据头长度
     */
    protected static final int BODY_CHECK_LENGTH = 27;

    /**
     * 编码格式
     */
    protected Charset charSet = StandardCharsets.UTF_8;

    /**
     * @return  DataPacket
     */
    @Override
    public Class<DataPacket> getClazz() {
        return DataPacket.class;
    }

    /**
     * 协议编码
     *
     * @param packet packet
     * @author TiAmo(13721682347 @ 163.com)
     */
    @Override
    public ByteBuffer encode(BasePacket packet) {
        DataPacket dataPacket;
        if(!Objects.equals(packet.getClass(), getClazz())){
            return null;
        }
        dataPacket = (DataPacket) packet;
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(dataPacket.getTag());
        writer.write(dataPacket.getMsgId());
        byte msgBodyAttr = dataPacket.getMsgBodyAttr();
        writer.write(msgBodyAttr);
        writer.write(dataPacket.getMsgBodyLen());
        writer.write(dataPacket.getMsgNumber());
        writer.write(dataPacket.getIdCode());
        writer.write(dataPacket.getMsgBody());

        //校验码
        byte[] byteArray = writer.toByteArray();
        String checkTag = BCCUtil.bcc(byteArray, 1, byteArray.length);
        writer.write(HexExtUtil.decodeHex(checkTag)[0]);
        writer.write(dataPacket.getTagEnd());

        // 转义
        byte[] bytes = this.transferEncode(writer.toByteArray());
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return buffer;
    }

    /**
     * 协议解码
     *
     * @param buffer buffer
     * @author TiAmo(13721682347 @ 163.com)
     */
    @SneakyThrows
    @Override
    public DataPacket decode(ByteBuffer buffer) {
        try{
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            // 收到的数据组不了业务包，则返回null以告诉框架数据不够
            if (buffer.remaining() < HEADER_LENGTH) {
                return null;
            }
            int errorCount = 0;
            // 过滤掉错误字节
            boolean returnFlag = false;
            StringBuilder str = new StringBuilder();
            while (true) {
                buffer.mark();
                byte data = buffer.get();
                if (data != DataPacket.TAG) {
                    errorCount++;
                    str.append(HexExtUtil.encodeHexStr(new byte[]{data}));
                    continue;
                }

                byte next = buffer.get();
                // 去除尾
                if (next == DataPacket.TAG) {
                    buffer.reset();
                    next = buffer.get();
                    errorCount++;
                    returnFlag = true;
                    str.append(HexExtUtil.encodeHexStr(new byte[]{next}));
                    break;
                }
                buffer.reset();
                break;
            }

            if (errorCount > 0) {
                log.info("解码错误：count:{}  data{}", errorCount, str);
            }

            if (returnFlag) {
                buffer.clear();
                return null;
            }

            buffer.mark();
            int bodyLength = buffer.getInt();
            if (bodyLength < 0) {
                buffer.clear();
                return null;
            }
            buffer.reset();

            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            bytes = this.transferDecode(bytes);

            // 新包
            DataPacketReader reader = new DataPacketReader(bytes);
            DataPacket packet = new DataPacket();
            packet.setTag(reader.readByte());
            packet.setMsgId(reader.readByte());
            packet.setMsgBodyAttr(reader.readByte());
            packet.setMsgBodyLen(reader.readShort());

            // 消息包中数据是否完整
            if (bytes.length < packet.getMsgBodyLen() + BODY_CHECK_LENGTH) {
                buffer.clear();
                return null;
            }

            // 数据包完整
            Boolean readPacket = reader.readPacket(packet, bytes);
            if (!readPacket) {
                buffer.clear();
                return null;
            }

            StringJoiner joiner = new StringJoiner(StringPool.COMMA, StringPool.EMPTY, StringPool.EMPTY);
            joiner.add(MessageFormatter.format("终端号:{} msgId:{}", new String(packet.getIdCode()), HexExtUtil.encodeHexStr(packet.getMsgId())).getMessage());
            joiner.add(MessageFormatter.format("hexData:{}", HexExtUtil.encodeHexStrFormat(bytes, StringPool.SPACE)).getMessage());
            log.info(joiner.toString());
            return packet;
        }catch (Exception e){
            throw new TioDecodeException(e.getMessage());
        }
    }


    /**
     * 转义编码
     *
     * @author TiAmo(13721682347@163.com)
     */
    public byte[] transferEncode(byte[] hexBytes) {
        ByteBuffer buffer = getTransferEncodeBuffer(hexBytes);
        int position = buffer.position();
        if (position == hexBytes.length) {
            return hexBytes;
        }
        buffer.flip();
        byte[] encodeBytes = new byte[position];
        buffer.get(encodeBytes);

        if (log.isDebugEnabled()) {
            log.debug("transferEncode before:{}", HexExtUtil.encodeHexStrFormat(hexBytes, StringPool.SPACE));
            log.debug("transferEncode after:{}", HexExtUtil.encodeHexStrFormat(encodeBytes, StringPool.SPACE));
        }
        return encodeBytes;
    }


    /**
     * 转义解码
     * 2201->22
     * 2202->23
     * @param hexBytes hexBytes
     * @return byte[]
     */
    public byte[] transferDecode(byte[] hexBytes) {
        ByteBuffer buffer = transferDecodeBuffer(hexBytes);
        int position = buffer.position();
        if (position == hexBytes.length) {
            return hexBytes;
        }
        buffer.flip();
        byte[] dst = new byte[position];
        buffer.get(dst);
        if (log.isDebugEnabled()) {
            log.debug("转义前:{}", HexExtUtil.encodeHexStr(hexBytes));
            log.debug("转义后:{}", HexUtil.encodeHexStr(dst));
        }
        return dst;
    }


    /**
     * 转义Buffer
     *
     * @author TiAmo(13721682347@163.com)
     */
    private ByteBuffer getTransferEncodeBuffer(byte[] hexBytes) {
        byte tag = DataPacket.TAG;
        ByteBuffer buffer = ByteBuffer.allocate(hexBytes.length * 2);
        // 过滤头尾的标识码
        buffer.put(tag);
        for (int i = 1; i < hexBytes.length - 1; i++) {
            byte hex = hexBytes[i];
            // 22转义为22 01
            if (hex == 0x22) {
                buffer.put((byte) 0x22);
                buffer.put((byte) 0x01);
                continue;
            }

            // 23转义为22 02
            if (hex == 0x23) {
                buffer.put((byte) 0x22);
                buffer.put((byte) 0x02);
                continue;
            }
            buffer.put(hex);
        }
        buffer.put(tag);
        return buffer;
    }

    private ByteBuffer transferDecodeBuffer(byte[] hexBytes){
        ByteBuffer buffer = ByteBuffer.allocate(hexBytes.length);
        boolean mark = false;

        buffer.put(DataPacket.TAG);
        // 过滤头尾的标识码
        for (int i = 1; i < hexBytes.length - 1; i++) {
            byte hex = hexBytes[i];
            if (mark) {
                // 22转义
                if (hex == 0x01) {
                    buffer.put((byte) 0x22);
                    continue;
                }
                if (hex == 0x02) {
                    buffer.put((byte) 0x23);
                    continue;
                }
                buffer.put(hex);
                mark = false;
                continue;
            }
            // 23->2202
            if (hex == 0x22) {
                mark = true;
                continue;
            }
            buffer.put(hex);
        }
        buffer.put(DataPacket.TAG);
        return buffer;
    }
}
