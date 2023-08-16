package com.huamar.charge.pile.server.service.handler;

import com.huamar.charge.pile.convert.McHeartbeatConvert;
import com.huamar.charge.pile.entity.dto.fault.McHeartbeatReqDTO;
import com.huamar.charge.pile.entity.dto.resp.McCommonResp;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.McAnswerEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.protocol.DataPacket;
import com.huamar.charge.pile.server.service.McAnswerFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * 设备心跳包
 * 2023/08/01
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineHeartbeatHandler implements MachineMessageHandler<DataPacket> {

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
        return ProtocolCodeEnum.HEART_BEAT;
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
            log.info("设备心跳包，ip={}", channelContext.getClientNode().getIp());
            McHeartbeatReqDTO reqDTO = this.reader(packet);
            // 通用应答
            answerFactory.getExecute(McAnswerEnum.COMMON).execute(McCommonResp.ok(packet), channelContext);
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
    public McHeartbeatReqDTO reader(DataPacket packet) {
        return McHeartbeatConvert.INSTANCE.convert(packet);
    }

    @PostConstruct
    public void logTest() {
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                MDC.put(ConstEnum.ID_CODE.getCode(), "471000220714302005");
                log.info("日志测试-设备心跳包，idCode:{}，ip={}", "471000220714302005", "0.0.0.0");
                TimeUnit.SECONDS.sleep(1);
                MDC.clear();
            }
        }).start();
    }
}
