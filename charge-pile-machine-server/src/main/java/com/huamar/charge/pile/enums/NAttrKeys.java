package com.huamar.charge.pile.enums;

import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import io.netty.util.AttributeKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NAttrKeys {
    static final public AttributeKey<ProtocolCPacket> PROTOCOL_C_0x09_PACKET = AttributeKey.valueOf("PROTOCOL_C_0x09_PACKET");
    static final public AttributeKey<ProtocolCPacket> PROTOCOL_C_LATEST_PACKET = AttributeKey.valueOf("PROTOCOL_C_LATEST_PACKET");
    static final public AttributeKey<Integer> PROTOCOL_C_LATEST_ORDER_V = AttributeKey.valueOf("PROTOCOL_C_LATEST_ORDER_V");
    static final public AttributeKey<ConcurrentHashMap<Integer, Integer>> GUN_ORDER_MAP = AttributeKey.valueOf("GUN_ORDER_MAP");
}
