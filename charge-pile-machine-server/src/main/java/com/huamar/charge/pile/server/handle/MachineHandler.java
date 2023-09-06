package com.huamar.charge.pile.server.handle;

import cn.hutool.core.util.IdUtil;
import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.FailMathPacket;
import com.huamar.charge.pile.server.service.MachineContext;
import com.huamar.charge.pile.server.service.MachineMessageFactory;
import com.huamar.charge.pile.server.service.handler.MachineMessageHandler;
import com.huamar.charge.common.util.HexExtUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.server.intf.ServerAioHandler;

/**
 * 设备业务拦截器
 * date 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MachineHandler extends AbstractHandler implements ServerAioHandler {

    /**
     * 消息处理器工厂
     */
    MachineMessageFactory machineMessageFactory;

    /**
     * 设备终端上下文
     */
    MachineContext machineContext;

    /**
     * 处理消息
     */
    @Override
    @SneakyThrows
    public void handler(Packet packet, ChannelContext channelContext) {
        try {
            if(packet instanceof DataPacket){
                MDC.put(ConstEnum.ID_CODE.getCode(), new String(((DataPacket) packet).getIdCode()));
                machineContext.handlerSession(packet, channelContext);
                DataPacket dataPacket = (DataPacket) packet;
                String code = HexExtUtil.encodeHexStr(dataPacket.getMsgId());
                MachineMessageHandler<DataPacket> handler = machineMessageFactory.getHandler(ProtocolCodeEnum.getByCode(code));
                handler.handler(dataPacket, channelContext);
                return;
            }

            if(packet instanceof FailMathPacket){
                FailMathPacket dataPacket = (FailMathPacket) packet;
                log.info("FailMathPacket data:{}", HexExtUtil.encodeHexStrFormat(dataPacket.getBody(), StringPool.SPACE));
            }
        }catch (Exception e){
            log.error("error ==> e:{}", e.getMessage(), e);
        }finally {
            MDC.clear();
        }

    }

    @Autowired
    public void setMachineMessageFactory(MachineMessageFactory machineMessageFactory) {
        this.machineMessageFactory = machineMessageFactory;
    }

    @Autowired
    public void setMachineContext(MachineContext machineContext) {
        this.machineContext = machineContext;
    }
}
