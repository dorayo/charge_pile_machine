package com.huamar.charge.pile.entity.dto.platform;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 电价下发控制对象
 * 2023/08
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PileElectricityPriceDTO extends PileBaseControlDTO {

    List<ChargPriceDTO> list;

}
