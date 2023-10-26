package com.huamar.charge.pile.server.service.fault;

import com.huamar.charge.pile.entity.dto.fault.PileFaultPutReqDTO;
import com.huamar.charge.pile.enums.PileFaultPutEnum;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.DataPacketWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备故障汇报接口
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
public class McGunFaultPutExecute implements McFaultPutExecute<PileFaultPutReqDTO> {

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public PileFaultPutEnum getCode() {
        return PileFaultPutEnum.CONFIG_EVENT;
    }

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void execute(PileFaultPutReqDTO reqDTO) {
        log.info("电表故障:{}", "start");
    }

    /**
     * 读取参数
     *
     * @param packet packet
     * @return McBaseParameterDTO
     */
    @Override
    public PileFaultPutReqDTO reader(DataPacket packet) {
        return McFaultPutExecute.super.reader(packet);
    }

    /**
     * 封装协议数据
     *
     * @param command command
     * @return DataPacketWriter
     */
    @Override
    public DataPacketWriter writer(PileFaultPutReqDTO command) {
        return McFaultPutExecute.super.writer(command);
    }
}
