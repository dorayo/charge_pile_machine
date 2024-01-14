package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.pile.convert.PileHandshakeEventConvert;
import com.huamar.charge.pile.entity.dto.event.PileEventReqDTO;
import com.huamar.charge.pile.entity.dto.event.PileHandshakeEventDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.event.PileHandshakeEventPushDTO;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.enums.PileEventEnum;
import com.huamar.charge.common.protocol.DataPacketReader;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PileHandshakeEventExecute implements PileEventExecute {


    private final PileMessageProduce messageProduce;

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public PileEventEnum getCode() {
        return PileEventEnum.HAND_SHAKE;
    }

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void execute(PileEventReqDTO reqDTO) {
        log.info("事件汇报：{}", getCode().getDesc());
        PileHandshakeEventDTO eventDTO = this.parse(reqDTO);
        log.info("事件汇报：{}, data:{}", getCode().getDesc(), JSONParser.jsonString(eventDTO));


        PileHandshakeEventPushDTO eventPushDTO = PileHandshakeEventConvert.INSTANCE.convert(eventDTO);
        PileHandshakeEventConvert.INSTANCE.copyBaseField(eventPushDTO, reqDTO);

        MessageData<PileHandshakeEventPushDTO> messageData = new MessageData<>(eventPushDTO);
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
    public PileHandshakeEventDTO parse(PileEventReqDTO reqDTO) {
        DataPacketReader reader = new DataPacketReader(reqDTO.getEventData());
        PileHandshakeEventDTO eventDTO = new PileHandshakeEventDTO();
        eventDTO.setGunSort(reader.readByte());
        eventDTO.setDistinguishResult(reader.readByte());
        eventDTO.setBmsProtocolVersion(HexExtUtil.encodeHexStr(reader.readBytes(3)));
        eventDTO.setBatteryType(reader.readByte());
        eventDTO.setCarRatedCapacity(reader.readInt());
        eventDTO.setCarRatedVoltage(reader.readUnsignedShort());
        eventDTO.setBatteryCellVoltage(reader.readUnsignedShort());
        eventDTO.setBatteryCellElectricity(reader.readUnsignedShort());
        eventDTO.setBatterySerialNumber(reader.readUnsignedShort());
        eventDTO.setBatteryProductionDate(reader.readBCD3());
        eventDTO.setBatteryChargeCount(reader.readInt());
        eventDTO.setBatteryTag(reader.readByte());
        eventDTO.setCarIdentificationCode(HexExtUtil.encodeHexStr(reader.readBytes(17)));
        return eventDTO;
    }


}
