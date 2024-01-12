package com.huamar.charge.pile.enums;

import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import io.netty.util.AttributeKey;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NAttrKeys {

    // 充电桩计费模型下发
    static final public AttributeKey<ProtocolCPacket> PROTOCOL_C_0x09_PACKET = AttributeKey.valueOf("PROTOCOL_C_0x09_PACKET");
    static final public AttributeKey<ProtocolCPacket> PROTOCOL_C_LATEST_PACKET = AttributeKey.valueOf("PROTOCOL_C_LATEST_PACKET");
    static final public AttributeKey<Integer> PROTOCOL_C_LATEST_ORDER_V = AttributeKey.valueOf("PROTOCOL_C_LATEST_ORDER_V");
    static final public AttributeKey<ConcurrentHashMap<Integer, Integer>> GUN_ORDER_MAP = AttributeKey.valueOf("GUN_ORDER_MAP");
    static final public AttributeKey<byte[]> ID_BODY = AttributeKey.valueOf("ID_BODY");

    // 自增序列号
    static final public AttributeKey<AtomicInteger> SERIAL_NUMBER = AttributeKey.valueOf("SERIAL_NUMBER");

    /**
     * 获取流水号
     * @param sessionChannel sessionChannel
     * @return Short
     */
    public static Short getSerialNumber(SimpleSessionChannel sessionChannel){
        AtomicInteger orderV = sessionChannel.channel().channel().attr(NAttrKeys.SERIAL_NUMBER).get();
        if(Objects.isNull(orderV)){
            orderV = new AtomicInteger(0);
            sessionChannel.channel().channel().attr(NAttrKeys.SERIAL_NUMBER).set(orderV);
        }

        int incrementAndGet = orderV.incrementAndGet();
        if(incrementAndGet >= Short.MAX_VALUE){
            orderV.set(0);
            incrementAndGet = orderV.incrementAndGet();
        }
        return (short) incrementAndGet;
    }

}
