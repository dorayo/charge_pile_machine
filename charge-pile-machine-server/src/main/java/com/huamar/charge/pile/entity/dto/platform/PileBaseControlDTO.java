package com.huamar.charge.pile.entity.dto.platform;

import com.huamar.charge.common.common.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 远程控制基础类
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class PileBaseControlDTO extends BaseDTO {

    /**
     * 设备唯一编码
     */
    public String idCode;

}
