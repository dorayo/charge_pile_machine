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
		reqDTO.setMsgNumber(reader.readShort());
		reqDTO.setMeterType(Convert.toInt(reader.readByte()));
		reqDTO.setModuleType(Convert.toInt(reader.readByte()));
		reqDTO.setModuleCurrent(Convert.toInt(reader.readByte()));
		reqDTO.setModuleNum(Convert.toInt(reader.readByte()));
		reqDTO.setLockDay(Convert.toInt(reader.readShort()));
		reqDTO.setGun1Lock(Convert.toInt(reader.readByte()));
		reqDTO.setGun2Lock(Convert.toInt(reader.readByte()));
		reqDTO.setGun3Lock(Convert.toInt(reader.readByte()));
		reqDTO.setGun4Lock(Convert.toInt(reader.readByte()));
		reqDTO.setGun5Lock(Convert.toInt(reader.readByte()));
		reqDTO.setGun6Lock(Convert.toInt(reader.readByte()));
		reqDTO.setGun7Lock(Convert.toInt(reader.readByte()));
		reqDTO.setGun8Lock(Convert.toInt(reader.readByte()));
		reqDTO.setGun9Lock(Convert.toInt(reader.readByte()));
		reqDTO.setGun10Lock(Convert.toInt(reader.readByte()));
		reqDTO.setGun1Limit(Convert.toInt(reader.readByte()));
		reqDTO.setGun2Limit(Convert.toInt(reader.readByte()));
		reqDTO.setGun3Limit(Convert.toInt(reader.readByte()));
		reqDTO.setGun4Limit(Convert.toInt(reader.readByte()));
		reqDTO.setGun5Limit(Convert.toInt(reader.readByte()));
		reqDTO.setGun6Limit(Convert.toInt(reader.readByte()));
		reqDTO.setGun7Limit(Convert.toInt(reader.readByte()));
		reqDTO.setGun8Limit(Convert.toInt(reader.readByte()));
		reqDTO.setGun9Limit(Convert.toInt(reader.readByte()));
		reqDTO.setGun10Limit(Convert.toInt(reader.readByte()));
		reqDTO.setGunNum(Convert.toInt(reader.readByte()));
		reqDTO.setServerType(Convert.toInt(reader.readByte()));

		Integer scale = Convert.toInt(reader.readByte());
		reqDTO.setScale((scale == null ? 0.0 : scale) / 100D);

		reqDTO.setTrafficTb(Convert.toInt(reader.readShort()));
		reqDTO.setTrafficGb(Convert.toInt(reader.readShort()));
		reqDTO.setTrafficMb(Convert.toInt(reader.readShort()));
		reqDTO.setTrafficKb(Convert.toInt(reader.readShort()));
		reqDTO.setVersion(Convert.toInt(reader.readByte()));
		reqDTO.setSlowStartMode(Convert.toInt(reader.readByte()));
		reqDTO.setSlowDutyCycle(Convert.toInt(reader.readByte()));

		Integer slowSamp = Convert.toInt(reader.readShort());
		reqDTO.setSlowSampling((slowSamp == null ? 0.0 : slowSamp) / 100D);
		reqDTO.setLowCurrentEndTime(Convert.toInt(reader.readShort()));

		return reqDTO;
	}
}
