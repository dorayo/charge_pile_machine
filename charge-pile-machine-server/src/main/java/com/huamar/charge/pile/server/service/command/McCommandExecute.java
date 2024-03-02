package com.huamar.charge.pile.server.service.command;

import com.huamar.charge.pile.entity.dto.command.McBaseCommandDTO;
import com.huamar.charge.pile.entity.dto.command.McCommandDTO;
import com.huamar.charge.pile.enums.McCommandEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketWriter;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.pile.server.session.SessionManager;

/**
 * 远程控制执行接口
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public interface McCommandExecute<T extends McBaseCommandDTO> {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    McCommandEnum getCode();


    /**
     * 执行方法
     *
     * @param command command
     */
    void execute(T command);

    /**
     * 指令转换
     */
    McCommandDTO convert(T command);


    /**
     * 获取协议体
     * @param command command
     * @return DataPacket
     */
    default DataPacket packet(T command) {


        Short messageNumber = SessionManager.getMessageNumber(command.getIdCode());
        command.headMessageNum(messageNumber);

        //noinspection DuplicatedCode
        DataPacket packet = new DataPacket();
        packet.setTag(DataPacket.TAG);
        packet.setMsgId(HexExtUtil.decodeHex(ProtocolCodeEnum.COMMON_SEND.getCode())[0]);
        packet.setIdCode(command.getIdCode().getBytes());
        packet.setMsgBodyAttr(HexExtUtil.decodeHex("00")[0]);
        packet.setTagEnd(DataPacket.TAG);

        //noinspection DuplicatedCode
        McCommandDTO mcCommandDTO = this.convert(command);
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(mcCommandDTO.getTypeCode());
        writer.write(mcCommandDTO.getDataLength());
        writer.write(mcCommandDTO.getData());
        byte[] bytes = writer.toByteArray();
        packet.setMsgBodyLen((short) bytes.length);
        packet.setMsgBody(bytes);
        return packet;
    }

}
