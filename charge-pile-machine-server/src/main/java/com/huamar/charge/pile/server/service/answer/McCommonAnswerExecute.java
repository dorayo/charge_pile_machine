package com.huamar.charge.pile.server.service.answer;

import com.huamar.charge.pile.entity.dto.resp.McCommonResp;
import com.huamar.charge.pile.enums.McAnswerEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.protocol.DataPacketBuilder;
import com.huamar.charge.pile.protocol.DataPacketWriter;
import com.huamar.charge.pile.server.service.MachineContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;

/**
 * 设备应答接口
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Service
@RequiredArgsConstructor
public class McCommonAnswerExecute implements McAnswerExecute<McCommonResp> {

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
        return McAnswerEnum.COMMON;
    }


    /**
     * 执行方法
     *
     * @param resp resp
     * @param channelContext channelContext
     */
    @Override
    public void execute(McCommonResp resp, ChannelContext channelContext) {
        DataPacketWriter writer = this.writer(resp);
        DataPacketBuilder builder = DataPacketBuilder.builder(machineContext);
        builder.body(writer)
                .messageId(ProtocolCodeEnum.COMMON_ACK)
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
    public DataPacketWriter writer(McCommonResp command) {
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(command.getMsgId());
        writer.write(command.getMsgNumber());
        writer.write(command.getMsgResult());
        writer.write(command.getTime().getData());
        return writer;
    }
}

