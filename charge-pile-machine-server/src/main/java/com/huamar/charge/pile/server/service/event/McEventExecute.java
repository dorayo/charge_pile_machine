package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.pile.entity.dto.McEventBaseDTO;
import com.huamar.charge.pile.entity.dto.McEventReqDTO;
import com.huamar.charge.pile.enums.McEventEnum;

/**
 * 设备端数据汇报接口
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public interface McEventExecute {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    McEventEnum getCode();

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    void execute(McEventReqDTO reqDTO);

    /**
     * 解析元数据
     * @param reqDTO reqDTO
     * @return McEventBaseDTO
     */
    McEventBaseDTO parse(McEventReqDTO reqDTO);
}
