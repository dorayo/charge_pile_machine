package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.pile.entity.dto.McEventReqDTO;
import com.huamar.charge.pile.entity.dto.McOrderUploadEventDTO;
import com.huamar.charge.pile.enums.McEventEnum;
import com.huamar.charge.pile.protocol.DataPacketReader;
import org.springframework.stereotype.Component;

/**
 * 设备端数据汇报接口-订单上传事件
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
public class McOrderUploadEventExecute implements McEventExecute{


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McEventEnum getCode() {
        return McEventEnum.ORDER_UPLOAD;
    }

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void execute(McEventReqDTO reqDTO) {
        //TODO 业务实现
        McOrderUploadEventDTO eventDTO = this.parse(reqDTO);
    }

    /**
     * 解析元数据
     *
     * @param reqDTO reqDTO
     * @return McEventBaseDTO
     */
    @Override
    public McOrderUploadEventDTO parse(McEventReqDTO reqDTO) {
        DataPacketReader reader = new DataPacketReader(reqDTO.getEventData());
        McOrderUploadEventDTO eventDTO = new McOrderUploadEventDTO();
        eventDTO.setCardNumberLen(reader.readByte());
        try {
            eventDTO.setCardNumber(Integer.valueOf(String.valueOf(reader.readBCD8())).toString());
        } catch (Exception ignored) {
        }
        eventDTO.setOverType(reader.readByte());
        eventDTO.setOverValue(reader.readInt());
        eventDTO.setGunSort(reader.readByte());
        eventDTO.setOrderSerialNumber(reader.readString(32));
        return eventDTO;
    }


}
