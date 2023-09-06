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

    PILE_HEART_BEAT("PILE_HEART_BEAT", "设备心跳"),

    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 远程控制 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    PILE_UPDATE("PILE_UPDATE", "设备更新"),

    PILE_START_CHARGE("PILE_START_CHARGE", "设备开启充电"),

    PILE_STOP_CHARGE("PILE_STOP_CHARGE", "设备开启充电"),

    ELECTRICITY_PRICE("PILE_ELECTRICITY_PRICE_SEND", "电价下发"),

    PILE_PARAMETER_UPDATE("PILE_PARAMETER_UPDATE", "设备远程参数下发"),
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< 远程控制 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<



    // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 远程控制应答 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    PILE_MESSAGE_COMMON_RESP("PILE_MESSAGE_COMMON_RESP", "远程控制请求应答"),
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< 远程控制应答 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<




    // >>>>>>>>>>>>>>>> 事件汇报 >>>>>>>>>>>>>>>>
    EVENT_CONFIG_EVENT("EVENT_CONFIG_ARG", "充电参数配置信息"),





    // >>>>>>>>>>>>>>>>>>>>>>> 数据汇报 >>>>>>>>>>>>>>>>>>>>>>>
    PILE_ONLINE("PILE_ONLINE", "充电桩实时状态"),
    // <<<<<<<<<<<<<<<<<<<<<<<< 数据汇报 <<<<<<<<<<<<<<<<<<<<<<<<


    WARNING("WARNING", "压制警告");

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
