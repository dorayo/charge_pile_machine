package com.huamar.charge.pile.server.service.command;

import cn.hutool.core.convert.Convert;
import com.huamar.charge.pile.entity.dto.command.McCardQueryCommandDTO;
import com.huamar.charge.pile.entity.dto.command.McCommandDTO;
import com.huamar.charge.pile.enums.McCommandEnum;
import com.huamar.charge.pile.protocol.DataPacket;
import com.huamar.charge.pile.protocol.DataPacketWriter;
import com.huamar.charge.pile.server.service.MachineContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 远程控制执行-卡查询结果
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McCardQueryCommandExecute implements McCommandExecute<McCardQueryCommandDTO> {

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
        return McCommandEnum.CARD_QUERY;
    }


    /**
     * 执行方法
     *
     * @param command command
     */
    @Override
    public void execute(McCardQueryCommandDTO command) {
        DataPacket packet = this.packet(command);
        boolean sendCommand = machineContext.sendCommand(packet);
        log.info("Card Query idCode:{} sendCommand:{} ", command.getIdCode(), sendCommand);
    }


    /**
     * 指令转换
     *
     * @param command McChargeCommandDTO
     */
    @Override
    public McCommandDTO convert(McCardQueryCommandDTO command) {
        short typeCode = Convert.toShort(getCode().getCode());
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(command.getResult());
        writer.write(command.getCardState());
        writer.write(command.getMoney());
        writer.write(command.getDescLen());
        writer.write(command.getDesc());
        writer.write(command.getOrderSerialNumber(), 32);
        writer.write(command.getGunSort());
        return new McCommandDTO(typeCode, command.getFieldsByteLength(), writer.toByteArray());
    }

}
