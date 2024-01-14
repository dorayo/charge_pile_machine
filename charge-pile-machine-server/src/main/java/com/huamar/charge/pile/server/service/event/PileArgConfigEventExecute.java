package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.pile.convert.PileChargeFinishEventConvert;
import com.huamar.charge.pile.entity.dto.MachineDataUpItem;
import com.huamar.charge.pile.entity.dto.event.PileChargeArgConfigDTO;
import com.huamar.charge.pile.entity.dto.event.PileChargeFinishEventDTO;
import com.huamar.charge.pile.entity.dto.event.PileEventReqDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.event.PileChargeArgConfigPushDTO;
import com.huamar.charge.pile.entity.dto.platform.event.PileChargeFinishEventPushDTO;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.enums.PileEventEnum;
import com.huamar.charge.common.protocol.DataPacketReader;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import com.huamar.charge.common.util.JSONParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 设备端数据汇报接口-充电参数配置信息
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PileArgConfigEventExecute implements PileEventExecute {

    private final PileMessageProduce messageProduce;


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public PileEventEnum getCode() {
        return PileEventEnum.CONFIG_EVENT;
    }

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void execute(PileEventReqDTO reqDTO) {
        log.info("事件汇报：{}", getCode().getDesc());

        PileChargeArgConfigDTO argConfigDTO = this.parse(reqDTO);
        log.info("事件汇报：{}, data:{}", getCode().getDesc(), JSONParser.jsonString(argConfigDTO));

        PileChargeArgConfigPushDTO pushDTO = new PileChargeArgConfigPushDTO();
        BeanUtils.copyProperties(argConfigDTO, pushDTO);
        pushDTO.setIdCode(reqDTO.getIdCode());
        pushDTO.setEventState((int) reqDTO.getEventState());
        pushDTO.setEventType((int) reqDTO.getEventType());
        pushDTO.setEventStartTime(reqDTO.getEventStartTime().toString());
        pushDTO.setEventEndTime(reqDTO.getEventEndTime().toString());
        pushDTO.setSynTime(argConfigDTO.getSynTime().toString());

        MessageData<PileChargeArgConfigPushDTO> messageData = new MessageData<>(pushDTO);
        messageData.setBusinessCode(MessageCodeEnum.EVENT_CONFIG_EVENT.getCode());
        messageProduce.send(messageData);

    }

    /**
     * 解析元数据
     *
     * @param reqDTO reqDTO
     * @return McEventBaseDTO
     */
    @Override
    public PileChargeArgConfigDTO parse(PileEventReqDTO reqDTO) {
        PileChargeArgConfigDTO pileChargeArgConfigDTO = new PileChargeArgConfigDTO();
        DataPacketReader reader = new DataPacketReader(reqDTO.getEventData());
        pileChargeArgConfigDTO.setMaxValidVoltage(reader.readUnsignedShort());
        pileChargeArgConfigDTO.setMaxValidElectricity(reader.readUnsignedShort());
        pileChargeArgConfigDTO.setMaxTotalPower(reader.readInt());
        pileChargeArgConfigDTO.setMaxValidTotalVoltage(reader.readUnsignedShort());
        pileChargeArgConfigDTO.setMaxValidTemperature(reader.readByte());
        pileChargeArgConfigDTO.setCarElectricityState(reader.readByte());
        pileChargeArgConfigDTO.setCarTotalVoltage(reader.readUnsignedShort());
        pileChargeArgConfigDTO.setSynTime(reader.readBCD());
        pileChargeArgConfigDTO.setChargerMaxOutVoltage(reader.readUnsignedShort());
        pileChargeArgConfigDTO.setChargerMinOutVoltage(reader.readUnsignedShort());
        pileChargeArgConfigDTO.setChargerMaxOutElectricity(reader.readUnsignedShort());
        pileChargeArgConfigDTO.setBmsReadyState(reader.readByte());
        pileChargeArgConfigDTO.setChargerOutReadyState(reader.readByte());
        pileChargeArgConfigDTO.setGunSort(reader.readByte());
        return pileChargeArgConfigDTO;
    }

}
