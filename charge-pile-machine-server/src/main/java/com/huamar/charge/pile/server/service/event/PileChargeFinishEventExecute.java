package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.pile.convert.PileChargeFinishEventConvert;
import com.huamar.charge.pile.entity.dto.MachineDataUpItem;
import com.huamar.charge.pile.entity.dto.command.McElectricityPriceCommandDTO;
import com.huamar.charge.pile.entity.dto.event.PileChargeFinishEventDTO;
import com.huamar.charge.pile.entity.dto.event.PileEventReqDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.event.PileChargeFinishEventPushDTO;
import com.huamar.charge.pile.enums.CacheKeyEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.enums.PileEventEnum;
import com.huamar.charge.common.protocol.DataPacketReader;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 设备端数据汇报接口-充电结束统计
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PileChargeFinishEventExecute implements PileEventExecute {


    private final PileMessageProduce messageProduce;

    private final RedissonClient redissonClient;

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


        PileChargeFinishEventPushDTO eventPushDTO = PileChargeFinishEventConvert.INSTANCE.convert(eventDTO);
        PileChargeFinishEventConvert.INSTANCE.copyBaseField(eventPushDTO, reqDTO);

        MessageData<PileChargeFinishEventPushDTO> messageData = new MessageData<>(eventPushDTO);
        messageData.setBusinessCode(MessageCodeEnum.EVENT_CHARGE_FINISH.getCode());
        messageData.setBusinessId(reqDTO.getIdCode());
        messageProduce.send(messageData);
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
        eventDTO.setCarIdentificationCode(reader.readString(17));
        eventDTO.setOrderSerialNumber(reader.readString(32));
        eventDTO.setEndReason(reader.readShort());
        //判断是否还有未读完数据，兼容不同版本协议
        if(!reader.isEnd()){
            eventDTO.setStartSoc(reader.readByte());
        }
        return eventDTO;
    }

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    public void execute(MachineDataUpItem reqDTO) {
        log.info("事件汇报：{}", getCode().getDesc());
        PileChargeFinishEventDTO eventDTO = this.parse(reqDTO);

        log.info("事件汇报：{}, data:{}", getCode().getDesc(), JSONParser.jsonString(eventDTO));


        PileChargeFinishEventPushDTO eventPushDTO = PileChargeFinishEventConvert.INSTANCE.convert(eventDTO);
        //计算国花服务费
        CacheKeyEnum keyEnum = CacheKeyEnum.MACHINE_SERVICE_PRICE;
        String key = reqDTO.getIdCode();
        key = keyEnum.joinKey(key);
        RBucket<McElectricityPriceCommandDTO> bucket = redissonClient.getBucket(key);
        McElectricityPriceCommandDTO mcElectricityPriceCommandDTO = bucket.get();
        if(mcElectricityPriceCommandDTO != null){
            int monery = (int)(mcElectricityPriceCommandDTO.getServicePrice1()/10000*(eventDTO.getOutPower()));
            eventPushDTO.setServiceMoney(monery);
            eventPushDTO.setChargeMoney(eventPushDTO.getChargeMoney()-monery);
        }

        PileEventReqDTO reqDTOTemp = new  PileEventReqDTO();
        reqDTOTemp.setIdCode(reqDTO.getIdCode());
        reqDTOTemp.setEventStartTime(eventDTO.getStartTime());
        reqDTOTemp.setEventEndTime(eventDTO.getEndTime());
        reqDTOTemp.setEventState((byte)2);
        PileChargeFinishEventConvert.INSTANCE.copyBaseField(eventPushDTO, reqDTOTemp);

        MessageData<PileChargeFinishEventPushDTO> messageData = new MessageData<>(eventPushDTO);
        messageData.setBusinessCode(MessageCodeEnum.EVENT_CHARGE_FINISH.getCode());
        messageData.setBusinessId(reqDTO.getIdCode());
        messageProduce.send(messageData);
    }

    /**
     * 解析元数据
     *
     * @param reqDTO reqDTO
     * @return McEventBaseDTO
     */
    private PileChargeFinishEventDTO parse(MachineDataUpItem reqDTO) {
        DataPacketReader reader = new DataPacketReader(reqDTO.getData());
        PileChargeFinishEventDTO eventDTO = new PileChargeFinishEventDTO();
        eventDTO.setGunSort(reader.readByte());
        eventDTO.setOutPower(reader.readInt());
        eventDTO.setChargeMoney(reader.readInt());
        eventDTO.setEndReason(reader.readShort());
        eventDTO.setStartTime(reader.readBCD());
        eventDTO.setEndTime(reader.readBCD());
        eventDTO.setCumulativeChargeTime(reader.readInt());
        reader.readByte();
        reader.readString(4);
        eventDTO.setOrderSerialNumber(reader.readString(32));
        reader.readByte();
        reader.readString(4);
        eventDTO.setOrderSerialNumber(reader.readString(17));

        //判断是否还有未读完数据，兼容不同版本协议
        if(!reader.isEnd()){
            eventDTO.setStartSoc(reader.readByte());
        }
        return eventDTO;
    }

}
