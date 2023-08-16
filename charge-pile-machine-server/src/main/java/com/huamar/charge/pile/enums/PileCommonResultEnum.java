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
public enum PileCommonResultEnum {

    SUCCESS("0000", "命令执行成功"),

    FAIL("0001", "命令执行失败"),

    GUN_STATE_ERROR("0002", "远程控制应答-枪状态错误 (0x0002)"),

    ELECTRIC_METER_FAULT("0003","远程控制应答-电表故障 (0x0003)"),

    BALANCE_LESS_THAN("0004","远程控制应答-余额不足 (0x0004)"),

    MONEY_LESS_THAN("0005","远程控制应答-按金额-金额太小 (0x0005)"),

    DEGREE_LESS_THAN("0006","远程控制应答-按度数-度数太小 (0x0006)"),

    DEGREE_GUN_CODE_ERROR("0007","远程控制应答-枪号错误 (0x0007)"),

    TERMINAL_UNSUPPORTED_FUNCTION("0201","远程参数下发-终端不支持此参数 (0x00A0)"),

    TERMINAL_UNSUPPORTED_A_PARAM("0202","远程参数下发-终端不支持某个参数 (0x0202)"),

    UNKNOWN_REASON("0203","远程参数下发-位置原因导致参数下发失败 (0x0203)"),

    @SuppressWarnings("SpellCheckingInspection")
    INVALID("FFFF","远程参数下发-位置原因导致参数下发失败 (0xFFFF)"),

    UNKNOWN("UNKNOWN","未知返回结果 UNKNOWN"),

    ;
    private final String code;
    private final String desc;
    PileCommonResultEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PileCommonResultEnum getByCode(String code) {
        for (PileCommonResultEnum e : values()) {
            if (Objects.equals(code, e.getCode())) {
                return e;
            }
        }
        return null;
    }
}
