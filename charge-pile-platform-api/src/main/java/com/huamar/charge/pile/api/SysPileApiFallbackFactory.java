package com.huamar.charge.pile.api;


import com.huamar.charge.common.api.vo.Result;
import com.huamar.charge.pile.api.dto.PileDTO;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * SysPileApiFallbackFactory
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
public class SysPileApiFallbackFactory implements FallbackFactory<ISysPileApi> {

    @Override
    public ISysPileApi create(Throwable throwable) {
        return new ISysPileApi() {
            @Override
            public Result<PileDTO> getByCode(String pileCode) {
                return null;
            }
        };
    }
}