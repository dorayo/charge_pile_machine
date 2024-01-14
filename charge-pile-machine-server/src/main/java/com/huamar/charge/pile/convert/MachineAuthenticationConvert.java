package com.huamar.charge.pile.convert;


import com.huamar.charge.pile.entity.dto.MachineAuthenticationReqDTO;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketReader;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.HashMap;

/**
 * 终端鉴权请求转换器
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Mapper
public interface MachineAuthenticationConvert {

	MachineAuthenticationConvert INSTANCE = Mappers.getMapper(MachineAuthenticationConvert.class);

	/**
	 * 转换数据传输对象
	 * @param dataPacket dataPacket
	 * @return MachineAuthenticationReqDTO
	 */
	default MachineAuthenticationReqDTO convert(DataPacket dataPacket){
		DataPacketReader reader = new DataPacketReader(dataPacket.getMsgBody());
		MachineAuthenticationReqDTO reqDTO = new MachineAuthenticationReqDTO();
		reqDTO.setLoginNumber(reader.readUnsignedShort());
		reqDTO.setTerminalTime(reader.readBCD());
		reqDTO.setMacAddress(reader.readBCD());
		reqDTO.setBoardNum(reader.readByte());
		reqDTO.setProgramVersionLen(reader.readByte());
		reqDTO.setBoardVersionMaps(new HashMap<>());
//		String programVersionNum = reader.readString(reqDTO.getProgramVersionLen()).trim();
//		reqDTO.getBoardVersionMaps().put(1, programVersionNum);
		reqDTO.setLongitude((reader.readInt()) / 100000D);
		reqDTO.setLatitude((reader.readInt()) / 100000D);
		reqDTO.setIdCode(new String(dataPacket.getIdCode()));
		return reqDTO;
	}
}
