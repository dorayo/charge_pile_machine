package com.huamar.charge.pile.server.service.charge;

import com.huamar.charge.pile.entity.dto.charge.ChargeInfoDTO;
import com.huamar.charge.pile.entity.dto.command.YKCChargePrice;

/**
 * 充电中订单信息
 * 2024/01/09
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
public interface ChargeInfoService {

    /**
     * 获取设备充电订单
     *
     * @param orderNumber orderNumber
     * @return ChargeInfoDTO
     */
    ChargeInfoDTO get(String orderNumber);


    /**
     * 更新
     *
     * @param chargeInfoDTO chargeInfoDTO
     */
    void put(ChargeInfoDTO chargeInfoDTO);



    /**
     * 获取站点价格，未实现二级缓存
     *
     * @param stationId stationId
     * @param type 类型 1 直流 2交流
     * @return YKCChargePrice

     */
    YKCChargePrice getPriceInfoForCache(Integer stationId, Integer type);


    /**
     * 存放电站电价
     * @param stationId stationId
     * @param price price
     * @param type 类型 1 直流 2交流
     */
    void putPriceInfoForCache(Integer stationId, Integer type, YKCChargePrice price);
}
