package com.huamar.charge.pile.server.service.parameter;

import com.huamar.charge.common.common.BCDUtils;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketWriter;
import com.huamar.charge.pile.entity.dto.parameter.McParameterReadDTO;
import com.huamar.charge.pile.enums.McParameterEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.server.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 远程控制执行下发
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McParameterReadSendExecute implements McParameterExecute<McParameterReadDTO> {



    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McParameterEnum getCode() {
        return McParameterEnum.READ;
    }

    /**
     * 执行方法
     *
     * @param command command
     */
    @Override
    public void execute(McParameterReadDTO command) {
        DataPacket packet = this.packet(command);
        packet.setMsgId(ProtocolCodeEnum.PARAMETER_READ_SEND.codeByte());
        boolean sendCommand = SessionManager.writePacket(packet);
        log.info("Parameter Read idCode:{} sendCommand:{} ", command.getIdCode(), sendCommand);
    }


    /**
     * 写入协议数据
     *
     * @param command command
     * @return DataPacketWriter
     */
    @Override
    public DataPacketWriter writer(McParameterReadDTO command) {
        command.setTime(BCDUtils.bcdTime());
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(command.getTime().getData());
        return writer;
    }
}
