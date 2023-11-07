package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.pile.entity.dto.MachineDataUpItem;
import com.huamar.charge.pile.entity.dto.event.PileChargeFinishEventDTO;
import com.huamar.charge.pile.entity.dto.event.PileEventReqDTO;
import com.huamar.charge.pile.entity.dto.event.PileVinWhiteListEventDTO;
import com.huamar.charge.pile.enums.PileEventEnum;
import com.huamar.charge.common.protocol.DataPacketReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 设备端数据汇报接口-订单上传事件
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
public class PileVinWhiteListEventExecute implements PileEventExecute {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public PileEventEnum getCode() {
        return PileEventEnum.VIN_WHITE_LIST;
    }

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void execute(PileEventReqDTO reqDTO) {
        log.info("事件汇报：{}", getCode().getDesc());
        PileVinWhiteListEventDTO eventDTO = this.parse(reqDTO);
    }

    /**
     * 解析元数据
     *
     * @param reqDTO reqDTO
     * @return McEventBaseDTO
     */
    @Override
    public PileVinWhiteListEventDTO parse(PileEventReqDTO reqDTO) {
        DataPacketReader reader = new DataPacketReader(reqDTO.getEventData());
        PileVinWhiteListEventDTO eventDTO = new PileVinWhiteListEventDTO();
        eventDTO.setCarIdentificationCode(reader.readString(17));
        eventDTO.setGunSort(reader.readByte());
        return eventDTO;
    }

}
