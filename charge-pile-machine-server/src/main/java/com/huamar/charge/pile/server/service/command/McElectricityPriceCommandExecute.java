package com.huamar.charge.pile.server.service.command;

import cn.hutool.core.convert.Convert;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketWriter;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.common.util.netty.NUtils;
import com.huamar.charge.pile.entity.dto.command.McCommandDTO;
import com.huamar.charge.pile.entity.dto.command.McElectricityPriceCommandDTO;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.McCommandEnum;
import com.huamar.charge.pile.enums.McTypeEnum;
import com.huamar.charge.pile.enums.NAttrKeys;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import com.huamar.charge.pile.utils.binaryBuilder.BinaryBuilders;
import com.sun.xml.internal.stream.util.BufferAllocator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 远程控制执行-电价下发
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McElectricityPriceCommandExecute implements McCommandExecute<McElectricityPriceCommandDTO> {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McCommandEnum getCode() {
        return McCommandEnum.ELECTRICITY_PRICE;
    }


    /**
     * 执行方法
     *
     * @param command command
     */
    @Override
    public void execute(McElectricityPriceCommandDTO command) {
        McTypeEnum type = McTypeEnum.A;
        SimpleSessionChannel sessionChannel = (SimpleSessionChannel) SessionManager.get(command.getIdCode());
        if (sessionChannel != null) {
            type = sessionChannel.getType();
        }
        if (type == McTypeEnum.C) {
            byte[] timeStages = command.getTimeStage().toString().getBytes();
            for (int i = timeStages.length - 1; i >= 0; i--) {
                timeStages[i] = (byte) (timeStages[i] - 0x31);
            }
            ProtocolCPacket packet = sessionChannel.channel().attr(NAttrKeys.PROTOCOL_C_0x09_PACKET).get();
            ByteBuf responseBody = ByteBufAllocator.DEFAULT.heapBuffer(59);
            responseBody.writeBytes(packet.getIdBody());
            responseBody.writeByte(0x01);
            responseBody.writeByte(0x00);
            responseBody.writeByte(command.getPrice1());
            responseBody.writeByte(command.getServicePrice1());
            responseBody.writeByte(command.getPrice2());
            responseBody.writeByte(command.getServicePrice2());
            responseBody.writeByte(command.getPrice3());
            responseBody.writeByte(command.getServicePrice3());
            responseBody.writeByte(command.getPrice4());
            responseBody.writeByte(command.getServicePrice4());
            responseBody.writeByte(command.getServicePrice4());
            responseBody.writeByte(0x00);
            responseBody.writeBytes(timeStages);
            ByteBuf response = BinaryBuilders.protocolCLeResponseBuilder(NUtils.nBFToBf(responseBody), packet.getOrderVBf(), (byte) 0x0A);
            responseBody.release();
            sessionChannel.channel().writeAndFlush(response).addListener((f) -> {
                if (f.isSuccess()) {
                    log.info("write 0x0A success ");
                } else {
                    f.cause().printStackTrace();
                    log.info("write 0x0A error ");
                }
                response.release();
            });
            return;
        }
        DataPacket packet = this.packet(command);
        boolean sendCommand = SessionManager.writePacket(packet);
        log.info("Electricity Price idCode:{} sendCommand:{} msgId:{} ", command.getIdCode(), sendCommand,packet.getMsgNumber());
    }


    /**
     * 指令转换
     *
     * @param command McChargeCommandDTO
     */
    @Override
    public McCommandDTO convert(McElectricityPriceCommandDTO command) {
        McTypeEnum type = McTypeEnum.A;
        SimpleSessionChannel sessionChannel = (SimpleSessionChannel) SessionManager.get(command.getIdCode());
        if (sessionChannel != null) {
            type = sessionChannel.getType();
        }

        short typeCode = Convert.toShort(getCode().getCode());
        DataPacketWriter writer = new DataPacketWriter();
        //noinspection SwitchStatementWithTooFewBranches
        log.info("电价信息:{}", JSONParser.jsonString(command));
        switch (type) {
            case B:
                writer.write(command.getGunSort());
                writer.write(command.getPrice1());
                writer.write(command.getPrice2());
                writer.write(command.getPrice3());
                writer.write(command.getPrice4());
                writer.write(command.getTimeStage());
                writer.write(command.getServicePrice1());

                break;
            default:
                writer.write(command.getGunSort());
                writer.write(command.getPrice1());
                writer.write(command.getPrice2());
                writer.write(command.getPrice3());
                writer.write(command.getPrice4());
                writer.write(command.getPrice5());
                writer.write(command.getPrice6());
                writer.write(command.getServicePrice1());
                writer.write(command.getServicePrice2());
                writer.write(command.getServicePrice3());
                writer.write(command.getServicePrice4());
                writer.write(command.getServicePrice5());
                writer.write(command.getServicePrice6());
                writer.write(command.getTimeStage());
        }
        McCommandDTO commandDTO = new McCommandDTO(typeCode, command.getFieldsByteLength(), writer.toByteArray());
        log.info("McCommandDTO:{}", JSONParser.jsonString(commandDTO));
        return commandDTO;
    }

}
