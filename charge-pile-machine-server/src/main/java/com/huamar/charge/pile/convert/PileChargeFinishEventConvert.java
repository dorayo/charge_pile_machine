package com.huamar.charge.pile.convert;

import com.huamar.charge.pile.entity.dto.event.PileChargeFinishEventDTO;
import com.huamar.charge.pile.entity.dto.event.PileEventReqDTO;
import com.huamar.charge.pile.entity.dto.platform.event.PileChargeFinishEventPushDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 设备事件-充电完成事件
 * 2023/08
 *
 * @author TiAmo(13721682347@163.com)
 */
@Mapper
public interface PileChargeFinishEventConvert {

    PileChargeFinishEventConvert INSTANCE = Mappers.getMapper(PileChargeFinishEventConvert.class);


    PileChargeFinishEventPushDTO convert(PileChargeFinishEventDTO pileHandshakeEventDTO);


    /**
     * 复制默认的属性
     * @param baseDTO baseDTO
     * @param reqDTO reqDTO
     */
    default void copyBaseField(PileChargeFinishEventPushDTO baseDTO, PileEventReqDTO reqDTO){
        baseDTO.setIdCode(reqDTO.getIdCode());
        baseDTO.setEventState((int) reqDTO.getEventState());
        baseDTO.setEventType((int) reqDTO.getEventType());
        baseDTO.setEventStartTime(reqDTO.getEventStartTime().toString());
        baseDTO.setEventEndTime(reqDTO.getEventEndTime().toString());
    }
}
