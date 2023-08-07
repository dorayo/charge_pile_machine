package com.huamar.charge.pile.dto;

import com.huamar.charge.pile.dto.parameter.McBaseParameterDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 远程参数查询请求
 * date 2023/07/25
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McParameterReqDTO extends McBaseParameterDTO {

    /**
     * 消息流水号
     */
    private Short msgNumber;

    /**
     * 电表类型
     */
    private Integer meterType;

    /**
     * 模块类型
     */
    private Integer moduleType;

    /**
     * 单模块输出电流
     */
    private Integer moduleCurrent;

    /**
     * 模块个数
     */
    private Integer moduleNum;

    /**
     * 锁定天数
     */
    private Integer lockDay;

    /**
     * 1枪锁定信息
     */
    private Integer gun1Lock;

    /**
     * 2枪锁定信息
     */
    private Integer gun2Lock;

    /**
     * 3枪锁定信息
     */
    private Integer gun3Lock;

    /**
     * 4枪锁定信息
     */
    private Integer gun4Lock;

    /**
     * 5枪锁定信息
     */
    private Integer gun5Lock;

    /**
     * 6枪锁定信息
     */
    private Integer gun6Lock;

    /**
     * 7枪锁定信息
     */
    private Integer gun7Lock;

    /**
     * 8枪锁定信息
     */
    private Integer gun8Lock;

    /**
     * 9枪锁定信息
     */
    private Integer gun9Lock;

    /**
     * 10枪锁定信息
     */
    private Integer gun10Lock;

    /**
     * 1枪限流
     */
    private Integer gun1Limit;

    /**
     * 2枪限流
     */
    private Integer gun2Limit;

    /**
     * 3枪限流
     */
    private Integer gun3Limit;

    /**
     * 4枪限流
     */
    private Integer gun4Limit;

    /**
     * 5枪限流
     */
    private Integer gun5Limit;

    /**
     * 6枪限流
     */
    private Integer gun6Limit;

    /**
     * 7枪限流
     */
    private Integer gun7Limit;

    /**
     * 8枪限流
     */
    private Integer gun8Limit;

    /**
     * 9枪限流
     */
    private Integer gun9Limit;

    /**
     * 10枪限流
     */
    private Integer gun10Limit;

    /**
     * 充电枪个数
     */
    private Integer gunNum;

    /**
     * 后台类型
     */
    private Integer serverType;

    /**
     * 计损比例
     */
    private Double scale;

    /**
     * 消耗流量(TB)
     */
    private Integer trafficTb;

    /**
     * 消耗流量(GB)
     */
    private Integer trafficGb;

    /**
     * 消耗流量(MB)
     */
    private Integer trafficMb;

    /**
     * 消耗流量(KB)
     */
    private Integer trafficKb;

    /**
     * 后台协议版本
     */
    private Integer version;

    /**
     * 交流开机模式
     */
    private Integer slowStartMode;

    /**
     * 交流PWM占空比
     */
    private Integer slowDutyCycle;

    /**
     * 交流AD采样系数
     */
    private Double slowSampling;

    /**
     * 低电流关机时间
     */
    private Integer lowCurrentEndTime;

    /**
     * 预留字段1
     */
    private int retain1;

    /**
     * 预留字段2
     */
    private int retain2;

}
