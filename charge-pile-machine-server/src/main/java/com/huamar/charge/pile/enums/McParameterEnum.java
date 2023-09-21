package com.huamar.charge.pile.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 远程控制
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Getter
public enum McParameterEnum {

    SEND("40", "远程参数下发 (0x40)"),

    READ("41", "远程参数读取下发 (0x41)"),

    READ_CONFIG("42", "远程参数读取 (0x42)"),
    ;
    private final String code;
    private final String desc;

    McParameterEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static McParameterEnum getByCode(String code) {
        for (McParameterEnum e : values()) {
            if (Objects.equals(code, e.getCode())) {
                return e;
            }
        }
        return null;
    }
}
