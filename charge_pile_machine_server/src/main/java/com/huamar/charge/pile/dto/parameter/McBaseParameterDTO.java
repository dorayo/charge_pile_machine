package com.huamar.charge.pile.dto.parameter;

import com.huamar.charge.pile.common.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备远程参数控制
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McBaseParameterDTO extends BaseDTO {

    /**
     * 设备唯一编码
     */
    private String idCode;
}
