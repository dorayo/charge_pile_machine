package com.huamar.charge.pile.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 通用应答响应处理
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Getter
public enum McCommonResultEnum {

    GUN_STATE_ERROR("2", "远程控制应答-枪状态错误 (0x0002)"),
    ELECTRIC_METER_FAULT("3","远程控制应答-电表故障 (0x0003)"),
    ;
    private final String code;
    private final String desc;
    McCommonResultEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static McCommonResultEnum getByCode(String code) {
        for (McCommonResultEnum e : values()) {
            if (Objects.equals(code, e.getCode())) {
                return e;
            }
        }
        return null;
    }
}
