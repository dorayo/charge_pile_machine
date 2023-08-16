package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.pile.entity.dto.event.PileEventReqDTO;
import com.huamar.charge.pile.entity.dto.event.PileOrderUploadEventDTO;
import com.huamar.charge.pile.enums.PileEventEnum;
import com.huamar.charge.pile.protocol.DataPacketReader;
import com.huamar.charge.pile.util.JSONParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备端数据汇报接口-订单上传事件
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
public class PileOrderUploadEventExecute implements PileEventExecute {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public PileEventEnum getCode() {
        return PileEventEnum.ORDER_UPLOAD;
    }

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void execute(PileEventReqDTO reqDTO) {
        log.info("事件汇报：{}", getCode().getDesc());
        //TODO 业务实现
        PileOrderUploadEventDTO eventDTO = this.parse(reqDTO);
    }

    /**
     * 解析元数据
     *
     * @param reqDTO reqDTO
     * @return McEventBaseDTO
     */
    @Override
    public PileOrderUploadEventDTO parse(PileEventReqDTO reqDTO) {
        DataPacketReader reader = new DataPacketReader(reqDTO.getEventData());
        PileOrderUploadEventDTO eventDTO = new PileOrderUploadEventDTO();
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
