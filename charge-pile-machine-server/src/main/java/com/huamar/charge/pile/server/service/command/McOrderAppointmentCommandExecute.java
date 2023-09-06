package com.huamar.charge.pile.server.service.command;

import cn.hutool.core.convert.Convert;
import com.huamar.charge.pile.entity.dto.command.McCommandDTO;
import com.huamar.charge.pile.entity.dto.command.McOrderAppointmentCommandDTO;
import com.huamar.charge.pile.enums.McCommandEnum;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketWriter;
import com.huamar.charge.pile.server.service.MachineContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 远程控制执行-Vin白名单查询
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McOrderAppointmentCommandExecute implements McCommandExecute<McOrderAppointmentCommandDTO> {

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
        return McCommandEnum.ORDER_APPOINTMENT;
    }


    /**
     * 执行方法
     *
     * @param command command
     */
    @Override
    public void execute(McOrderAppointmentCommandDTO command) {
        DataPacket packet = this.packet(command);
        boolean sendCommand = machineContext.sendCommand(packet);
        log.info("Order Appointment idCode:{} sendCommand:{} ", command.getIdCode(), sendCommand);
    }


    /**
     * 指令转换
     *
     * @param command McChargeCommandDTO
     */
    @Override
    public McCommandDTO convert(McOrderAppointmentCommandDTO command) {
        short typeCode = Convert.toShort(getCode().getCode());
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(command.getOrderSerialNumber(), 32);
        writer.write(command.getCurMoney());
        writer.write(command.getServiceMoney());
        writer.write(command.getOutPower());
        writer.write(command.getGunSort());
        writer.write(command.getCumulativeTime());
        return new McCommandDTO(typeCode, command.getFieldsByteLength(), writer.toByteArray());
    }

}
