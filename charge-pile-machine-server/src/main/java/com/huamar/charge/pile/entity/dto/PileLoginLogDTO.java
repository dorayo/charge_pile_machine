package com.huamar.charge.pile.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 设备登录记录
 * 2024/1/22
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PileLoginLogDTO extends BaseReqDTO {


    private String type;

    private Integer stationId;

    private LocalDateTime lastConTime;

    private String lastConIpAddr;

    private int conCount = 0;

    /**
     * 0 离线 1在线
     */
    private Integer status = 1;

    /**
     * 0 未认证 1 已认证
     */
    private Integer authStatus = 1;

    /**
     * ipAddrList
     */
    private Set<String> ipAddrList = new HashSet<>();

}
