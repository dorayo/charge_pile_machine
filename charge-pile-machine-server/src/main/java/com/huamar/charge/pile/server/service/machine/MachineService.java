package com.huamar.charge.pile.server.service.machine;

import com.huamar.charge.pile.api.dto.PileDTO;

/**
 * 设备端接口
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
public interface MachineService {

    /**
     * 获取设备
     *
     * @param idCode idCode
     * @return PileDTO
     */
    PileDTO getPile(String idCode);

    /**
     * 从缓存获取
     * @param idCode idCode
     * @return PileDTO
     */
    PileDTO getCache(String idCode);

    /**
     * 存放缓存
     *
     * @param idCode  idCode
     * @param pileDTO pileDTO
     */
    void putCache(String idCode, PileDTO pileDTO);

    /**
     * 释放缓存
     * @param idCode idCode
     */
    void removeCache(String idCode);

    /**
     * 获取二维码地址
     *
     * @return String
     */
    String getQrCode();
}
