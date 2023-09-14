package com.huamar.charge.pile.server.service.handler;

import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.convert.McEventConvert;
import com.huamar.charge.pile.entity.dto.event.PileEventReqDTO;
import com.huamar.charge.pile.entity.dto.resp.McCommonResp;
import com.huamar.charge.pile.enums.McAnswerEnum;
import com.huamar.charge.pile.enums.PileEventEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.pile.server.service.factory.McAnswerFactory;
import com.huamar.charge.pile.server.service.factory.PileEventFactory;
import com.huamar.charge.pile.server.service.event.PileEventExecute;
import com.huamar.charge.common.util.HexExtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
public class MachineEventHandler implements MachinePacketHandler<DataPacket> {


    private final PileEventFactory eventFactory;

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
     * @param sessionChannel sessionChannel
     */
    @Override
    public void handler(DataPacket packet, SessionChannel sessionChannel) {
        try {
            log.info("设备事件汇报 start ==>");
            String ip = sessionChannel.getIp();
            PileEventReqDTO reqDTO = McEventConvert.INSTANCE.convert(packet);
            String eventCode = HexExtUtil.encodeHexStr(reqDTO.getEventType());
            log.info("设备事件汇报，ip={} eventCode:{}", ip, eventCode);
            answerFactory.getExecute(McAnswerEnum.COMMON).execute(McCommonResp.ok(packet), sessionChannel);

            PileEventEnum eventEnum = PileEventEnum.getByCode(eventCode);
            if(Objects.isNull(eventEnum)){
                log.error("eventEnum is null eventCode:{}", eventCode);
            }

            PileEventExecute execute = eventFactory.getExecute(eventEnum);
            if(Objects.isNull(execute)){
                log.error("eventFactory get null eventEnum:{}", eventEnum);
            }
            execute.execute(reqDTO);
        }catch (Exception e){
            log.error("event handler error ==> e:{}",e.getMessage(), e);
            answerFactory.getExecute(McAnswerEnum.COMMON).execute(McCommonResp.fail(packet), sessionChannel);
        }
    }

}
