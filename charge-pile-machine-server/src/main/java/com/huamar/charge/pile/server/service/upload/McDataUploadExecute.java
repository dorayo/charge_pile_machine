package com.huamar.charge.pile.server.service.upload;

import com.huamar.charge.common.common.codec.BCD;
import com.huamar.charge.pile.entity.dto.MachineDataUpItem;
import com.huamar.charge.pile.enums.McDataUploadEnum;

import java.util.List;

/**
 * 设备端数据汇报接口
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public interface McDataUploadExecute {


    /**
     * 协议编码
     * @return ProtocolCodeEnum
     */
    McDataUploadEnum getCode();

    void execute(BCD time, List<MachineDataUpItem> list);
}
