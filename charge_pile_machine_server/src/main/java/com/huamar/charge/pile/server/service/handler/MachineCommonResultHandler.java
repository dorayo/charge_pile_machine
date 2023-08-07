package com.huamar.charge.pile.server.service.handler;

import com.huamar.charge.pile.convert.McCommonConvert;
import com.huamar.charge.pile.dto.McCommonReq;
import com.huamar.charge.pile.enums.McCommonResultEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.protocol.DataPacket;
import com.huamar.charge.pile.server.service.McCommonResultFactory;
import com.huamar.charge.pile.server.service.common.McCommonResultExecute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;

/**
 * 通用应答处理
 * 2023/08/01
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineCommonResultHandler implements MachineMessageHandler<DataPacket> {

    /**
     * 通用应答处理工厂
     */
    private final McCommonResultFactory commonResultFactory;

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public ProtocolCodeEnum getCode() {
        return ProtocolCodeEnum.COMMON_ACK;
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
            log.info("通用应答处理，ip={}", channelContext.getClientNode().getIp());
            McCommonReq commonReq = this.reader(packet);
            String hexString = Integer.toHexString(commonReq.getMsgResult());
            McCommonResultEnum commonResultEnum = McCommonResultEnum.getByCode(hexString);
            McCommonResultExecute<McCommonReq> execute = commonResultFactory.getExecute(commonResultEnum);
            execute.execute(commonReq);
        }catch (Exception e){
            log.error("handler MachineCommon error:{}, e ->", e.getMessage(), e);
        }
    }

    /**
     * 读取参数
     *
     * @param packet packet
     * @return McBaseParameterDTO
     */
    @Override
    public McCommonReq reader(DataPacket packet) {
        McCommonReq req = McCommonConvert.INSTANCE.convert(packet);
        req.setIdCode(new String(packet.getIdCode()));
        return req;
    }

}
