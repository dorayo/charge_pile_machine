package com.huamar.charge.pile.server.service.upload;

import com.huamar.charge.common.common.codec.BCD;
import com.huamar.charge.pile.entity.dto.MachineDataUpItem;
import com.huamar.charge.pile.entity.dto.McChargeStageDataDTO;
import com.huamar.charge.pile.enums.McDataUploadEnum;
import com.huamar.charge.common.protocol.DataPacketReader;
import com.huamar.charge.common.util.JSONParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 充电桩 充电阶段数据 One
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Service
@Slf4j
public class McDataUploadStageExecute implements McDataUploadExecute {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McDataUploadEnum getCode() {
        return McDataUploadEnum.COMMON_0X0A;
    }

    /**
     * @param time time
     * @param list list
     */
    @Override
    public void execute(BCD time, List<MachineDataUpItem> list) {
        // TODO 业务实现
        list.forEach( item -> {
            McChargeStageDataDTO parse = this.parse(item);
            log.info("充电桩实时状态信息表 data:{}", JSONParser.jsonString(parse));
        });
    }

    /**
     * 解析对象
     * @param data data
     * @return McChargerOnlineInfoDto
     */
    private McChargeStageDataDTO parse(MachineDataUpItem data){
        DataPacketReader reader = new DataPacketReader(data.getData());
        McChargeStageDataDTO chargeStageDataDTO = new McChargeStageDataDTO();
        chargeStageDataDTO.setBatteryChargeVoltage(reader.readShort());
        chargeStageDataDTO.setBatteryChargeElectricity(reader.readShort());
        chargeStageDataDTO.setElectricityState(reader.readByte());
        chargeStageDataDTO.setMaxVoltage(reader.readShort());
        chargeStageDataDTO.setMaxVoltageNumber(reader.readShort());
        chargeStageDataDTO.setMaxTemperature(reader.readByte());
        chargeStageDataDTO.setMaxTemperatureNumber(reader.readByte());
        chargeStageDataDTO.setMinTemperature(reader.readByte());
        chargeStageDataDTO.setMinTemperatureNumber(reader.readByte());
        chargeStageDataDTO.setRemainChargeTime(reader.readShort());
        chargeStageDataDTO.setPileVoltageOutValue(reader.readShort());
        chargeStageDataDTO.setPileElectricityOutValue(reader.readShort());
        chargeStageDataDTO.setGunSort(reader.readByte());
        return chargeStageDataDTO;
    }

}
