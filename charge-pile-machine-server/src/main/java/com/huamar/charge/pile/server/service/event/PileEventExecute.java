package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.pile.entity.dto.MachineDataUpItem;
import com.huamar.charge.pile.entity.dto.event.PileChargeFinishEventDTO;
import com.huamar.charge.pile.entity.dto.event.PileEventBaseDTO;
import com.huamar.charge.pile.entity.dto.event.PileEventReqDTO;
import com.huamar.charge.pile.enums.PileEventEnum;

/**
 * 设备端数据汇报接口
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public interface PileEventExecute {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    PileEventEnum getCode();

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    void execute(PileEventReqDTO reqDTO);

    /**
     * 解析元数据
     * @param reqDTO reqDTO
     * @return McEventBaseDTO
     */
    PileEventBaseDTO parse(PileEventReqDTO reqDTO);

}
