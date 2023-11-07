package com.huamar.charge.pile.server.service.command;

import cn.hutool.core.convert.Convert;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketWriter;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.netty.NUtils;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.entity.dto.command.McChargeCommandDTO;
import com.huamar.charge.pile.entity.dto.command.McCommandDTO;
import com.huamar.charge.pile.enums.McCommandEnum;
import com.huamar.charge.pile.enums.McTypeEnum;
import com.huamar.charge.pile.enums.NAttrKeys;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import com.huamar.charge.pile.utils.binaryBuilder.BinaryBuilders;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 远程控制执行-充电控制
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McChargeCommandExecute implements McCommandExecute<McChargeCommandDTO> {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McCommandEnum getCode() {
        return McCommandEnum.CHARGE;
    }

    static public byte[] empty = new byte[16];

    /**
     * 执行方法
     *
     * @param command command
     */
    @Override
    public void execute(McChargeCommandDTO command) {
        Short messageNumber = SessionManager.getMessageNumber(command.getIdCode());
        SimpleSessionChannel session = (SimpleSessionChannel) SessionManager.get(command.getIdCode());
        if (session.getType() == McTypeEnum.C) {
            ProtocolCPacket packet = session.channel().attr(NAttrKeys.PROTOCOL_C_LATEST_PACKET).get();
            ByteBuf responseBody = ByteBufAllocator.DEFAULT.heapBuffer(16 + 7 + 1 + 8 + 8 + 4);
            responseBody.writeBytes(BinaryViews.bcdStringToByte(new String(command.getOrderSerialNumber()).substring(0, 32)));
            responseBody.writeBytes(packet.getIdBody());
            responseBody.writeByte(command.getGunSort());
            responseBody.writeBytes(empty);
            responseBody.writeIntLE(command.getBalance());
            ByteBuf response = BinaryBuilders.protocolCLeResponseBuilder(NUtils.nBFToBf(responseBody), packet.getOrderVBf(), (byte) 0x34);
            responseBody.release();
            session.channel().writeAndFlush(response).addListener((f) -> {
                if (f.isSuccess()) {
                    log.info("0x34 success");
                } else {
                    log.error("0x34 success error");
                    f.cause().printStackTrace();
                }
                response.release();
            });
            return;
        }

        DataPacket packet = this.packet(command);
        packet.setMsgNumber(messageNumber);
        boolean sendCommand = SessionManager.writePacket(packet);
        command.headCommandState(sendCommand);
        command.headMessageNum(messageNumber);
        log.info("ChargeCommand idCode:{} sendCommand:{} ", command.getIdCode(), sendCommand);
    }

    /**
     * 指令转换
     *
     * @param command McChargeCommandDTO
     */
    @Override
    public McCommandDTO convert(McChargeCommandDTO command) {
        //cover a b
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(command.getChargeControl());
        writer.write(command.getGunSort());
        writer.write(command.getChargeEndType());
        writer.write(command.getChargeEndValue());
        writer.write(command.getOrderSerialNumber());
        writer.write(command.getBalance());
        short typeCode = Convert.toShort(getCode().getCode());
        return new McCommandDTO(typeCode, command.getFieldsByteLength(), writer.toByteArray());
    }

}
