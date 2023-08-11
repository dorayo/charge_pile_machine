package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.pile.entity.dto.McHandshakeEventDTO;
import com.huamar.charge.pile.entity.dto.McEventReqDTO;
import com.huamar.charge.pile.enums.McEventEnum;
import com.huamar.charge.pile.protocol.DataPacketReader;
import com.huamar.charge.pile.util.HexExtUtil;
import org.springframework.stereotype.Component;

/**
 * 设备端数据汇报接口-充电握手事件
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
public class McHandshakeEventExecute implements McEventExecute{


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McEventEnum getCode() {
        return McEventEnum.HAND_SHAKE;
    }

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void execute(McEventReqDTO reqDTO) {
        //TODO 业务实现
        McHandshakeEventDTO eventDTO = this.parse(reqDTO);
    }

    /**
     * 解析元数据
     *
     * @param reqDTO reqDTO
     * @return McEventBaseDTO
     */
    @Override
    public McHandshakeEventDTO parse(McEventReqDTO reqDTO) {
        DataPacketReader reader = new DataPacketReader(reqDTO.getEventData());
        McHandshakeEventDTO eventDTO = new McHandshakeEventDTO();
        eventDTO.setGunSort(reader.readByte());
        eventDTO.setDistinguishResult(reader.readByte());
        eventDTO.setBmsProtocolVersion(HexExtUtil.encodeHexStr(reader.readBytes(3)));
        eventDTO.setBatteryType(reader.readByte());
        eventDTO.setCarRatedCapacity(reader.readInt());
        eventDTO.setCarRatedVoltage(reader.readShort());
        eventDTO.setDantidianya(reader.readShort());
        eventDTO.setZuigaodianliu(reader.readShort());
        eventDTO.setBatterySerialNumber(reader.readShort());
        eventDTO.setBatteryProductionDate(reader.readBCD3());
        eventDTO.setBatteryChargeCount(reader.readInt());
        eventDTO.setBatteryTag(reader.readByte());
        eventDTO.setCarIdentificationCode(HexExtUtil.encodeHexStr(reader.readBytes(17)));
        return eventDTO;
    }


}
