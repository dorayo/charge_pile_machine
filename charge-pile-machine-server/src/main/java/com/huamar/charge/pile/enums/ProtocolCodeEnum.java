package com.huamar.charge.pile.enums;

import com.huamar.charge.common.util.HexExtUtil;
import lombok.Getter;

import java.util.Objects;

/**
 * 协议消息ID枚举
 * 2023/06/11
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Getter
public enum ProtocolCodeEnum {

    HEART_BEAT("31", "终端心跳 (0x31)"),

    AUTH("32", "终端鉴权 (0x33)"),

    AUTH_ANSWER("33", "权鉴应答(0x33)"),

    DATA_UPLOAD("34", "数据汇报 (0x34)"),

    EVENT("36", "事件汇报 (0x36)"),

    COMMON_ACK("30", "通用应答 (0x30)"),

    COMMON_SEND("35", "(0x35 远程控制（指令下发）)"),

    PARAMETER_SEND("40", "远程控制 参数下发 (0x40)"),

    PARAMETER_READ_SEND("41", "远程控制 参数下发 (0x41)"),

    PARAMETER_PARAMETER("42", "远程参数读取 (0x42)"),

    FAULT("38", "故障信息上报 (0x38)"),

    UPGRADE("37", "远程升级 (0x37)"),

    ;
    private final String code;
    private final String desc;
    ProtocolCodeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * code byte
     * @return byte codeByte
     */
    public byte codeByte(){
        return HexExtUtil.decodeHex(this.code)[0];
    }

    public static ProtocolCodeEnum getByCode(String code) {
        for (ProtocolCodeEnum e : values()) {
            if (Objects.equals(code, e.getCode())) {
                return e;
            }
        }
        throw new RuntimeException("enum not exists.");
    }
}
