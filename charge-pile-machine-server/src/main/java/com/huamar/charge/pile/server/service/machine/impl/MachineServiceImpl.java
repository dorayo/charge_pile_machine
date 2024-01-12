package com.huamar.charge.pile.server.service.machine.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.huamar.charge.common.api.vo.Result;
import com.huamar.charge.pile.api.ISysPileApi;
import com.huamar.charge.pile.api.dto.PileDTO;
import com.huamar.charge.pile.config.PileMachineProperties;
import com.huamar.charge.pile.server.service.machine.MachineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 设备端接口
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MachineServiceImpl implements MachineService {



    /**
     * 微服务设备Api
     */
    protected final ISysPileApi iSysPileApi;

    /**
     * 设备端属性配置
     */
    private final PileMachineProperties pileMachineProperties;

    private final static Cache<String, PileDTO> cache = Caffeine.newBuilder()
            .initialCapacity(2000)
            .expireAfterAccess(Duration.ofMinutes(10))
            .build();

    /**
     * 获取设备
     *
     * @param idCode idCode
     * @return PileDTO
     */
    @Override
    public PileDTO getPile(String idCode) {
        Result<PileDTO> result = iSysPileApi.getByCode(idCode);
        if(result.isSuccess()){
            return result.getResult();
        }
        log.info("getPile error dada:{}", result);
        return null;
    }


    @Override
    public PileDTO getCache(String idCode) {
        return cache.getIfPresent(idCode);
    }

    @Override
    public void putCache(String idCode, PileDTO pileDTO) {
        cache.put(idCode, pileDTO);
    }

    @Override
    public void removeCache(String idCode) {
        cache.invalidate(idCode);
    }


    /**
     * 获取二维码地址
     *
     * @return String
     */
    @Override
    public String getQrCode() {
        return pileMachineProperties.getQrCodeUrl();
    }

}
