package com.huamar.charge.pile.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 日志枚举，根据不同的logger 分割log文件存储
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Getter
public enum LoggerEnum {

    HEARTBEAT_LOGGER("MachineHeartbeatLogger", "设备心跳日志"),

    MACHINE_PACKET_LOGGER("MachinePacketLogger", "设备数据包日志"),

    BASE("压制警告", "压制警告"),

    ;
    private final String code;
    private final String desc;
    LoggerEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static LoggerEnum getByCode(String code) {
        for (LoggerEnum e : values()) {
            if (Objects.equals(code, e.getCode())) {
                return e;
            }
        }
        throw new RuntimeException("enum not exists.");
    }
}
