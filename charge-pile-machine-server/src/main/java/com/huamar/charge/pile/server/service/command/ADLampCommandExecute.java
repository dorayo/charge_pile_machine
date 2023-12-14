package com.huamar.charge.pile.server.service.command;

import cn.hutool.core.convert.Convert;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketWriter;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.pile.entity.dto.command.ADLampCommandDTO;
import com.huamar.charge.pile.entity.dto.command.McCommandDTO;
import com.huamar.charge.pile.enums.McCommandEnum;
import com.huamar.charge.pile.enums.McTypeEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 远程控制执行-充电控制
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ADLampCommandExecute implements McCommandExecute<ADLampCommandDTO> {

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McCommandEnum getCode() {
        return McCommandEnum.CUSTOM_AD_LAMP;
    }


    /**
     * 执行方法
     *
     * @param command command
     */
    @Override
    public void execute(ADLampCommandDTO command) {
        DataPacket packet = this.packet(command);
        if(Objects.isNull(packet)){
            return;
        }
        boolean sendCommand = SessionManager.writePacket(packet);
        log.info("QrCodeCommand idCode:{} sendCommand:{} ", command.getIdCode(), sendCommand);
    }

    /**
     * 获取协议体
     *
     * @param command command
     * @return DataPacket
     */
    @Override
    public DataPacket packet(ADLampCommandDTO command) {
        McTypeEnum type = McTypeEnum.A;
        SimpleSessionChannel sessionChannel = (SimpleSessionChannel) SessionManager.get(command.getIdCode());
        if (sessionChannel != null) {
            type = sessionChannel.getType();
        }

        McCommandDTO commandDTO = null;

        DataPacketWriter writer = new DataPacketWriter();
        writer.write(command.getOnHour());
        writer.write(command.getOnMinute());
        writer.write(command.getOffHour());
        writer.write(command.getOffMinute());

        switch (type) {
            case A:
                log.info("A 背光灯下发，未实现");
                break;

            case B:
                short typeCode = Convert.toShort(getCode().getCode());
                commandDTO = new McCommandDTO(typeCode, command.getFieldsByteLength(), writer.toByteArray());

            case C:
                log.info("C 背光灯下发，未实现");
                break;

            default:
                break;
        }

        if(Objects.isNull(commandDTO)){
            return null;
        }


        DataPacket packet= new DataPacket();
        packet.setTag(DataPacket.TAG);
        packet.setMsgId(HexExtUtil.decodeHex(ProtocolCodeEnum.COMMON_SEND.getCode())[0]);
        packet.setIdCode(command.getIdCode().getBytes());
        packet.setMsgBodyAttr(HexExtUtil.decodeHex("00")[0]);
        packet.setTagEnd(DataPacket.TAG);

        DataPacketWriter packetWriter = new DataPacketWriter();
        packetWriter.write(commandDTO.getTypeCode());
        packetWriter.write(commandDTO.getDataLength());
        packetWriter.write(commandDTO.getData());
        byte[] bytes = packetWriter.toByteArray();
        packet.setMsgBodyLen((short) bytes.length);
        packet.setMsgBody(bytes);
        return packet;
    }

    /**
     * 指令转换
     *
     * @param command McChargeCommandDTO
     */
    @Override
    public McCommandDTO convert(ADLampCommandDTO command) {
        return null;
    }

}
