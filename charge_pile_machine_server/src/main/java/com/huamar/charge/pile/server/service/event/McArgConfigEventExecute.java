package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.pile.dto.McEventBaseDTO;
import com.huamar.charge.pile.dto.McEventReqDTO;
import com.huamar.charge.pile.enums.McEventEnum;
import org.springframework.stereotype.Component;

/**
 * 设备端数据汇报接口-充电参数配置信息
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
public class McArgConfigEventExecute implements McEventExecute{


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McEventEnum getCode() {
        return McEventEnum.CONFIG_EVENT;
    }

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void execute(McEventReqDTO reqDTO) {
        //TODO 业务实现
    }

    /**
     * 解析元数据
     *
     * @param reqDTO reqDTO
     * @return McEventBaseDTO
     */
    @Override
    public McEventBaseDTO parse(McEventReqDTO reqDTO) {
        return null;
    }


}
