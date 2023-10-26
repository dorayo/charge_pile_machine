package com.huamar.charge.pile.convert;


import com.huamar.charge.pile.entity.dto.fault.PileFaultPutReqDTO;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketReader;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 终端鉴权请求转换器
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Mapper
public interface McFaultConvert {

	McFaultConvert INSTANCE = Mappers.getMapper(McFaultConvert.class);

	/**
	 * 转换数据传输对象
	 * @param dataPacket dataPacket
	 * @return MachineAuthenticationReqDTO
	 */
	default PileFaultPutReqDTO convert(DataPacket dataPacket){
		DataPacketReader reader = new DataPacketReader(dataPacket.getMsgBody());
		PileFaultPutReqDTO reqDTO = new PileFaultPutReqDTO();
		reqDTO.setGunCount(reader.readByte());
		reqDTO.setGunNumber(reader.readByte());
		reqDTO.setStatus(reader.readByte());
		reqDTO.setDescId(reader.readShort());
		reqDTO.setAttributes(reader.readString(32));
		return reqDTO;
	}
}
