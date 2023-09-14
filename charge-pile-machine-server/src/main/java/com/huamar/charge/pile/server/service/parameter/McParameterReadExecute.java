package com.huamar.charge.pile.server.service.parameter;

import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.pile.convert.McParameterConvert;
import com.huamar.charge.pile.entity.dto.McParameterReqDTO;
import com.huamar.charge.pile.enums.McParameterEnum;
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
public class McParameterReadExecute implements McParameterExecute<McParameterReqDTO> {

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McParameterEnum getCode() {
        return McParameterEnum.READ_CONFIG;
    }

    /**
     * 执行方法
     *
     * @param command command
     */
    @Override
    public void execute(McParameterReqDTO command) {
        log.info("Parameter Read idCode:{}", command.getIdCode());
    }


    /**
     * 读取参数
     *
     * @param packet packet
     * @return McBaseParameterDTO
     */
    @Override
    public McParameterReqDTO reader(DataPacket packet) {
        return McParameterConvert.INSTANCE.convert(packet);
    }

}
