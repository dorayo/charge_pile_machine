package com.huamar.charge.pile.entity.dto.platform;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 平台推送-设备心跳
 * 2023/08
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PileHeartbeatDTO extends PileBaseControlDTO {

    /**
     * 服务器时间
     */
    public String time;

    /**
     * 数据接收事件
     */
    private LocalDateTime dateTime;

    /**
     * 平台协议号
     */
    public Byte protocolNumber;

    /**
     * 预留字段
     */
    public Byte retain1;

    /**
     * 预留字段
     */
    public Byte retain2;

    /**
     * 预留字段
     */
    public Byte retain3;
}
