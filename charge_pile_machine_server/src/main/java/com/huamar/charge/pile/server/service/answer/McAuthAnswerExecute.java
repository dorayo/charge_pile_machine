package com.huamar.charge.pile.server.service.answer;

import com.huamar.charge.pile.dto.resp.McAuthResp;
import com.huamar.charge.pile.enums.McAnswerEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.protocol.DataPacketBuilder;
import com.huamar.charge.pile.protocol.DataPacketWriter;
import com.huamar.charge.pile.server.service.MachineContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;

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
     * 设备终端上下文
     */
    private final MachineContext machineContext;

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
     * @param resp resp
     * @param channelContext channelContext
     */
    @Override
    public void execute(McAuthResp resp, ChannelContext channelContext) {
        DataPacketWriter writer = this.writer(resp);
        DataPacketBuilder builder = DataPacketBuilder.builder(machineContext);
        builder.body(writer)
                .messageId(ProtocolCodeEnum.AUTH_ANSWER)
                .idCode(resp.getIdCode());
        machineContext.answer(builder.build(), channelContext);
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

