package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.pile.entity.dto.event.PileEventReqDTO;
import com.huamar.charge.pile.entity.dto.event.PileStartupChargeEvent;
import com.huamar.charge.pile.enums.PileEventEnum;
import com.huamar.charge.pile.protocol.DataPacketReader;
import com.huamar.charge.pile.util.JSONParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备端数据汇报接口-主动请求开机事件
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
public class PileStartupEventExecute implements PileEventExecute {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public PileEventEnum getCode() {
        return PileEventEnum.STARTUP;
    }

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void execute(PileEventReqDTO reqDTO) {
        log.info("事件汇报：{}", getCode().getDesc());
        PileStartupChargeEvent eventDTO = this.parse(reqDTO);
    }

    /**
     * 解析元数据
     *
     * @param reqDTO reqDTO
     * @return McEventBaseDTO
     */
    @Override
    public PileStartupChargeEvent parse(PileEventReqDTO reqDTO) {
        DataPacketReader reader = new DataPacketReader(reqDTO.getEventData());
        PileStartupChargeEvent eventDTO = new PileStartupChargeEvent();
        eventDTO.setGunSort(reader.readByte());
        eventDTO.setRestartReason(reader.readByte());
        eventDTO.setLastSoc(reader.readByte());
        eventDTO.setOrderSerialNumber(reader.readString(32).substring(0, 32));
        eventDTO.setReserve1(reader.readInt());
        eventDTO.setReserve2(reader.readInt());
        return eventDTO;
    }


}
