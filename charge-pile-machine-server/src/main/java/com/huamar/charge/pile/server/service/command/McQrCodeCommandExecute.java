package com.huamar.charge.pile.server.service.command;

import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketWriter;
import com.huamar.charge.pile.entity.dto.command.McCommandDTO;
import com.huamar.charge.pile.entity.dto.command.McQrCodeCommandDTO;
import com.huamar.charge.pile.enums.McCommandEnum;
import com.huamar.charge.pile.server.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 远程控制执行-充电控制
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McQrCodeCommandExecute implements McCommandExecute<McQrCodeCommandDTO> {

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McCommandEnum getCode() {
        return McCommandEnum.QR_CODE;
    }


    /**
     * 执行方法
     *
     * @param command command
     */
    @Override
    public void execute(McQrCodeCommandDTO command) {
        DataPacket packet = this.packet(command);
        Short messageNumber = SessionManager.getMessageNumber(command.getIdCode());
        packet.setMsgNumber(messageNumber);
        boolean sendCommand = SessionManager.writePacket(packet);
        log.info("SLX QrCodeCommand idCode:{} sendCommand:{} msgNumber:{}", command.getIdCode(), sendCommand, messageNumber);
    }

    /**
     * 指令转换
     *
     * @param command McChargeCommandDTO
     */
    @Override
    public McCommandDTO convert(McQrCodeCommandDTO command) {
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(command.getUrlLength());
        writer.write(command.getUrl().getBytes(StandardCharsets.US_ASCII));

        McCommandEnum commandEnum = getCode();
        short typeCode = Short.parseShort(commandEnum.getCode());
        command.setFieldsByteLength((byte) (command.getUrlLength() + 1));
        return new McCommandDTO(typeCode, command.getFieldsByteLength(), writer.toByteArray());
    }

}
