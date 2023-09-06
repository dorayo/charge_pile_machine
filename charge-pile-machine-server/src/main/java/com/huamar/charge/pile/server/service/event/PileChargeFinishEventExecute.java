package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.pile.entity.dto.event.PileChargeFinishEventDTO;
import com.huamar.charge.pile.entity.dto.event.PileEventReqDTO;
import com.huamar.charge.pile.enums.PileEventEnum;
import com.huamar.charge.common.protocol.DataPacketReader;
import com.huamar.charge.common.util.JSONParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备端数据汇报接口-充电握手事件
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
public class PileChargeFinishEventExecute implements PileEventExecute {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public PileEventEnum getCode() {
        return PileEventEnum.CHARGE_FINISH;
    }

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void execute(PileEventReqDTO reqDTO) {
        log.info("事件汇报：{}", getCode().getDesc());
        PileChargeFinishEventDTO eventDTO = this.parse(reqDTO);
        log.info("事件汇报：{}, data:{}", getCode().getDesc(), JSONParser.jsonString(eventDTO));
    }

    /**
     * 解析元数据
     *
     * @param reqDTO reqDTO
     * @return McEventBaseDTO
     */
    @Override
    public PileChargeFinishEventDTO parse(PileEventReqDTO reqDTO) {
        DataPacketReader reader = new DataPacketReader(reqDTO.getEventData());
        PileChargeFinishEventDTO eventDTO = new PileChargeFinishEventDTO();
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
