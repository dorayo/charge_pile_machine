package com.huamar.charge.pile.server.service.handler;

import com.huamar.charge.pile.convert.McFaultConvert;
import com.huamar.charge.pile.entity.dto.fault.McFaultPutReqDTO;
import com.huamar.charge.pile.entity.dto.resp.McCommonResp;
import com.huamar.charge.pile.enums.McAnswerEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.pile.server.service.McAnswerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;

/**
 * 设备故障上报
 * 2023/08/01
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineFaultHandler implements MachineMessageHandler<DataPacket> {

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
        return ProtocolCodeEnum.FAULT;
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
            log.info("设备故障上报，ip={}", channelContext.getClientNode().getIp());
            McFaultPutReqDTO reqDTO = this.reader(packet);
            // 通用应答
            answerFactory.getExecute(McAnswerEnum.COMMON).execute(McCommonResp.ok(packet), channelContext);

            //TODO 业务实现


        }catch (Exception e){
            answerFactory.getExecute(McAnswerEnum.COMMON).execute(McCommonResp.fail(packet), channelContext);
        }
    }

    /**
     * 读取参数
     *
     * @param packet packet
     * @return McBaseParameterDTO
     */
    @Override
    public McFaultPutReqDTO reader(DataPacket packet) {
        return McFaultConvert.INSTANCE.convert(packet);
    }
}
