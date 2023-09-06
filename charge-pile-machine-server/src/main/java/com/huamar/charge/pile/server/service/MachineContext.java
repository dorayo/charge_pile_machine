package com.huamar.charge.pile.server.service;

import cn.hutool.core.convert.Convert;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.pile.enums.CacheKeyEnum;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.server.MachineServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * 设备业务上下文
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MachineContext {

    private final RedissonClient redissonClient;

    /**
     * 执行Session相关测试
     *
     * @param packet packet
     * @param channelContext channelContext
     */
    public void handlerSession(Packet packet, ChannelContext channelContext) {
        DataPacket dataPacket = (DataPacket) packet;
        String pileId = new String(dataPacket.getIdCode());
        Object machine = channelContext.get(ConstEnum.MACHINE_ID.getCode());
        if(Objects.isNull(machine)){
            channelContext.set(ConstEnum.MACHINE_ID.getCode(), pileId);
            Tio.bindBsId(channelContext, pileId);
        }
        if(Objects.isNull(Tio.getByBsId(channelContext.getTioConfig(), pileId))){
            channelContext.set(ConstEnum.MACHINE_ID.getCode(), pileId);
            Tio.bindBsId(channelContext, pileId);
        }
    }

    /**
     * 消息应答
     *
     * @param packet packet
     * @param channelContext channelContext
     */
    public void answer(Packet packet, ChannelContext channelContext){
        Tio.send(channelContext, packet);
    }


    /**
     * 发送消息
     *
     * @param dataPacket dataPacket
     * @return boolean
     */
    public boolean sendCommand(DataPacket dataPacket){
        return Tio.sendToBsId(MachineServer.serverTioConfig, new String(dataPacket.getIdCode()), dataPacket);
    }

    /**
     * 获取消息流水号
     *
     * @return Short
     */
    public Short getMessageNumber(String idCode){
        RAtomicLong atomicLong = redissonClient.getAtomicLong(CacheKeyEnum.MACHINE_MESSAGE_NUM_INCR.joinKey(idCode));
        long incremented = atomicLong.incrementAndGet();
        if(Objects.equals(incremented, 1L)){
            atomicLong.expire(CacheKeyEnum.MACHINE_MESSAGE_NUM_INCR.getDuration());
        }

        if(Objects.equals(incremented, 65535)){
            atomicLong.set(0L);
            incremented = 0;
            atomicLong.expire(CacheKeyEnum.MACHINE_MESSAGE_NUM_INCR.getDuration());
        }
        return Convert.toShort(incremented);
    }


    /**
     * 测试日志分包代码
     */
    @PostConstruct
    public void logTest() {
        final String idCode = "123456789012345678";
        Runnable runnable = () -> {
            MDC.put(ConstEnum.ID_CODE.getCode(), idCode);
            log.info("业务日志，idCode:{}，ip={}", idCode, "0.0.0.0");
            MDC.clear();
        };
        new Thread(runnable).start();
    }

}
