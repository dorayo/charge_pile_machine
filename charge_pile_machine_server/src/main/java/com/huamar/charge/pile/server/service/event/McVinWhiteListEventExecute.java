package com.huamar.charge.pile.server.service.event;

import com.huamar.charge.pile.dto.McEventReqDTO;
import com.huamar.charge.pile.dto.McVinWhiteListEventDTO;
import com.huamar.charge.pile.enums.McEventEnum;
import com.huamar.charge.pile.protocol.DataPacketReader;
import org.springframework.stereotype.Component;

/**
 * 设备端数据汇报接口-订单上传事件
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Component
public class McVinWhiteListEventExecute implements McEventExecute{


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McEventEnum getCode() {
        return McEventEnum.VIN_WHITE_LIST;
    }

    /**
     * 执行方法
     *
     * @param reqDTO reqDTO
     */
    @Override
    public void execute(McEventReqDTO reqDTO) {
        //TODO 业务实现
        McVinWhiteListEventDTO eventDTO = this.parse(reqDTO);
    }

    /**
     * 解析元数据
     *
     * @param reqDTO reqDTO
     * @return McEventBaseDTO
     */
    @Override
    public McVinWhiteListEventDTO parse(McEventReqDTO reqDTO) {
        DataPacketReader reader = new DataPacketReader(reqDTO.getEventData());
        McVinWhiteListEventDTO eventDTO = new McVinWhiteListEventDTO();
        eventDTO.setCarIdentificationCode(reader.readString(17));
        eventDTO.setGunSort(reader.readByte());
        return eventDTO;
    }


}
