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
public enum McFaultPutEnum {

    CONFIG_EVENT("1", "电表故障"),
    SCREEN_FAULT_EVENT("2", "显示器故障"),
    ;
    private final String code;
    private final String desc;

    McFaultPutEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static McFaultPutEnum getByCode(String code) {
        for (McFaultPutEnum e : values()) {
            if (Objects.equals(code, e.getCode())) {
                return e;
            }
        }
        return null;
    }
}
