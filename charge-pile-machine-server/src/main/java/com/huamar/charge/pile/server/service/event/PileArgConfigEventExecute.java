package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.pile.entity.dto.event.PileEventBaseDTO;
import com.huamar.charge.pile.entity.dto.event.PileEventReqDTO;
import com.huamar.charge.pile.enums.PileEventEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备端数据汇报接口-充电参数配置信息
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
@Slf4j
public class PileArgConfigEventExecute implements PileEventExecute {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public PileEventEnum getCode() {
        return PileEventEnum.CONFIG_EVENT;
    }

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void execute(PileEventReqDTO reqDTO) {
        //TODO 业务实现
        log.info("事件汇报：{}", getCode().getDesc());
    }

    /**
     * 解析元数据
     *
     * @param reqDTO reqDTO
     * @return McEventBaseDTO
     */
    @Override
    public PileEventBaseDTO parse(PileEventReqDTO reqDTO) {
        return null;
    }


}
