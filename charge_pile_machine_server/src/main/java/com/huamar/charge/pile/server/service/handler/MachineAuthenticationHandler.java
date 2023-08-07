package com.huamar.charge.pile.server.service.handler;

import com.huamar.charge.pile.convert.MachineAuthenticationConvert;
import com.huamar.charge.pile.dto.MachineAuthenticationReqDTO;
import com.huamar.charge.pile.dto.PileDTO;
import com.huamar.charge.pile.dto.resp.McAuthResp;
import com.huamar.charge.pile.enums.McAnswerEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.protocol.DataPacket;
import com.huamar.charge.pile.server.service.McAnswerFactory;
import com.huamar.charge.pile.server.service.mc.MachineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;

/**
 * 终端鉴权
 * 2023/06/11
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineAuthenticationHandler implements MachineMessageHandler<DataPacket> {


    private final MachineService machineService;

    private final McAnswerFactory answerFactory;

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public ProtocolCodeEnum getCode() {
        return ProtocolCodeEnum.AUTH;
    }

    /**
     * 执行器
     *
     * @param packet         packet
     * @param channelContext channelContext
     */
    @Override
    public void handler(DataPacket packet, ChannelContext channelContext) {
        MachineAuthenticationReqDTO reqDTO = this.reader(packet);
        log.info("终端鉴权，loginNumber={} time={} ip={}",
                reqDTO.getLoginNumber(),
                reqDTO.getTerminalTime(),
                channelContext.getClientNode().getIp()
        );

        // TODO 验证充电桩 业务逻辑
        PileDTO pile = machineService.getPile(reqDTO.getIdCode());

        // TODO 应答权健
        McAuthResp authResp = new McAuthResp();
        answerFactory.getExecute(McAnswerEnum.AUTH).execute(authResp, channelContext);

    }

    /**
     * 读取参数
     *
     * @param packet packet
     * @return McBaseParameterDTO
     */
    @Override
    public MachineAuthenticationReqDTO reader(DataPacket packet) {
        return MachineAuthenticationConvert.INSTANCE.convert(packet);
    }
}
