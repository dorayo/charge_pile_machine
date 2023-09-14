package com.huamar.charge.common.protocol;

import lombok.Data;

/**
 * 网络消息包
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Data
public abstract class BasePacket {


    /**
     * 获取
     * @return byte[]
     */
    abstract byte[] getBytes();
}
