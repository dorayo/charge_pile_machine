package com.huamar.charge.pile.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 终端数据汇报枚举
 * 2023/06/11
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Getter
public enum McDataUploadEnum {

    COMMON_0X08("08", "充电桩实时状态信息表 (0x08)"),

    COMMON_0X28("28", "充电桩实时状态信息表 (0x28)"),

    COMMON_0X0A("0A", "充电阶段信息表 (0x0A)"),

    COMMON_0X2A("2A", "充电阶段信息表 (0x2A)"),

    ;
    private final String code;
    private final String desc;
    McDataUploadEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static McDataUploadEnum getByCode(String code) {
        for (McDataUploadEnum e : values()) {
            if (Objects.equals(code, e.getCode())) {
                return e;
            }
        }
        return null;
    }
}
