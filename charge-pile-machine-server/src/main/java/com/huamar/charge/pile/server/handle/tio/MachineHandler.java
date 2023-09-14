package com.huamar.charge.pile.server.handle.tio;

import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.common.protocol.BasePacket;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.protocol.FailMathPacket;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.server.protocol.TioPacket;
import com.huamar.charge.pile.server.service.factory.MachinePacketFactory;
import com.huamar.charge.pile.server.service.handler.MachinePacketHandler;
import com.huamar.charge.pile.server.session.TioSessionChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;
import org.tio.server.intf.ServerAioHandler;

import java.util.Objects;

/**
 * 设备业务拦截器
 * date 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
public class MachineHandler extends AbstractHandler implements ServerAioHandler {

    /**
     * 消息处理器工厂
     */
    private final MachinePacketFactory machinePacketFactory;

    public MachineHandler(MachinePacketFactory machinePacketFactory) {
        this.machinePacketFactory = machinePacketFactory;
    }

    /**
     * 处理消息
     */
    @Override
    @SneakyThrows
    public void handler(Packet packet, ChannelContext channelContext) {
        try {
            TioSessionChannel tioSessionChannel = new TioSessionChannel(channelContext);
            TioPacket tioPacket = (TioPacket) packet;
            BasePacket basePacket = tioPacket.getBasePacket();

            if (basePacket instanceof DataPacket) {
                MDC.put(ConstEnum.ID_CODE.getCode(), new String(((DataPacket) basePacket).getIdCode()));
                this.handlerSession(basePacket, tioSessionChannel);
                DataPacket dataPacket = (DataPacket) basePacket;
                String code = HexExtUtil.encodeHexStr(dataPacket.getMsgId());
                MachinePacketHandler<DataPacket> handler = machinePacketFactory.getHandler(ProtocolCodeEnum.getByCode(code));
                handler.handler(dataPacket, tioSessionChannel);
                return;
            }

            if (basePacket instanceof FailMathPacket) {
                FailMathPacket dataPacket = (FailMathPacket) basePacket;
                log.info("FailMathPacket data:{}", HexExtUtil.encodeHexStrFormat(dataPacket.getBody(), StringPool.SPACE));
            }
        } catch (Exception e) {
            log.error("error ==> e:{}", e.getMessage(), e);
        } finally {
            MDC.clear();
        }

    }

    /**
     * 执行Session相关测试
     *
     * @param packet         basePacket
     * @param sessionChannel sessionChannel
     */
    private void handlerSession(BasePacket packet, SessionChannel sessionChannel) {
        DataPacket dataPacket = (DataPacket) packet;
        ChannelContext channel = (ChannelContext) sessionChannel.channel();
        String pileId = new String(dataPacket.getIdCode());
        Object machine = sessionChannel.getAttribute(ConstEnum.MACHINE_ID.getCode());
        if (Objects.isNull(machine)) {
            sessionChannel.setAttribute(ConstEnum.MACHINE_ID.getCode(), pileId);
            Tio.bindBsId(channel, pileId);
        }
    }

}
