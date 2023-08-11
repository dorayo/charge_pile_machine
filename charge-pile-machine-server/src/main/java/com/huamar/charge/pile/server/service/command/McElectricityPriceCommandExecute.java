package com.huamar.charge.pile.server.service.command;

import cn.hutool.core.convert.Convert;
import com.huamar.charge.pile.entity.dto.command.McCommandDTO;
import com.huamar.charge.pile.entity.dto.command.McElectricityPriceCommandDTO;
import com.huamar.charge.pile.enums.McCommandEnum;
import com.huamar.charge.pile.protocol.DataPacket;
import com.huamar.charge.pile.protocol.DataPacketWriter;
import com.huamar.charge.pile.server.service.MachineContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 远程控制执行-电价下发
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McElectricityPriceCommandExecute implements McCommandExecute<McElectricityPriceCommandDTO> {

    /**
     * 设备上下文
     */
    private final MachineContext machineContext;

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McCommandEnum getCode() {
        return McCommandEnum.ELECTRICITY_PRICE;
    }


    /**
     * 执行方法
     *
     * @param command command
     */
    @Override
    public void execute(McElectricityPriceCommandDTO command) {
        DataPacket packet = this.packet(command);
        boolean sendCommand = machineContext.sendCommand(packet);
        log.info("Electricity Price idCode:{} sendCommand:{} ", command.getIdCode(), sendCommand);
    }


    /**
     * 指令转换
     *
     * @param command McChargeCommandDTO
     */
    @Override
    public McCommandDTO convert(McElectricityPriceCommandDTO command) {
        short typeCode = Convert.toShort(getCode().getCode());
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(command.getGunSort());
        writer.write(command.getPrice1());
        writer.write(command.getPrice2());
        writer.write(command.getPrice3());
        writer.write(command.getPrice4());
        writer.write(command.getPrice5());
        writer.write(command.getPrice6());
        writer.write(command.getServicePrice1());
        writer.write(command.getServicePrice2());
        writer.write(command.getServicePrice3());
        writer.write(command.getServicePrice4());
        writer.write(command.getServicePrice5());
        writer.write(command.getServicePrice6());
        writer.write(command.getTimeStage());
        return new McCommandDTO(typeCode, command.getFieldsByteLength(), writer.toByteArray());
    }

}
