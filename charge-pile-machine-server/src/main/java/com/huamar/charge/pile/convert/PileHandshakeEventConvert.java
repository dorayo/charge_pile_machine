package com.huamar.charge.pile.convert;

import com.huamar.charge.pile.entity.dto.event.PileEventReqDTO;
import com.huamar.charge.pile.entity.dto.event.PileHandshakeEventDTO;
import com.huamar.charge.pile.entity.dto.platform.event.PileEventPushBaseDTO;
import com.huamar.charge.pile.entity.dto.platform.event.PileHandshakeEventPushDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 充电握手转换器
 * 2023/08
 *
 * @author TiAmo(13721682347@163.com)
 */
@Mapper
public interface PileHandshakeEventConvert {

    PileHandshakeEventConvert INSTANCE = Mappers.getMapper(PileHandshakeEventConvert.class);


    @SuppressWarnings("UnmappedTargetProperties")
    PileHandshakeEventPushDTO convert(PileHandshakeEventDTO pileHandshakeEventDTO);


    /**
     * 复制默认的属性
     * @param baseDTO baseDTO
     * @param reqDTO reqDTO
     */
    default void copyBaseField(PileEventPushBaseDTO baseDTO, PileEventReqDTO reqDTO){
        baseDTO.setIdCode(reqDTO.getIdCode());
        baseDTO.setEventState((int) reqDTO.getEventState());
        baseDTO.setEventType((int) reqDTO.getEventType());
        baseDTO.setEventStartTime(reqDTO.getEventStartTime().toString());
        baseDTO.setEventEndTime(reqDTO.getEventEndTime().toString());
    }
}
