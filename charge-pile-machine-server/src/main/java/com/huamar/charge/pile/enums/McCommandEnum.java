package com.huamar.charge.pile.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 远程控制
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Getter
public enum McCommandEnum {

    CHARGE("0002", "充电控制 (0x0002)"),

    QR_CODE("0006", "二维码下发 (0x0006)"),

    VIN_QUERY("0007", "vin白名单 (0x0007)"),

    ELECTRICITY_PRICE("0003", "电价下发 (0x0003)"),

    ORDER_APPOINTMENT("0005", "充电预约 (0x0005)"),

    CARD_QUERY("0004", "卡查询结果 (0x0004)"),

    /**
     * 国花协议 背光灯
     */
    CUSTOM_AD_LAMP("CUSTOM:0004", "背光灯时间 (0x0004)");

    ;
    private final String code;
    private final String desc;
    McCommandEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static McCommandEnum getByCode(String code) {
        for (McCommandEnum e : values()) {
            if (Objects.equals(code, e.getCode())) {
                return e;
            }
        }
        return null;
    }
}
