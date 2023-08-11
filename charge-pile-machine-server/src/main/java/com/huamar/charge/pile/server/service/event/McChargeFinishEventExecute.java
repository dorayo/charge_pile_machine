package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.pile.entity.dto.McChargeFinishEventDTO;
import com.huamar.charge.pile.entity.dto.McEventReqDTO;
import com.huamar.charge.pile.enums.McEventEnum;
import com.huamar.charge.pile.protocol.DataPacketReader;
import org.springframework.stereotype.Component;

/**
 * 设备端数据汇报接口-充电握手事件
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
public class McChargeFinishEventExecute implements McEventExecute{


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McEventEnum getCode() {
        return McEventEnum.CHARGE_FINISH;
    }

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void execute(McEventReqDTO reqDTO) {
        //TODO 业务实现
        McChargeFinishEventDTO eventDTO = this.parse(reqDTO);
    }

    /**
     * 解析元数据
     *
     * @param reqDTO reqDTO
     * @return McEventBaseDTO
     */
    @Override
    public McChargeFinishEventDTO parse(McEventReqDTO reqDTO) {
        DataPacketReader reader = new DataPacketReader(reqDTO.getEventData());
        McChargeFinishEventDTO eventDTO = new McChargeFinishEventDTO();
        eventDTO.setTerminationElectricityState(reader.readByte());
        eventDTO.setBatteryMinVoltage(reader.readShort());
        eventDTO.setBatteryMaxVoltage(reader.readShort());
        eventDTO.setBatteryMinTemperature(reader.readByte());
        eventDTO.setBatteryMaxTemperature(reader.readByte());
        eventDTO.setStartTime(reader.readBCD());
        eventDTO.setCumulativeChargeTime(reader.readInt());
        eventDTO.setOutPower(reader.readInt());
        eventDTO.setGunSort(reader.readByte());
        eventDTO.setChargeMoney(reader.readInt());
        eventDTO.setServiceMoney(reader.readInt());
        eventDTO.setCarIdentificationCode(reader.readFixLenString(17));
        eventDTO.setOrderSerialNumber(reader.readFixLenString(32));
        eventDTO.setEndReason(reader.readShort());
        //判断是否还有未读完数据，兼容不同版本协议
        if(!reader.isEnd()){
            eventDTO.setStartSoc(reader.readByte());
        }
        return eventDTO;
    }


}
