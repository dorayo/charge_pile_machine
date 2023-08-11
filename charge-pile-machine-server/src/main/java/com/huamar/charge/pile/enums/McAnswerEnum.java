package com.huamar.charge.pile.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 应答类型
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Getter
public enum McAnswerEnum {

    COMMON("30", "通用应答(0x30)"),

    AUTH("33", "权鉴(0x33)"),

    UPGRADE("37", "远程升级(0x37)"),
    ;
    private final String code;
    private final String desc;
    McAnswerEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static McAnswerEnum getByCode(String code) {
        for (McAnswerEnum e : values()) {
            if (Objects.equals(code, e.getCode())) {
                return e;
            }
        }
        return null;
    }
}
