package com.huamar.charge.pile.convert;


import com.huamar.charge.pile.entity.dto.event.PileEventReqDTO;
import com.huamar.charge.pile.protocol.DataPacket;
import com.huamar.charge.pile.protocol.DataPacketReader;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 终端鉴权请求转换器
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Mapper
public interface McEventConvert {

	McEventConvert INSTANCE = Mappers.getMapper(McEventConvert.class);

	/**
	 * 转换数据传输对象
	 * @param dataPacket dataPacket
	 * @return MachineAuthenticationReqDTO
	 */
	default PileEventReqDTO convert(DataPacket dataPacket){
		DataPacketReader reader = new DataPacketReader(dataPacket.getMsgBody());
		PileEventReqDTO reqDTO = new PileEventReqDTO();
		reqDTO.setEventState(reader.readByte());
		reqDTO.setEventType(reader.readByte());
		reqDTO.setEventStartTime(reader.readBCD());
		reqDTO.setEventEndTime(reader.readBCD());
		reqDTO.setEventData(reader.readRemainBytes());
		reqDTO.setIdCode(new String(dataPacket.getIdCode()));
		return reqDTO;
	}
}
