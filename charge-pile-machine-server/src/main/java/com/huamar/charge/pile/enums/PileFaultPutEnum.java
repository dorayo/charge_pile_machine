package com.huamar.charge.pile.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 事件汇报枚举
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Getter
public enum PileFaultPutEnum {

    CONFIG_EVENT("1", "电表故障"),
    SCREEN_FAULT_EVENT("2", "显示器故障"),
    ;
    private final String code;
    private final String desc;

    PileFaultPutEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PileFaultPutEnum getByCode(String code) {
        for (PileFaultPutEnum e : values()) {
            if (Objects.equals(code, e.getCode())) {
                return e;
            }
        }
        return null;
    }
}
