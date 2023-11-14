package com.huamar.charge.pile.server.service.answer;

import com.huamar.charge.common.protocol.DataPacketWriter;
import com.huamar.charge.common.protocol.PacketBuilder;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.entity.dto.resp.McCommonResp;
import com.huamar.charge.pile.enums.McAnswerEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.server.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 设备应答接口
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McCommonAnswerExecute implements McAnswerExecute<McCommonResp> {

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
    public void execute(McCommonResp resp, SessionChannel channelContext) {
        log.info("设备通用应答：idCode:{} resp：{}", resp.getIdCode(), JSONParser.jsonString(resp));
        DataPacketWriter writer = this.writer(resp);
        PacketBuilder builder = PacketBuilder.builder();
        builder.body(writer)
                .idCode(resp.getIdCode())
                .messageId(ProtocolCodeEnum.COMMON_ACK.getCode())
                .messageNumber(resp.getMsgNumber());
        SessionManager.writePacket(builder.build(), channelContext);
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

