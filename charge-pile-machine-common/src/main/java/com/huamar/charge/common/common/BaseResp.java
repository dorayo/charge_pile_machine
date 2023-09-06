package com.huamar.charge.common.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 响应
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Data
public class BaseResp implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 6.设备识别码（18）
     */
    protected String idCode;

}
