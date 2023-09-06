package com.huamar.charge.pile.convert;


import com.huamar.charge.pile.entity.dto.MachineDataUpItem;
import com.huamar.charge.pile.entity.dto.MachineDataUploadReqDTO;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketReader;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;

/**
 * 终端鉴权请求转换器
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Mapper
public interface MachineDataUploadConvert {

	MachineDataUploadConvert INSTANCE = Mappers.getMapper(MachineDataUploadConvert.class);

	/**
	 * 转换数据传输对象
	 * @param dataPacket dataPacket
	 * @return MachineAuthenticationReqDTO
	 */
	default MachineDataUploadReqDTO convert(DataPacket dataPacket){
		DataPacketReader reader = new DataPacketReader(dataPacket.getMsgBody());
		MachineDataUploadReqDTO reqDTO = new MachineDataUploadReqDTO();
		reqDTO.setTime(reader.readBCD());
		reqDTO.setListLen(reader.readByte());
		reqDTO.setDataUnitList(new ArrayList<>());

		for (int i = 0; i < reqDTO.listLen; i++) {
			byte unitId = reader.readByte();
			// 数据项字节长度
			short dataLen = reader.readShort();
			byte[] bytes = reader.readBytes(dataLen);
			reqDTO.getDataUnitList().add(new MachineDataUpItem(unitId, dataLen, bytes));
		}
		return reqDTO;
	}
}
