package com.huamar.charge.pile.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 消息队列业务类型
 * 2023/08/09
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Getter
public enum MessageCodeEnum {

    PILE_UPDATE("PILE_UPDATE", "设备更新"),
    ELECTRICITY_PRICE("PILE_ELECTRICITY_PRICE_SEND", "电价下发"),
    ;
    private final String code;
    private final String desc;

    MessageCodeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static MessageCodeEnum getByCode(String code) {
        for (MessageCodeEnum e : values()) {
            if (Objects.equals(code, e.getCode())) {
                return e;
            }
        }
        return null;
    }
}
