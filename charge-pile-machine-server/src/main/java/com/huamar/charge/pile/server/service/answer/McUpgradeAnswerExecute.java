package com.huamar.charge.pile.server.service.answer;

import com.huamar.charge.common.protocol.DataPacketWriter;
import com.huamar.charge.common.protocol.PacketBuilder;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.entity.dto.resp.McUpgradeResp;
import com.huamar.charge.pile.enums.McAnswerEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.server.session.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 设备升级应答
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Service
@RequiredArgsConstructor
public class McUpgradeAnswerExecute implements McAnswerExecute<McUpgradeResp> {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McAnswerEnum getCode() {
        return McAnswerEnum.UPGRADE;
    }


    @Override
    public void execute(McUpgradeResp resp, SessionChannel channel) {
        DataPacketWriter writer = this.writer(resp);
        PacketBuilder builder = PacketBuilder.builder()
                .idCode(resp.getIdCode())
                .messageId(ProtocolCodeEnum.UPGRADE.getCode())
                .messageNumber(SessionManager.getMessageNumber(resp.getIdCode()))
                .body(writer);
        SessionManager.writePacket(builder.build(), channel);
    }

    /**
     * 封装协议数据
     *
     * @param command command
     * @return DataPacketWriter
     */
    @Override
    public DataPacketWriter writer(McUpgradeResp command) {
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(command.getUpgradeType());
        writer.write(command.getVersionLength());
        writer.write(command.getVersion());
        writer.write(command.getMode());
        writer.write(command.getUrlLength());
        writer.write(command.getUrl());
        writer.write(command.getPwdStatus());
        writer.write(command.getUserNameLength());
        writer.write(command.getUsername());
        writer.write(command.getUserPwdLength());
        writer.write(command.getPwd());
        writer.write(command.getCrc());
        writer.write(command.getDomainLength());
        writer.write(command.getDomain());
        writer.write(command.getPortLength());
        writer.write(command.getPort());
        return writer;
    }
}

