package com.huamar.charge.pile.server.service.parameter;

import com.huamar.charge.pile.entity.dto.parameter.McBaseParameterDTO;
import com.huamar.charge.pile.enums.McParameterEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.protocol.DataPacket;
import com.huamar.charge.pile.protocol.DataPacketWriter;
import com.huamar.charge.pile.util.HexExtUtil;

/**
 * 远程控制执行接口
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public interface McParameterExecute<T extends McBaseParameterDTO> {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    McParameterEnum getCode();


    /**
     * 执行方法
     *
     * @param command command
     */
    void execute(T command);


    /**
     * 封装协议数据
     * @param command command
     * @return DataPacketWriter
     */
    default DataPacketWriter writer(T command){
        return null;
    }


    /**
     * 读取参数
     * @param packet packet
     * @return McBaseParameterDTO
     */
    default T reader(DataPacket packet){
        return null;
    }

    /**
     * 封装协议数据
     * @param command command
     * @return DataPacket
     */
    default DataPacket packet(T command){
        DataPacket packet= new DataPacket();
        packet.setTag(DataPacket.TAG);
        packet.setMsgId(ProtocolCodeEnum.PARAMETER_SEND.codeByte());
        packet.setIdCode(command.getIdCode().getBytes());
        packet.setMsgBodyAttr(HexExtUtil.decodeHex("00")[0]);
        packet.setTagEnd(DataPacket.TAG);

        DataPacketWriter writer = this.writer(command);
        byte[] bytes = writer.toByteArray();
        packet.setMsgBodyLen((short) bytes.length);
        packet.setMsgBody(bytes);
        return packet;
    }

}
