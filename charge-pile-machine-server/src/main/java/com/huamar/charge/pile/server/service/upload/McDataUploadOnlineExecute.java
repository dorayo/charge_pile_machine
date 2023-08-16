package com.huamar.charge.pile.server.service.upload;

import com.huamar.charge.pile.common.codec.BCD;
import com.huamar.charge.pile.entity.dto.MachineDataUpItem;
import com.huamar.charge.pile.entity.dto.McChargerOnlineInfoDTO;
import com.huamar.charge.pile.enums.McDataUploadEnum;
import com.huamar.charge.pile.protocol.DataPacketReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 充电桩实时状态信息 One
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Slf4j
@Component
public class McDataUploadOnlineExecute implements McDataUploadExecute {


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public McDataUploadEnum getCode() {
        return McDataUploadEnum.COMMON_0X08;
    }

    /**
     * @param time time
     * @param list list
     */
    @Override
    public void execute(BCD time, List<MachineDataUpItem> list) {
        // TODO 业务实现
        log.info("充电桩实时状态信息 start ==>");
    }

    /**
     * 解析对象
     * @param data data
     * @return McChargerOnlineInfoDto
     */
    private McChargerOnlineInfoDTO parse(MachineDataUpItem data){
        McChargerOnlineInfoDTO onlineInfoDto = new McChargerOnlineInfoDTO();
        DataPacketReader reader = new DataPacketReader(data.getData());
        if(reader.getBuffer().array().length == 24){
            this.parse(onlineInfoDto, reader);
            return onlineInfoDto;
        }
        if(reader.getBuffer().array().length == 32){
            this.parse(onlineInfoDto, reader);
            onlineInfoDto.setFaultCode1(reader.readInt());
            onlineInfoDto.setFaultCode2(reader.readInt());
            return onlineInfoDto;
        }
        if(onlineInfoDto.getGunNum() == 0){
            return onlineInfoDto;
        }

        // 新版本多个
        if (reader.getBuffer().array().length == (24 * onlineInfoDto.getGunNum()) + 2) {
            this.parse(onlineInfoDto, reader);
            return onlineInfoDto;
        }

        // //老版本多个
        if (reader.getBuffer().array().length == (32 * onlineInfoDto.getGunNum()) + 2) {
            this.parse(onlineInfoDto, reader);
            onlineInfoDto.setFaultCode1(reader.readInt());
            onlineInfoDto.setFaultCode2(reader.readInt());
            return onlineInfoDto;
        }
        return onlineInfoDto;
    }

    /**
     * 解析对象
     * @param onlineInfoDto onlineInfoDto
     * @param reader reader
     */
    private void parse(McChargerOnlineInfoDTO onlineInfoDto, DataPacketReader reader){
        onlineInfoDto.setGunSort(reader.readByte());
        onlineInfoDto.setGunState(reader.readByte());
        onlineInfoDto.setStartTime(reader.readBCD());
        onlineInfoDto.setCumulativeTime(reader.readInt());
        onlineInfoDto.setCurMoney(reader.readInt());
        onlineInfoDto.setServiceMoney(reader.readInt());
        onlineInfoDto.setCurChargeQuantity(reader.readInt());
    }
}
