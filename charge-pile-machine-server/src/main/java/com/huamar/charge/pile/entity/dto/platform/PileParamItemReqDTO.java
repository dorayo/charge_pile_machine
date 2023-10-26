package com.huamar.charge.pile.entity.dto.platform;

import com.huamar.charge.pile.entity.dto.parameter.PileParamItemDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 远程参数下发
 * 2023/08
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PileParamItemReqDTO extends PileBaseControlDTO {

    /**
     * 远程参数集合
     */
    List<PileParamItemDTO> list;
}
