package com.huamar.charge.pile.entity.dto.platform;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 平台电价
 * date 2023/08/01
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class ChargPriceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Integer id;
    /**
     * 电站id
     */
    private Integer stationId;
    /**
     * 电价描述
     */
    private String chargDetails;
    /**
     * 电价序号
     */
    private Integer sortNum;
    /**
     * 电费（元/度）
     */
    private BigDecimal charge;
    /**
     * 优惠前的价格
     */
    private BigDecimal preferential;
    /**
     * 电费时间段起始时间
     */
    private String startTime;
    /**
     * 电费时间段结束时间
     */
    private String endTime;
    /**
     * 服务费
     */
    private BigDecimal serviceCharge;

    /**
     * 电价类型  0.直流  1.交流
     */
    private Integer chargType;
}
