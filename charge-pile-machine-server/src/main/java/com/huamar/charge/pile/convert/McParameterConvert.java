package com.huamar.charge.pile.convert;


import cn.hutool.core.convert.Convert;
import com.huamar.charge.pile.entity.dto.PileParameterReqDTO;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketReader;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 远程参数应答
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Mapper
public interface McParameterConvert {

	McParameterConvert INSTANCE = Mappers.getMapper(McParameterConvert.class);

	/**
	 * 转换数据传输对象
	 * @param dataPacket dataPacket
	 * @return MachineAuthenticationReqDTO
	 */
	default PileParameterReqDTO convert(DataPacket dataPacket){
		DataPacketReader reader = new DataPacketReader(dataPacket.getMsgBody());
		PileParameterReqDTO reqDTO = new PileParameterReqDTO();
		reqDTO.setMsgNumber(reader.readUnsignedShort());
		reqDTO.setMeterType((int) reader.readUnsignedByte());
		reqDTO.setModuleType((int) (reader.readUnsignedByte()));
		reqDTO.setModuleCurrent((int)(reader.readUnsignedByte()));
		reqDTO.setModuleNum((int)(reader.readUnsignedByte()));
		reqDTO.setLockDay((int) reader.readUnsignedByte());
		reqDTO.setGun1Lock((int)(reader.readUnsignedByte()));
		reqDTO.setGun2Lock((int)(reader.readUnsignedByte()));
		reqDTO.setGun3Lock((int)(reader.readUnsignedByte()));
		reqDTO.setGun4Lock((int)(reader.readUnsignedByte()));
		reqDTO.setGun5Lock((int)(reader.readUnsignedByte()));
		reqDTO.setGun6Lock((int)(reader.readUnsignedByte()));
		reqDTO.setGun7Lock((int)(reader.readUnsignedByte()));
		reqDTO.setGun8Lock((int)(reader.readUnsignedByte()));
		reqDTO.setGun9Lock((int)(reader.readUnsignedByte()));
		reqDTO.setGun10Lock((int)(reader.readUnsignedByte()));
		reqDTO.setGun1Limit((int)(reader.readUnsignedByte()));
		reqDTO.setGun2Limit((int)(reader.readUnsignedByte()));
		reqDTO.setGun3Limit((int)(reader.readUnsignedByte()));
		reqDTO.setGun4Limit((int)(reader.readUnsignedByte()));
		reqDTO.setGun5Limit((int)(reader.readUnsignedByte()));
		reqDTO.setGun6Limit((int)(reader.readUnsignedByte()));
		reqDTO.setGun7Limit((int)(reader.readUnsignedByte()));
		reqDTO.setGun8Limit((int)(reader.readUnsignedByte()));
		reqDTO.setGun9Limit((int)(reader.readUnsignedByte()));
		reqDTO.setGun10Limit((int)(reader.readUnsignedByte()));
		reqDTO.setGunNum((int)(reader.readUnsignedByte()));
		reqDTO.setServerType((int)(reader.readUnsignedByte()));

		int scale = (reader.readUnsignedByte());
		reqDTO.setScale(scale / 100D);

		reqDTO.setTrafficTb(reader.readUnsignedShort());
		reqDTO.setTrafficGb(reader.readUnsignedShort());
		reqDTO.setTrafficMb(reader.readUnsignedShort());
		reqDTO.setTrafficKb(reader.readUnsignedShort());
		reqDTO.setVersion(reader.readUnsignedShort());
		reqDTO.setSlowStartMode(reader.readUnsignedShort());
		reqDTO.setSlowDutyCycle(reader.readUnsignedShort());

		int slowSamp = reader.readUnsignedShort();
		reqDTO.setSlowSampling(slowSamp / 100D);
		reqDTO.setLowCurrentEndTime(reader.readUnsignedShort());

		return reqDTO;
	}
}
