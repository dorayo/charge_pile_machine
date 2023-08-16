package com.huamar.charge.pile.server.service;

import cn.hutool.core.convert.Convert;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.protocol.DataPacket;
import com.huamar.charge.pile.server.MachineServer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;
import org.tio.core.intf.Packet;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 设备业务上下文
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
@Slf4j
public class MachineContext {

    private final AtomicInteger messageNumber = new AtomicInteger(0);

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
        dataPacket.setMsgNumber(getMessageNumber());
        return Tio.sendToBsId(MachineServer.serverTioConfig, new String(dataPacket.getIdCode()), dataPacket);
    }

    /**
     * 获取消息流水号
     *
     * @return Short
     */
    public Short getMessageNumber(){
        int andIncrement = messageNumber.getAndIncrement();
        if(Objects.equals(andIncrement, 65535)){
            messageNumber.set(0);
            return Convert.toShort(messageNumber.getAndIncrement());
        }
        return Convert.toShort(andIncrement);
    }


    @PostConstruct
    public void logTest() {
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                MDC.put(ConstEnum.ID_CODE.getCode(), "471000220714302005");
                log.info("业务日志，idCode:{}，ip={}", "471000220714302005", "0.0.0.0");
                TimeUnit.SECONDS.sleep(1);
                MDC.clear();
            }
        }).start();
    }

}
