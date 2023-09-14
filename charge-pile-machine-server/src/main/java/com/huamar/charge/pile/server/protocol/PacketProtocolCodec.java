package com.huamar.charge.pile.server.protocol;

import cn.hutool.core.util.HexUtil;
import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.common.protocol.*;
import com.huamar.charge.common.util.BCCUtil;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.LoggerEnum;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.helpers.MessageFormatter;
import org.tio.core.exception.TioDecodeException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * 协议转换类
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
public class PacketProtocolCodec implements ProtocolCodec {

    private final Logger log = LoggerFactory.getLogger(LoggerEnum.MACHINE_PACKET_LOGGER.getCode());

    /**
     * 最小包长度
     */
    protected static final int HEADER_LENGTH = 7;

    /**
     * 数据头长度
     */
    protected static final int BODY_CHECK_LENGTH = 27;

    /**
     * @return DataPacket
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
        if (!Objects.equals(packet.getClass(), getClazz())) {
            return null;
        }
        DataPacket dataPacket = (DataPacket) packet;
        byte[] bytes = this.encodeToBytes(dataPacket);
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
    @SuppressWarnings("DuplicatedCode")
    @SneakyThrows
    @Override
    public BasePacket decode(ByteBuffer buffer) {
        try {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            // 收到的数据组不了业务包，则返回null以告诉框架数据不够
            if (buffer.remaining() < HEADER_LENGTH) {
                return null;
            }

            ByteBuffer failBuffer = ByteBuffer.allocate(buffer.capacity());
            ByteBuffer packetBuffer = ByteBuffer.allocate(buffer.capacity());
            this.unpack(buffer, failBuffer, packetBuffer);

            // 异常解析数据
            if (failBuffer.hasRemaining()) {
                byte[] bytes = new byte[failBuffer.remaining()];
                failBuffer.get(bytes);
                log.warn("fail packet:{}", HexExtUtil.encodeHexStrFormat(bytes, StringPool.SPACE));
                return new FailMathPacket(bytes);
            }

            byte[] bytes = new byte[packetBuffer.remaining()];
            packetBuffer.get(bytes);
            bytes = this.transferDecode(bytes);

            // 新包
            DataPacketReader reader = new DataPacketReader(bytes);
            DataPacket packet = new DataPacket();
            packet.setTag(reader.readByte());
            packet.setMsgId(reader.readByte());
            packet.setMsgBodyAttr(reader.readByte());
            packet.setMsgBodyLen(reader.readShort());

            // 不能小于0
            if (packet.getMsgBodyLen() < (short) 0) {
                packet.setMsgBodyLen((short) 0);
            }

            // 消息包中数据是否完整
            if (bytes.length < packet.getMsgBodyLen() + BODY_CHECK_LENGTH) {
                log.info("数据包长度异常：msgBodyLength:{}, bytes.length:{}, 数据头长度:{} ", packet.getMsgBodyLen(), bytes.length, BODY_CHECK_LENGTH);
                return null;
            }

            // 数据包完整
            Boolean readPacket = reader.readPacket(packet, bytes);
            if (!readPacket) {
                log.warn("fail packet:{}", HexExtUtil.encodeHexStrFormat(bytes, StringPool.SPACE));
                return new FailMathPacket(bytes);
            }

            String messageId = HexExtUtil.encodeHexStr(packet.getMsgId());
            MDC.put(ConstEnum.ID_CODE.getCode(), new String(packet.getIdCode()));
            StringJoiner joiner = new StringJoiner(StringPool.COMMA, StringPool.EMPTY, StringPool.EMPTY);
            joiner.add(MessageFormatter.format("终端号:{} msgId:{}", new String(packet.getIdCode()), messageId).getMessage());
            joiner.add(MessageFormatter.format("hexData:{}", HexExtUtil.encodeHexStrFormat(bytes, StringPool.SPACE)).getMessage());
            log.info(joiner.toString());
            return packet;
        } catch (Exception e) {
            log.error("fail TioDecodeException:{}", e.getMessage(), e);
            throw new TioDecodeException(e.getMessage());
        } finally {
            MDC.clear();
        }
    }

    /**
     * 编码
     *
     * @param packet  packet
     * @param byteBuf byteBuf
     */
    @Override
    public boolean encode(BasePacket packet, ByteBuf byteBuf) {
        if (!Objects.equals(packet.getClass(), getClazz())) {
            return false;
        }
        DataPacket dataPacket = (DataPacket) packet;
        byte[] bytes = this.encodeToBytes(dataPacket);
        byteBuf.writeBytes(bytes);
        return true;
    }

