package com.huamar.charge.pile.enums;

import lombok.Getter;

import java.time.Duration;
import java.util.Objects;

/**
 * 缓存空间枚举
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Getter
public enum CacheKeyEnum {

    MACHINE_MESSAGE_NUM_INCR("charge:mc:msg:number:incr", "设备流水号滚动缓存", Duration.ofDays(365)),

    MACHINE_COMMAND_ANSWER("charge:mc:cmd:answer:", "设备命令应答缓存 key=idCode-messageNumber", Duration.ofMinutes(10)),

    WARNING("压制警告", "压制警告", Duration.ofMinutes(1)),

    MACHINE_SERVICE_PRICE("charge:price", "服务费", Duration.ofDays(30)),

    /**
     * 充电中订单信息 二级缓存 LOCAL + REDIS Cache
     */
    CHARGE_ORDER_INFO("charge:mc:order_info", "充电中订单信息", Duration.ofHours(30)),

    CHARGE_PILE_AUTH_LOG("charge:mc:auth-log", "设备认证记录", Duration.ofHours(30)),

    CHARGE_PILE_INVALID_CONNECTION("charge:mc:invalid-connection", "设备无效连接", Duration.ofHours(30)),

    ;
    private final String code;

    private final String desc;

    /**
     * 过期时间
     */
    private final Duration duration;

    CacheKeyEnum(String code, String desc, Duration duration) {
        this.code = code;
        this.desc = desc;
        this.duration = duration;
    }

    public static CacheKeyEnum getByCode(String code) {
        for (CacheKeyEnum e : values()) {
            if (Objects.equals(code, e.getCode())) {
                return e;
            }
        }
        throw new RuntimeException("enum not exists.");
    }


    /**
     * 拼接缓存名字
     * @param text text
     * @return String
     */
    public String joinKey(String text){
        return this.getCode() + ":" + text;
    }
}
