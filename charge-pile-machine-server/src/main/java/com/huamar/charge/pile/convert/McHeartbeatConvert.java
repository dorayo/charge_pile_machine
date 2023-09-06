package com.huamar.charge.pile.convert;


import com.huamar.charge.pile.entity.dto.fault.McHeartbeatReqDTO;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketReader;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 设备心跳 转换层
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Mapper
public interface McHeartbeatConvert {

	McHeartbeatConvert INSTANCE = Mappers.getMapper(McHeartbeatConvert.class);

	/**
	 * 转换数据传输对象
	 * @param dataPacket dataPacket
	 * @return MachineAuthenticationReqDTO
	 */
	default McHeartbeatReqDTO convert(DataPacket dataPacket){
		DataPacketReader reader = new DataPacketReader(dataPacket.getMsgBody());
		McHeartbeatReqDTO reqDTO = new McHeartbeatReqDTO();
		reqDTO.setTime(reader.readBCD());
		reqDTO.setProtocolNumber(reader.readByte());
		reqDTO.setRetain1(reader.readByte());
		reqDTO.setRetain2(reader.readByte());
		reqDTO.setRetain3(reader.readByte());
		return reqDTO;
	}
}
