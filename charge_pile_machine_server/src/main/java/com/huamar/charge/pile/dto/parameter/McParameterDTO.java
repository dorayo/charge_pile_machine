package com.huamar.charge.pile.dto.parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 设备远程参数控制
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McParameterDTO extends McBaseParameterDTO {

    /**
     * 参数个数
     */
    private byte paramNumber;

    /**
     * 参数内容
     */
    private List<McParamItemDTO> dataList;
}
