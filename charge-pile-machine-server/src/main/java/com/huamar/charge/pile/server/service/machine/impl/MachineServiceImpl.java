package com.huamar.charge.pile.server.service.machine.impl;

import com.huamar.charge.common.api.vo.Result;
import com.huamar.charge.pile.api.ISysPileApi;
import com.huamar.charge.pile.api.dto.PileDTO;
import com.huamar.charge.pile.server.service.machine.MachineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    protected final ISysPileApi iSysPileApi;

    /**
     * 获取设备
     *
     * @param idCode idCode
     * @return PileDTO
     */
    @Override
    public PileDTO getPile(String idCode) {
        //Result<PileDTO> result = iSysPileApi.getByCode(idCode);
        Result<PileDTO> result = Result.OK(null);
        if(result.isSuccess()){
            return result.getResult();
        }
        log.info("getPile error dada:{}", result);
        return null;
    }


    /**
     * 获取二维码地址
     * //TODO 二维码配置
     *
     * @return String
     */
    @Override
    public String getQrCode() {
        return "1234567890";
    }


}
