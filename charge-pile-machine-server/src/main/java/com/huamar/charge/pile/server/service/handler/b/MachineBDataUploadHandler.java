package com.huamar.charge.pile.server.service.handler.b;

import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.convert.MachineDataUploadConvert;
import com.huamar.charge.pile.entity.dto.MachineDataUpItem;
import com.huamar.charge.pile.entity.dto.MachineDataUploadReqDTO;
import com.huamar.charge.pile.entity.dto.resp.McCommonResp;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.server.service.answer.b.McBCommonAnswerExecute;
import com.huamar.charge.pile.server.service.event.PileChargeFinishEventExecute;
import com.huamar.charge.pile.server.service.factory.McAnswerFactory;
import com.huamar.charge.pile.server.service.factory.McDataUploadFactory;
import com.huamar.charge.pile.server.service.handler.MachinePacketHandler;
import com.huamar.charge.pile.server.service.upload.McDataUploadOnlineExecute;
import com.huamar.charge.pile.server.service.upload.McDataUploadStageExecute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 终端数据汇报拦截器
 * 2023/06/11
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineBDataUploadHandler implements MachinePacketHandler<DataPacket> {


    private final McDataUploadFactory dataUploadFactory;

    /**
     * 设备终端上下文
     */
    private final McAnswerFactory answerFactory;
    /**
     * 回复通用应答
     */
    private final McBCommonAnswerExecute mcBCommonAnswerExecute;
    /**
     * 处理ox08
     */
    private final McDataUploadOnlineExecute mcDataUploadOnlineExecute;
    /**
     * 处理ox0A
     */
    private final McDataUploadStageExecute mcDataUploadStageExecute;
    /**
     * 处理国花0x02
     */
    private final PileChargeFinishEventExecute pileChargeFinishEventExecute;

    /**
     * 执行器
     *
     * @param packet         packet
     * @param sessionChannel sessionChannel
     */
    @Override
    public void handler(DataPacket packet, SessionChannel sessionChannel) {
        String channelIp = sessionChannel.getIp();
        MachineDataUploadReqDTO dataUploadReqDTO = MachineDataUploadConvert.INSTANCE.convert(packet);
        log.info("终端数据汇报，IdCode={},IP:{},msgNum:{}", new String(packet.getIdCode()), channelIp, packet.getMsgNumber());
        mcBCommonAnswerExecute.execute(McCommonResp.ok(packet), sessionChannel);

        List<MachineDataUpItem> unitList = dataUploadReqDTO.getDataUnitList();
        for (MachineDataUpItem item : unitList) {
            item.setIdCode(new String(packet.getIdCode()));
            switch (item.getUnitId()) {

                // 充电记录上传
                case 0x02:
                    pileChargeFinishEventExecute.executeGH(item, sessionChannel);
                    //implementation at platform
                    break;

                // 地面充电机数据
                case 0x07:
                    mcDataUploadOnlineExecute.chargerExecute(dataUploadReqDTO.getTime(), item);
                    //implementation at platform
                    break;

                // 充电机实时状态信息
                case 0x08:
                    mcDataUploadOnlineExecute.execute(dataUploadReqDTO.getTime(), item);
                    break;

                // 充电阶段信息
                case 0x0A:
                    mcDataUploadStageExecute.executeGH(dataUploadReqDTO.getTime(), item);
                    break;

                default:
                    log.error("MachineDataUpItem unKnown unitId " + item.getUnitId());
            }
        }

    }


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public ProtocolCodeEnum getCode() {
        return ProtocolCodeEnum.DATA_UPLOAD_B;
    }
}
