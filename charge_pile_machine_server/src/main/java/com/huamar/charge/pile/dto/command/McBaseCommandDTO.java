package com.huamar.charge.pile.dto.command;

import com.huamar.charge.pile.common.BaseDTO;
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
public abstract class McBaseCommandDTO extends BaseDTO {

    /**
     * 设备唯一编码
     */
    public String idCode;

    /**
     * 控制命令类型
     */
    public short typeCode;

    /**
     * 控制命令数据长度
     */
    public byte fieldsByteLength = 0;

}
