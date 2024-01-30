package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.common.protocol.DataPacketReader;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.pile.entity.dto.event.PileEventReqDTO;
import com.huamar.charge.pile.entity.dto.event.PileOrderUploadEventDTO;
import com.huamar.charge.pile.entity.dto.event.PileSwipeCardEventDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.event.PileChargeArgConfigPushDTO;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.enums.PileEventEnum;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 事件汇报事件---刷卡查询事件
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PileSwipeCardQueryEventExecute implements PileEventExecute {

    private final PileMessageProduce messageProduce;

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public PileEventEnum getCode() {
        return PileEventEnum.SWIPE_CARD_QUERY;
    }


    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void execute(PileEventReqDTO reqDTO) {
        log.info("刷卡查询：{}", getCode().getDesc());
        PileSwipeCardEventDTO eventDTO = this.parse(reqDTO);

        log.info("刷卡查询：{}, data:{}", getCode().getDesc(), JSONParser.jsonString(eventDTO));

        MessageData<PileSwipeCardEventDTO> messageData = new MessageData<>(eventDTO);
        messageData.setBusinessCode(MessageCodeEnum.SWIPE_CARD_QUERY.getCode());
        messageData.setBusinessId(reqDTO.getIdCode());
        messageProduce.send(messageData);


    }

    /**
     * 解析元数据
     *
     * @param reqDTO reqDTO
     * @return McEventBaseDTO
     */
    @Override
    public PileSwipeCardEventDTO parse(PileEventReqDTO reqDTO) {
        DataPacketReader reader = new DataPacketReader(reqDTO.getEventData());
        PileSwipeCardEventDTO eventDTO = new PileSwipeCardEventDTO();
        eventDTO.setIdCode(reqDTO.getIdCode());
        eventDTO.setCardNumberLen(reader.readByte());
        try {
            eventDTO.setCardNumber(String.valueOf(reader.readBCD8()));
        } catch (Exception ignored) {
        }
        eventDTO.setGunSort(reader.readByte());
        eventDTO.setPasswordLen(reader.readByte());
        eventDTO.setPassword(String.valueOf(reader.readBCD8(eventDTO.getPasswordLen())));
        return eventDTO;
    }


}
