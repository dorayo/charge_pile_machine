package com.huamar.charge.pile.server.service.charge.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.huamar.charge.pile.entity.dto.charge.ChargeInfoDTO;
import com.huamar.charge.pile.entity.dto.command.YKCChargePrice;
import com.huamar.charge.pile.enums.CacheKeyEnum;
import com.huamar.charge.pile.server.service.charge.ChargeInfoService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 充电中订单信息
 * 2024/01/09
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Service
@RequiredArgsConstructor
public class ChargeInfoServiceImpl implements ChargeInfoService {

    private final static Cache<String, ChargeInfoDTO> cache = Caffeine.newBuilder()
            .initialCapacity(10000)
            .expireAfterAccess(Duration.ofHours(24))
            .build();

    private final static Cache<String, YKCChargePrice> priceCache = Caffeine.newBuilder()
            .initialCapacity(1024)
            .expireAfterAccess(Duration.ofDays(7))
            .build();

    private final RedissonClient redissonClient;

    /**
     * 获取充电中订单
     *
     * @param orderNumber orderNumber
     * @return ChargeInfoDTO
     */
    @Override
    public ChargeInfoDTO get(String orderNumber) {
        if(Objects.isNull(orderNumber)){
            return null;
        }

        ChargeInfoDTO var = cache.getIfPresent(orderNumber);
        if(Objects.nonNull(var)){
            return var;
        }

        String key = CacheKeyEnum.CHARGE_ORDER_INFO.joinKey(orderNumber);
        RBucket<ChargeInfoDTO> bucket = redissonClient.getBucket(key);

        return bucket.get();
    }


    /**
     * 更新,更新二级缓存
     *
     * @param chargeInfoDTO chargeInfoDTO
     */
    @Override
    public void put(ChargeInfoDTO chargeInfoDTO) {
        cache.put(chargeInfoDTO.getOrderNumber(), chargeInfoDTO);
        String key = CacheKeyEnum.CHARGE_ORDER_INFO.joinKey(chargeInfoDTO.getOrderNumber());
        RBucket<ChargeInfoDTO> bucket = redissonClient.getBucket(key);
        bucket.setAsync(chargeInfoDTO, CacheKeyEnum.CHARGE_ORDER_INFO.getDuration().getSeconds(), TimeUnit.SECONDS);
    }


    /**
     * 获取站点价格，未实现二级缓存
     *
     * @param stationId stationId
     * @return YKCChargePrice
     */
    @Override
    public YKCChargePrice getPriceInfoForCache(Integer stationId, Integer type) {
        if(Objects.isNull(stationId) || Objects.isNull(type)){
            return null;
        }
        return priceCache.getIfPresent(type + ":" + stationId);
    }

    /**
     * 存放电站电价
     *
     * @param stationId stationId
     *
     */
    @Override
    public void putPriceInfoForCache(Integer stationId, Integer type, YKCChargePrice price) {
        priceCache.put(type + ":" + stationId, price);
    }
}
