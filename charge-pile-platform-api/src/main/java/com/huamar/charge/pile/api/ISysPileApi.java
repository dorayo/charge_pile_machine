package com.huamar.charge.pile.api;


import com.huamar.charge.common.ServiceNameConstants;
import com.huamar.charge.common.api.vo.Result;
import com.huamar.charge.pile.api.dto.PileDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 平台服务接口
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
@FeignClient(contextId = "SysPileApi", name = ServiceNameConstants.SERVICE_SYSTEM, fallbackFactory = SysPileApiFallbackFactory.class)
public interface ISysPileApi {


    /**
     *
     * @param pileCode pileCode
     * @return Result<PileDTO>
     */
    @GetMapping(value = "/sys/communication/pile/get")
    Result<PileDTO> getByCode(@RequestParam(name = "pileCode", required = true) String pileCode);
}
