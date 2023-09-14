package com.huamar.charge.pile.server.service.answer;

import com.huamar.charge.common.protocol.DataPacketWriter;
import com.huamar.charge.common.protocol.PacketBuilder;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.entity.dto.resp.McAuthResp;
import com.huamar.charge.pile.enums.McAnswerEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.server.session.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 设备权健应答
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Service
@RequiredArgsConstructor
public class McAuthAnswerExecute implements McAnswerExecute<McAuthResp> {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McAnswerEnum getCode() {
        return McAnswerEnum.AUTH;
    }


    /**
     * 执行方法
     *
     * @param resp           resp
     * @param channelContext channelContext
     */
    @Override
    public void execute(McAuthResp resp, SessionChannel channelContext) {
        DataPacketWriter writer = this.writer(resp);
        PacketBuilder builder = PacketBuilder.builder();
        builder.body(writer)
                .messageId(ProtocolCodeEnum.AUTH_ANSWER.getCode())
                .messageNumber(SessionManager.getMessageNumber(resp.getIdCode()))
                .idCode(resp.getIdCode());
        SessionManager.writePacket(builder.build(), channelContext);
    }

    /**
     * 封装协议数据
     *
     * @param command command
     * @return DataPacketWriter
     */
    @Override
    public DataPacketWriter writer(McAuthResp command) {
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(command.getStatus());
        writer.write(command.getTime().getData());
        writer.write(command.getEncryptionType());
        if (command.getEncryptionType() != 0) {
            writer.write(command.getSecretKeyLength());
            writer.write(command.getSecretKey());
        }
        return writer;
    }
}

