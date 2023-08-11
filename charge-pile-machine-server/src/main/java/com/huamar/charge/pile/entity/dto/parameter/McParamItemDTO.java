package com.huamar.charge.pile.entity.dto.parameter;

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
public class McParamItemDTO extends BaseDTO {

    /**
     * 参数编号
     */
    private Short id;

    /**
     * 参数数据长度
     */
    private Short paramLength;

    /**
     * 参数数据
     */
    private String paramData;
}
