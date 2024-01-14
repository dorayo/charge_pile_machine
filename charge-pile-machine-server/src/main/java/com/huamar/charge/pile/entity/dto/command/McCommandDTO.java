package com.huamar.charge.pile.entity.dto.command;

import com.huamar.charge.common.common.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 远程控制基础类
 * date 2023/07/25
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McCommandDTO extends BaseDTO {

    /**
     * 控制命令类型
     */
    private short typeCode;

    /**
     * 内容长度
     */
    private byte dataLength;

    /**
     * 内容
     */
    private byte[] data;

    public McCommandDTO(short typeCode, byte dataLength, byte[] data) {
        this.typeCode = typeCode;
        this.dataLength = dataLength;
        this.data = data;
    }
}
