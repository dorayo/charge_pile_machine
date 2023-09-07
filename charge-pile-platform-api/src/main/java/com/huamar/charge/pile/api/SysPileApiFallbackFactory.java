package com.huamar.charge.pile.api;


import com.huamar.charge.common.api.vo.Result;
import com.huamar.charge.pile.api.dto.PileDTO;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciithemes.TA_GridThemes;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

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
            public Result<PileDTO> getByCode(@RequestParam(name = "pileCode", required = true) String pileCode) {
                return null;
            }
        };
    }

    public static void main(String[] args) {
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("row 1 col 1", "row 1 col 2");
        at.addRule();
        at.addRow("row 2 col 1", "row 2 col 2");
        at.addRule();

        String rend = at.render();
        System.out.println(rend);
    }
}