    /**
     * 协议解码
     *
     * @param byteBuf byteBuf
     * @author TiAmo(13721682347 @ 163.com)
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    public BasePacket decode(ByteBuf byteBuf) {
        try {
            // 收到的数据组不了业务包，则返回null以告诉框架数据不够
            if (byteBuf.readableBytes() < HEADER_LENGTH) {
                return null;
            }

            ByteBuffer failBuffer = ByteBuffer.allocate(byteBuf.capacity());
            ByteBuffer packetBuffer = ByteBuffer.allocate(byteBuf.capacity());
            this.unpack(byteBuf, failBuffer, packetBuffer);

            // 异常解析数据
            if (failBuffer.hasRemaining()) {
                byte[] bytes = new byte[failBuffer.remaining()];
                failBuffer.get(bytes);
                log.warn("fail packet:{}", HexExtUtil.encodeHexStrFormat(bytes, StringPool.SPACE));
                return new FailMathPacket(bytes);
            }

            byte[] bytes = new byte[packetBuffer.remaining()];
            packetBuffer.get(bytes);
            bytes = this.transferDecode(bytes);

            // 新包
            DataPacketReader reader = new DataPacketReader(bytes);
            DataPacket packet = new DataPacket();
            packet.setTag(reader.readByte());
            packet.setMsgId(reader.readByte());
            packet.setMsgBodyAttr(reader.readByte());
            packet.setMsgBodyLen(reader.readShort());

            // 不能小于0
            if (packet.getMsgBodyLen() < (short) 0) {
                packet.setMsgBodyLen((short) 0);
            }

            // 消息包中数据是否完整
            if (bytes.length < packet.getMsgBodyLen() + BODY_CHECK_LENGTH) {
                log.info("数据包长度异常：msgBodyLength:{}, bytes.length:{}, 数据头长度:{} ", packet.getMsgBodyLen(), bytes.length, BODY_CHECK_LENGTH);
                return null;
            }

            // 数据包完整
            Boolean readPacket = reader.readPacket(packet, bytes);
            if (!readPacket) {
                log.warn("fail packet:{}", HexExtUtil.encodeHexStrFormat(bytes, StringPool.SPACE));
                return new FailMathPacket(bytes);
            }

            String messageId = HexExtUtil.encodeHexStr(packet.getMsgId());
            MDC.put(ConstEnum.ID_CODE.getCode(), new String(packet.getIdCode()));
            StringJoiner joiner = new StringJoiner(StringPool.COMMA, StringPool.EMPTY, StringPool.EMPTY);
            joiner.add(MessageFormatter.format("终端号:{} msgId:{}", new String(packet.getIdCode()), messageId).getMessage());
            joiner.add(MessageFormatter.format("hexData:{}", HexExtUtil.encodeHexStrFormat(bytes, StringPool.SPACE)).getMessage());
            log.info(joiner.toString());
            return packet;
        } catch (Exception e) {
            log.error("fail TioDecodeException:{}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        } finally {
            MDC.clear();
        }
    }

    /**
     * 编码字节数组
     *
     * @param packet packet
     * @return byte[]
     */
    private byte[] encodeToBytes(DataPacket packet){
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(packet.getTag());
        writer.write(packet.getMsgId());
        byte msgBodyAttr = packet.getMsgBodyAttr();
        writer.write(msgBodyAttr);
        writer.write(packet.getMsgBodyLen());
        writer.write(packet.getMsgNumber());
        writer.write(packet.getIdCode());
        writer.write(packet.getMsgBody());

        //校验码
        byte[] byteArray = writer.toByteArray();
        String checkTag = BCCUtil.bcc(byteArray, 1, byteArray.length);
        writer.write(HexExtUtil.decodeHex(checkTag)[0]);
        writer.write(packet.getTagEnd());
        // 转义
        return this.transferEncode(writer.toByteArray());
    }

    /**
     * 转义编码
     *
     * @author TiAmo(13721682347 @ 163.com)
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
     *
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
            log.debug("transferDecode before:{}", HexExtUtil.encodeHexStr(hexBytes));
            log.debug("transferDecode after:{}", HexUtil.encodeHexStr(dst));
        }
        return dst;
    }


    /**
     * 转义Buffer
     *
     * @author TiAmo(13721682347 @ 163.com)
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

    private ByteBuffer transferDecodeBuffer(byte[] hexBytes) {
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

    /**
     * 粘包处理
     *
     * @param buffer buffer
     */
    private void unpack(ByteBuffer buffer, ByteBuffer failBuffer, ByteBuffer packetBuffer) {
        while (true) {
            buffer.mark();
            byte data = buffer.get();
            if (data != DataPacket.TAG) {
                failBuffer.put(data);
                continue;
            }

            // 去除粘包尾巴 tag 35
            byte next = buffer.get();
            if (next == DataPacket.TAG) {
                buffer.reset();
                failBuffer.put(buffer.get());
            }
            break;
        }

        // 异常解析数据
        failBuffer.flip();
        if (failBuffer.hasRemaining()) {
            return;
        }

        buffer.reset();

        byte make = buffer.get();
        packetBuffer.put(make);
        while (true){
            byte next = buffer.get();
            packetBuffer.put(next);
            if(next == DataPacket.TAG){
                break;
            }
        }

        // 不可封包数据
        packetBuffer.flip();
        if(packetBuffer.remaining() < HEADER_LENGTH){
            failBuffer.clear();
            byte[] failBytes = new byte[packetBuffer.remaining()];
            packetBuffer.get(failBytes);
            failBuffer.put(failBytes);
            failBuffer.flip();
        }
    }

    /**
     * 粘包处理
     *
     * @param buffer buffer
     */
    private void unpack(ByteBuf buffer, ByteBuffer failBuffer, ByteBuffer packetBuffer) {
        while (true) {
            buffer.markReaderIndex();
            byte data = buffer.readByte();
            if (data != DataPacket.TAG) {
                failBuffer.put(data);
                continue;
            }

            // 去除粘包尾巴 tag 35
            byte next = buffer.readByte();
            if (next == DataPacket.TAG) {
                buffer.resetReaderIndex();
                failBuffer.put(buffer.readByte());
            }
            break;
        }

        // 异常解析数据
        failBuffer.flip();
        if (failBuffer.hasRemaining()) {
            return;
        }

        buffer.resetReaderIndex();

        byte make = buffer.readByte();
        packetBuffer.put(make);
        while (true){
            byte next = buffer.readByte();
            packetBuffer.put(next);
            if(next == DataPacket.TAG){
                break;
            }
        }

        // 不可封包数据
        packetBuffer.flip();
        if(packetBuffer.remaining() < HEADER_LENGTH){
            failBuffer.clear();
            byte[] failBytes = new byte[packetBuffer.remaining()];
            packetBuffer.get(failBytes);
            failBuffer.put(failBytes);
            failBuffer.flip();
        }
    }
}
