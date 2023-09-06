package com.huamar.charge.pile.server.service.parameter;

import cn.hutool.core.collection.CollectionUtil;
import com.huamar.charge.pile.entity.dto.parameter.McParameterDTO;
import com.huamar.charge.pile.enums.McParameterEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketWriter;
import com.huamar.charge.pile.server.service.MachineContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 远程控制执行下发
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McParameterSendExecute implements McParameterExecute<McParameterDTO> {

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
    public McParameterEnum getCode() {
        return McParameterEnum.SEND;
    }

    /**
     * 执行方法
     *
     * @param command command
     */
    @Override
    public void execute(McParameterDTO command) {
        DataPacket packet = this.packet(command);
        packet.setMsgId(ProtocolCodeEnum.PARAMETER_SEND.codeByte());
        boolean sendCommand = machineContext.sendCommand(packet);
        log.info("Parameter Send idCode:{} sendCommand:{} ", command.getIdCode(), sendCommand);
    }


    /**
     * 写入协议数据
     *
     * @param command command
     * @return DataPacketWriter
     */
    @Override
    public DataPacketWriter writer(McParameterDTO command) {
        DataPacketWriter writer = new DataPacketWriter();
        writer.write(command.getParamNumber());
        if(CollectionUtil.isEmpty(command.getDataList())){
            return writer;
        }

        command.getDataList().forEach(item -> {
            Short id = item.getId();
            Short len = item.getParamLength();
            String paramData = item.getParamData();
            writer.write(id);
            writer.write(len);
            writer.write(paramData,len);
        });
        return writer;
    }
}
