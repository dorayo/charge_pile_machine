package com.huamar.charge.pile.server.service.fault;

import com.huamar.charge.pile.dto.fault.McFaultPutReqDTO;
import com.huamar.charge.pile.enums.McFaultPutEnum;
import com.huamar.charge.pile.protocol.DataPacket;
import com.huamar.charge.pile.protocol.DataPacketWriter;
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
public class McGunFaultPutExecute implements McFaultPutExecute<McFaultPutReqDTO> {

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McFaultPutEnum getCode() {
        return McFaultPutEnum.CONFIG_EVENT;
    }

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void execute(McFaultPutReqDTO reqDTO) {
        log.info("电表故障:{}", "start");
    }

    /**
     * 读取参数
     *
     * @param packet packet
     * @return McBaseParameterDTO
     */
    @Override
    public McFaultPutReqDTO reader(DataPacket packet) {
        return McFaultPutExecute.super.reader(packet);
    }

    /**
     * 封装协议数据
     *
     * @param command command
     * @return DataPacketWriter
     */
    @Override
    public DataPacketWriter writer(McFaultPutReqDTO command) {
        return McFaultPutExecute.super.writer(command);
    }
}
