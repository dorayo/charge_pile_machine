package com.huamar.charge.pile.convert;


import com.huamar.charge.pile.dto.McCommonReq;
import com.huamar.charge.pile.protocol.DataPacket;
import com.huamar.charge.pile.protocol.DataPacketReader;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 通用请求转换器
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Mapper
public interface McCommonConvert {

	McCommonConvert INSTANCE = Mappers.getMapper(McCommonConvert.class);

	/**
	 * 转换数据传输对象
	 * @param dataPacket dataPacket
	 * @return MachineAuthenticationReqDTO
	 */
	default McCommonReq convert(DataPacket dataPacket){
		DataPacketReader reader = new DataPacketReader(dataPacket.getMsgBody());
		McCommonReq reqDTO = new McCommonReq();
		reqDTO.setMsgId(reader.readByte());
		reqDTO.setMsgNumber(reader.readShort());
		reqDTO.setMsgResult(reader.readShort());
		reqDTO.setTime(reader.readBCD());
		return reqDTO;
	}
}
