package com.huamar.charge.pile.server.service.handler;

import com.huamar.charge.pile.entity.dto.parameter.McBaseParameterDTO;
import com.huamar.charge.pile.entity.dto.resp.McCommonResp;
import com.huamar.charge.pile.enums.McAnswerEnum;
import com.huamar.charge.pile.enums.McParameterEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.pile.server.service.McAnswerFactory;
import com.huamar.charge.pile.server.service.McParameterFactory;
import com.huamar.charge.pile.server.service.parameter.McParameterExecute;
import com.huamar.charge.common.util.HexExtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;

import java.util.Objects;

/**
 * 远程参数查询
 * 2023/07/11
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineParameterHandler implements MachineMessageHandler<DataPacket> {


    private final McParameterFactory mcParameterFactory;

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
        return ProtocolCodeEnum.PARAMETER_PARAMETER;
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
            log.info("远程参数查询应答，ip={}", channelContext.getClientNode().getIp());
            String messageIdCode = HexExtUtil.encodeHexStr(packet.getMsgId());
            McParameterEnum mcParameterEnum = McParameterEnum.getByCode(messageIdCode);
            if(Objects.isNull(mcParameterEnum)){
                log.error("mcParameterEnum is null eventCode:{}", messageIdCode);
            }

            @SuppressWarnings("rawtypes")
            McParameterExecute execute = mcParameterFactory.getExecute(mcParameterEnum);
            if(Objects.isNull(execute)){
                log.error("factory get null eventEnum:{}", mcParameterEnum);
            }

            McBaseParameterDTO reader = execute.reader(packet);
            if(Objects.isNull(reader)){
                log.error("handler reader null eventEnum:{}", mcParameterEnum);
            }
            //noinspection unchecked
            execute.execute(reader);
        }catch (Exception e){
            answerFactory.getExecute(McAnswerEnum.COMMON).execute(McCommonResp.fail(packet), channelContext);
        }
    }

}
