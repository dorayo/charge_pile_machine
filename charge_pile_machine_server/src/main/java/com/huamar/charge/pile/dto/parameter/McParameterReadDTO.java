package com.huamar.charge.pile.dto.parameter;

import com.huamar.charge.pile.common.codec.BCD;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备远程参数控制 读取参数
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McParameterReadDTO extends McBaseParameterDTO {

    /**
     * 服务器时间
     */
    public BCD time;
    /**
     * 预留字段
     */
    public int retain;
}
