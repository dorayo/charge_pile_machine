package com.huamar.charge.pile;

import cn.hutool.core.convert.Convert;
import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.common.protocol.BasePacket;
import com.huamar.charge.common.protocol.DataPacketReader;
import com.huamar.charge.common.protocol.DataPacketWriter;
import com.huamar.charge.common.protocol.PacketBuilder;
import com.huamar.charge.pile.entity.dto.command.McChargeCommandDTO;
import com.huamar.charge.pile.entity.dto.command.McCommandDTO;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.server.handle.ProtocolCodecFactory;
import com.huamar.charge.pile.server.service.MachineContext;
import com.huamar.charge.common.util.HexExtUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class McDataUploadTest {

    @DisplayName("测试NIO")
    @Test
    public void testNio() {
        byte[] bytes = new byte[]{
                (byte) 0x08,
                (byte) 0x28,
                (byte) 0x09,
                (byte) 0x0A,
                (byte) 0x2A,
                (byte) 0xA0,
                (byte) 0x6F
        };

        DataPacketReader reader = new DataPacketReader(bytes);
        ByteBuffer byteBuffer = reader.getBuffer();
        log.info("reader byte:{}", reader.readByte());
        byte[] bytesData = new byte[byteBuffer.limit() - byteBuffer.position()];
        byteBuffer.get(bytesData);
        log.info("reader byte:{}", bytesData);

        log.info("reader byte:{}", reader.readByte());
        log.info("reader byte:{}", reader.readByte());
        log.info("reader byte:{}", reader.readByte());
        log.info("reader byte:{}", reader.readByte());
        log.info("reader byte:{}", reader.readByte());
        log.info("reader byte:{}", reader.readByte());
    }


    @DisplayName("测试NIO")
    @Test
    public void convert() {
        McChargeCommandDTO command = new McChargeCommandDTO();
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(command.getChargeControl());
        writer.write(command.getGunSort());
        writer.write(command.getChargeEndType());
        writer.write(command.getChargeEndValue());
        writer.write(command.getOrderSerialNumber());
        writer.write(command.getBalance());
        short typeCode = Convert.toShort("0002");
        McCommandDTO commandDTO = new McCommandDTO(typeCode, command.getFieldsByteLength(), writer.toByteArray());
        log.info(commandDTO.toString());
    }

    @DisplayName("测试NIO")
    @Test
    public void test3() {
        Map<Integer, Integer> CMD_MAP = new ConcurrentHashMap<>();
        CMD_MAP.put(1,1);
        CMD_MAP.put(2,2);
        CMD_MAP.put(3,3);
        CMD_MAP.put(4,4);
        CMD_MAP.put(5,5);
        CMD_MAP.put(6,6);
        CMD_MAP.put(7,7);
        CMD_MAP.put(8,8);

        for (Integer item : CMD_MAP.values()){
            log.info("key:{}",item);
            CMD_MAP.remove(item);
        }
    }


    @SneakyThrows
    @DisplayName("commonTest")
    @Test
    public void commonTest() {
        DataPacketWriter writer = new DataPacketWriter();
        writer.write((byte) 0x30);
        writer.write((short) 0x0036);
        writer.write((short) 0xFFFF);
        writer.write(BCDUtils.bcdTime().getData());

        PacketBuilder builder = PacketBuilder.builder()
                .idCode("471000200519302002")
                .messageId(ProtocolCodeEnum.COMMON_ACK.getCode())
                .body(writer);


        BasePacket basePacket = builder.build();
        ByteBuffer byteBuffer = ProtocolCodecFactory.encode(basePacket);
        Assert.notNull(byteBuffer, "byteBuffer is null");
        String encodeHexStr = HexExtUtil.encodeHexStrFormat(byteBuffer.array(), StringPool.SPACE);
        log.info("data:{}", encodeHexStr);
        byte a = (byte) -22200;
        log.info("data:{}", HexExtUtil.encodeHexStr(a));
        log.info("data:{}", (byte)0xFB);
    }




}
