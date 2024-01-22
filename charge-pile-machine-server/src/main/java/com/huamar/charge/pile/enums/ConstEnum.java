package com.huamar.charge.pile.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 常量枚举
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Getter
public enum ConstEnum {

    MACHINE_ID("MACHINE_ID", "设备ID"),

    X_SESSION_ID("X_SESSION_ID", "会话ID"),

    X_LOGIN_TIME("X_LOGIN_TIME", "设备登录时间"),

    PROTOCOL_C_PRICE_ORDER_ID("PROTOCOL_C_PRICE_ORDER_ID", "电价单号"),

    YKC_CHARGE_PRICE("YKC_CHARGE_PRICE", "YKC 电价信息"),

    STATION_ID("STATION_ID", "电站ID"),

    ELE_CHARG_TYPE("ELE_CHARG_TYPE", "充电电价类型"),

    // MDC log使用
    ID_CODE("idCode", "设备ID"),

    BASE("压制警告", "压制警告"),
    ;


    private final String code;

    private final String desc;
    ConstEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ConstEnum getByCode(String code) {
        for (ConstEnum e : values()) {
            if (Objects.equals(code, e.getCode())) {
                return e;
            }
        }
        throw new RuntimeException("enum not exists.");
    }
}
