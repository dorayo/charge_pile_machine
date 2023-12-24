package com.huamar.charge.pile.server.service.event;

import com.alibaba.fastjson.JSONObject;
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
 * @author TiAmo(13721682347 @ 163.com)
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
        if (!reader.isEnd()) {
            eventDTO.setStartSoc(reader.readByte());
        }

        JSONObject jsonObject = new JSONObject();

        switch (eventDTO.getEndReason()) {
            case 0x00:
                jsonObject.put("code", 0);
                jsonObject.put("desc","未记录原因");
                jsonObject.put("tag","SLX");

                break;

            case 0x01:
                jsonObject.put("code", 1);
                jsonObject.put("desc","设备监控界面操作");
                jsonObject.put("tag","SLX");
                break;

            case 0x02:
                jsonObject.put("code", 2);
                jsonObject.put("desc","设备授权到期限制充电");
                jsonObject.put("tag","SLX");
                break;

            case 0x03:
                jsonObject.put("code", 3);
                jsonObject.put("desc","本地刷卡关机，不联网状态下使用，不计费");
                jsonObject.put("tag","SLX");
                break;

            case 0x04:
                jsonObject.put("code", 4);
                jsonObject.put("desc","云刷卡关机，连接硕立信平台使用");
                jsonObject.put("tag","SLX");
                break;

            case 0x05:
                jsonObject.put("code", 5);
                jsonObject.put("desc","设备急停按钮按下关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x06:
                jsonObject.put("code", 6);
                jsonObject.put("desc","CC1 监测点电压变化导致关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x0D:
                jsonObject.put("code", 13);
                jsonObject.put("desc","扫码关机和平台关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x0E:
                jsonObject.put("code", 14);
                jsonObject.put("desc","双枪并冲模式下关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x0F:
                jsonObject.put("code", 15);
                jsonObject.put("desc","控制器发生故障");
                jsonObject.put("tag","SLX");
                break;

            case 0x10:
                jsonObject.put("code", 16);
                jsonObject.put("desc","电子锁故障关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x11:
                jsonObject.put("code", 17);
                jsonObject.put("desc","绝缘检测不通过关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x12:
                jsonObject.put("code", 18);
                jsonObject.put("desc","泄放电路故障导致关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x13:
                jsonObject.put("code", 19);
                jsonObject.put("desc","充电机温度过高导致关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x14:
                jsonObject.put("code", 20);
                jsonObject.put("desc","充电机内部温度过高导致关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x15:
                jsonObject.put("code", 21);
                jsonObject.put("desc","接触器外端电压>10V关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x16:
                jsonObject.put("code", 22);
                jsonObject.put("desc","充电连接器（直流接触器）故障");
                jsonObject.put("tag","SLX");
                break;

            case 0x17:
                jsonObject.put("code", 23);
                jsonObject.put("desc","电池电压与报文电压相差正负5%");
                jsonObject.put("tag","SLX");
                break;

            case 0x18:
                jsonObject.put("code", 24);
                jsonObject.put("desc","规定时间内预约未完成（多数与预约相关模块发生故障）%");
                jsonObject.put("tag","SLX");
                break;

            case 0x19:
                jsonObject.put("code", 25);
                jsonObject.put("desc","所需电量不能传送");
                jsonObject.put("tag","SLX");
                break;

            case 0x1A:
                jsonObject.put("code", 26);
                jsonObject.put("desc","充电中检测输出电流与BCS报文中电流是否匹配");
                jsonObject.put("tag","SLX");
                break;

            case 0x1B:
                jsonObject.put("code", 27);
                jsonObject.put("desc","充电中检测输出电压与BCS报文中电压是否匹配");
                jsonObject.put("tag","SLX");
                break;

            case 0x1C:
                jsonObject.put("code", 28);
                jsonObject.put("desc","BRM报文超时导致关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x1D:
                jsonObject.put("code", 29);
                jsonObject.put("desc","BCP报文超时导致关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x1E:
                jsonObject.put("code", 30);
                jsonObject.put("desc","BRO报文超时导致关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x1F:
                jsonObject.put("code", 31);
                jsonObject.put("desc","BCL报文超时导致关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x20:
                jsonObject.put("code", 32);
                jsonObject.put("desc","BCS报文超时导致关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x21:
                jsonObject.put("code", 33);
                jsonObject.put("desc","BMS到达目标值SOC关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x22:
                jsonObject.put("code", 34);
                jsonObject.put("desc","BMS到达总电压设定关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x23:
                jsonObject.put("code", 35);
                jsonObject.put("desc","BMS到达单体总电压设定关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x24:
                jsonObject.put("code", 36);
                jsonObject.put("desc","BMS充电中检测到绝缘故障关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x25:
                jsonObject.put("code", 37);
                jsonObject.put("desc","BMS输出连接器过温关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x26:
                jsonObject.put("code", 38);
                jsonObject.put("desc","BMS元件过温故障关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x27:
                jsonObject.put("code", 39);
                jsonObject.put("desc","BMS充电连接器故障关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x28:
                jsonObject.put("code", 40);
                jsonObject.put("desc","BMS电池组过温故障关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x29:
                jsonObject.put("code", 41);
                jsonObject.put("desc","BMS高温继电器故障关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x2A:
                jsonObject.put("code", 42);
                jsonObject.put("desc","BMS检查点2故障关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x2B:
                jsonObject.put("code", 43);
                jsonObject.put("desc","BMS检测输出电流异常关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x2C:
                jsonObject.put("code", 44);
                jsonObject.put("desc","BMS检测输出电压异常关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x2D:
                jsonObject.put("code", 45);
                jsonObject.put("desc","余额不足关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x2E:
                jsonObject.put("code", 46);
                jsonObject.put("desc","达到设定电量");
                jsonObject.put("tag","SLX");
                break;

            case 0x2F:
                jsonObject.put("code", 47);
                jsonObject.put("desc","达到设定时间");
                jsonObject.put("tag","SLX");
                break;

            case 0x30:
                jsonObject.put("code", 48);
                jsonObject.put("desc","达到设定SOC");
                jsonObject.put("tag","SLX");
                break;

            case 0x31:
                jsonObject.put("code", 49);
                jsonObject.put("desc","达到设定金额");
                jsonObject.put("tag","SLX");
                break;

            case 0x32:
                jsonObject.put("code", 50);
                jsonObject.put("desc","枪状态错误");
                jsonObject.put("tag","SLX");
                break;

            case 0x33:
                jsonObject.put("code", 51);
                jsonObject.put("desc","交流桩：状态异常");
                jsonObject.put("tag","SLX");
                break;

            case 0x34:
                jsonObject.put("code", 52);
                jsonObject.put("desc","交流桩：用户拔枪");
                jsonObject.put("tag","SLX");
                break;

            case 0x35:
                jsonObject.put("code", 53);
                jsonObject.put("desc","交流桩：输出电流低");
                jsonObject.put("tag","SLX");
                break;

            case 0x36:
                jsonObject.put("code", 54);
                jsonObject.put("desc","交流桩：枪未插好");
                jsonObject.put("tag","SLX");
                break;

            case 0x37:
                jsonObject.put("code", 55);
                jsonObject.put("desc","交流桩：车主动关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x38:
                jsonObject.put("code", 56);
                jsonObject.put("desc","交流桩：断网关机");
                jsonObject.put("tag","SLX");
                break;

            case 0x39:
                jsonObject.put("code", 57);
                jsonObject.put("desc","远程调用System关机");
                jsonObject.put("tag","SLX");
                break;
        }


        eventDTO.setEndReasonJson(jsonObject.toJSONString());
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
        if (mcElectricityPriceCommandDTO != null) {
            int money = (int) ((mcElectricityPriceCommandDTO.getServicePrice1() * eventDTO.getOutPower() )/ 10000 );
            eventPushDTO.setServiceMoney(money);
            eventPushDTO.setChargeMoney(eventPushDTO.getChargeMoney() - money);
        }

        PileEventReqDTO reqDTOTemp = new PileEventReqDTO();
        reqDTOTemp.setIdCode(reqDTO.getIdCode());
        reqDTOTemp.setEventStartTime(eventDTO.getStartTime());
        reqDTOTemp.setEventEndTime(eventDTO.getEndTime());
        reqDTOTemp.setEventState((byte) 2);
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
        eventDTO.setOutPower(reader.readInt() * 100);
        eventDTO.setChargeMoney(reader.readInt() * 100);
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
        if (!reader.isEnd()) {
            eventDTO.setStartSoc(reader.readByte());
        }
        return eventDTO;
    }

}
