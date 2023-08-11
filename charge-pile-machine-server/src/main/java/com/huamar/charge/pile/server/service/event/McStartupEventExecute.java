package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.pile.entity.dto.McEventReqDTO;
import com.huamar.charge.pile.entity.dto.McStartupChargeEvent;
import com.huamar.charge.pile.enums.McEventEnum;
import com.huamar.charge.pile.protocol.DataPacketReader;
import org.springframework.stereotype.Component;

/**
 * 设备端数据汇报接口-主动请求开机事件
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
public class McStartupEventExecute implements McEventExecute{


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McEventEnum getCode() {
        return McEventEnum.STARTUP;
    }

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void execute(McEventReqDTO reqDTO) {
        //TODO 业务实现
        McStartupChargeEvent eventDTO = this.parse(reqDTO);
    }

    /**
     * 解析元数据
     *
     * @param reqDTO reqDTO
     * @return McEventBaseDTO
     */
    @Override
    public McStartupChargeEvent parse(McEventReqDTO reqDTO) {
        DataPacketReader reader = new DataPacketReader(reqDTO.getEventData());
        McStartupChargeEvent eventDTO = new McStartupChargeEvent();
        eventDTO.setGunSort(reader.readByte());
        eventDTO.setRestartReason(reader.readByte());
        eventDTO.setLastSoc(reader.readByte());
        eventDTO.setOrderSerialNumber(reader.readString(32).substring(0, 32));
        eventDTO.setReserve1(reader.readInt());
        eventDTO.setReserve2(reader.readInt());
        return eventDTO;
    }


}
