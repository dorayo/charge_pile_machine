package com.huamar.charge.pile.entity.dto;

import com.huamar.charge.common.common.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * BaseReqDTO
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BaseReqDTO extends BaseDTO {

    public String idCode;
}
