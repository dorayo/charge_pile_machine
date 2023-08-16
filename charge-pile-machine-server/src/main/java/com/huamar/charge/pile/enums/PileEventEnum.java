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
public enum PileEventEnum {

    CONFIG_EVENT("12", "充电参数配置信息 (0x12)"),

    HAND_SHAKE("14", "充电握手事件 (0x14)"),

    CHARGE_FINISH("16", "充电结束统计阶段事件 (0x16)"),

    ORDER_UPLOAD("18", "订单上传事件 (0x18)"),

    VIN_WHITE_LIST("19", "VIN白名单查询事件 (0x19)"),

    PILE_UPGRADE("20", "开始升级事件 (0x20)"),

    STARTUP("21", "充电桩主动请求重启 (0x21)"),

    ;
    private final String code;
    private final String desc;
    PileEventEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PileEventEnum getByCode(String code) {
        for (PileEventEnum e : values()) {
            if (Objects.equals(code, e.getCode())) {
                return e;
            }
        }
        return null;
    }
}
