package com.huamar.charge.pile.enums;

import lombok.Data;


public enum ProtocolCFromEquipmentBodyType {
    Auth(0x01, "鉴权"),

    Heartbeat(0x03, "心跳");

    private final byte code;

    private final String desc;

    ProtocolCFromEquipmentBodyType(int code, String desc) {
        this.code = (byte) code;
        this.desc = desc;
    }

}
