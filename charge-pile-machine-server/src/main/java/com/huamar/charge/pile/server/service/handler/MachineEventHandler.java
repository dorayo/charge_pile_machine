package com.huamar.charge.pile.server.service.handler;

import com.huamar.charge.pile.convert.McEventConvert;
import com.huamar.charge.pile.entity.dto.McEventReqDTO;
import com.huamar.charge.pile.entity.dto.resp.McCommonResp;
import com.huamar.charge.pile.enums.McAnswerEnum;
import com.huamar.charge.pile.enums.McEventEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.protocol.DataPacket;
import com.huamar.charge.pile.server.service.McAnswerFactory;
import com.huamar.charge.pile.server.service.McEventFactory;
import com.huamar.charge.pile.server.service.event.McEventExecute;
import com.huamar.charge.pile.util.HexExtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;

import java.util.Objects;

/**
 * 设备事件汇报
 * 2023/06/11
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineEventHandler implements MachineMessageHandler<DataPacket> {


    private final McEventFactory eventFactory;

    /**
     * 设备终端上下文
     */
    private final McAnswerFactory answerFactory;


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public ProtocolCodeEnum getCode() {
        return ProtocolCodeEnum.EVENT;
    }

    /**
     * 执行器
     *
     * @param packet         packet
     * @param channelContext channelContext
     */
    @Override
    public void handler(DataPacket packet, ChannelContext channelContext) {
        try {
            McEventReqDTO reqDTO = McEventConvert.INSTANCE.convert(packet);
            log.info("设备事件汇报，ip={}", channelContext.getClientNode().getIp());

            log.info("设备事件汇报：{}", "设备事件汇报");
            answerFactory.getExecute(McAnswerEnum.COMMON)
                    .execute(McCommonResp.ok(packet), channelContext);

            String eventCode = HexExtUtil.encodeHexStr(reqDTO.getEventType());
            McEventEnum eventEnum = McEventEnum.getByCode(eventCode);
            if(Objects.isNull(eventEnum)){
                log.error("eventEnum is null eventCode:{}", eventCode);
            }

            McEventExecute execute = eventFactory.getExecute(eventEnum);
            if(Objects.isNull(execute)){
                log.error("eventFactory get null eventEnum:{}", eventEnum);
            }

            execute.execute(reqDTO);
        }catch (Exception e){
            answerFactory.getExecute(McAnswerEnum.COMMON)
                    .execute(McCommonResp.fail(packet), channelContext);
        }
    }

}
