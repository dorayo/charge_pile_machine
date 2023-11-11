package com.huamar.charge.pile.server.service.upload;

import cn.hutool.core.util.IdUtil;
import com.huamar.charge.common.common.codec.BCD;
import com.huamar.charge.pile.config.PileMachineProperties;
import com.huamar.charge.pile.entity.dto.MachineDataUpItem;
import com.huamar.charge.pile.entity.dto.ChargeStageDataDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.enums.McDataUploadEnum;
import com.huamar.charge.common.protocol.DataPacketReader;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;


/**
 * 充电桩 充电阶段数据 One
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class McDataUploadStageExecute implements McDataUploadExecute {

    /**
     * 消息投递
     */
    private final PileMessageProduce pileMessageProduce;

    /**
     * 设备参数配置
     */
    private final PileMachineProperties pileMachineProperties;

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
        list.forEach(item -> {
            this.execute(time, item);
        });
    }

    /**
     * Execute.
     *
     * @param time time
     * @param item the item
     */
    public void execute(BCD time, MachineDataUpItem item) {
        // TODO 业务实现
        ChargeStageDataDTO parse = this.parse(item);
        log.info("充电桩实时状态信息表 data:{}", JSONParser.jsonString(parse));
        this.sendMessage(parse);
    }

    /**
     * Execute.
     *
     * @param time time
     * @param item the item
     */
    public void executeB(BCD time, MachineDataUpItem item) {
        // TODO 业务实现
        ChargeStageDataDTO parse = this.parse(item);
        log.info("充电桩实时状态信息表 data:{}", JSONParser.jsonString(parse));
        parse.setPileElectricityOutValue((short)(parse.getPileElectricityOutValue()+1600));
        parse.setBatteryChargeElectricity((short)(parse.getBatteryChargeElectricity()+1600));
        this.sendMessage(parse);
    }

    /**
     * 发送设备端消息
     *
     * @param chargeStageDataDTO chargeStageDataDTO
     */
    private void sendMessage(ChargeStageDataDTO chargeStageDataDTO) {
        try {
            Assert.notNull(chargeStageDataDTO, "chargeStageDataDTO noNull");
            MessageData<ChargeStageDataDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_CHARGE_STAGE, chargeStageDataDTO);
            messageData.setBusinessId(chargeStageDataDTO.getIdCode());
            messageData.setMessageId(IdUtil.simpleUUID());
            messageData.setRequestId(IdUtil.simpleUUID());
            pileMessageProduce.send(messageData);
        } catch (Exception e) {
            log.error("sendMessage send error e:{}", e.getMessage(), e);
        }
    }

    /**
     * 解析对象
     *
     * @param data data
     * @return McChargerOnlineInfoDto
     */
    private ChargeStageDataDTO parse(MachineDataUpItem data) {
        DataPacketReader reader = new DataPacketReader(data.getData());
        ChargeStageDataDTO chargeStageDataDTO = new ChargeStageDataDTO();
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
        chargeStageDataDTO.setIdCode(data.getIdCode());
        return chargeStageDataDTO;
    }

}
