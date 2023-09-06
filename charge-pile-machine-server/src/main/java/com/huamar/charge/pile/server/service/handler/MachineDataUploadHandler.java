package com.huamar.charge.pile.server.service.handler;

import com.huamar.charge.pile.convert.MachineDataUploadConvert;
import com.huamar.charge.pile.entity.dto.MachineDataUpItem;
import com.huamar.charge.pile.entity.dto.MachineDataUploadReqDTO;
import com.huamar.charge.pile.entity.dto.resp.McCommonResp;
import com.huamar.charge.pile.enums.McAnswerEnum;
import com.huamar.charge.pile.enums.McDataUploadEnum;
import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.pile.server.service.McAnswerFactory;
import com.huamar.charge.pile.server.service.McDataUploadFactory;
import com.huamar.charge.pile.server.service.upload.McDataUploadExecute;
import com.huamar.charge.common.util.HexExtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.tio.core.ChannelContext;

import java.util.*;

/**
 * 终端数据汇报拦截器
 * 2023/06/11
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MachineDataUploadHandler implements MachineMessageHandler<DataPacket> {


    private final McDataUploadFactory dataUploadFactory;

    /**
     * 设备终端上下文
     */
    private final McAnswerFactory answerFactory;

    /**
     * 执行器
     *
     * @param packet         packet
     * @param channelContext channelContext
     */
    @Override
    public void handler(DataPacket packet, ChannelContext channelContext) {
        MachineDataUploadReqDTO dataUploadReqDTO = MachineDataUploadConvert.INSTANCE.convert(packet);
        log.info("终端数据汇报，ip={},idCode:{},msgNum:{}", new String(packet.getIdCode()), channelContext.getClientNode().getIp(), packet.getMsgNumber());
        answerFactory.getExecute(McAnswerEnum.COMMON).execute(McCommonResp.ok(packet), channelContext);


        List<MachineDataUpItem> unitList = dataUploadReqDTO.getDataUnitList();
        Map<String, List<MachineDataUpItem>> map = new HashMap<>();
        if (CollectionUtils.isEmpty(unitList)) {
            return;
        }

        for (MachineDataUpItem item : unitList) {
            item.setIdCode(new String(packet.getIdCode()));
            String unitCode = HexExtUtil.encodeHexStr(item.getUnitId());
            unitCode = StringUtils.upperCase(unitCode);
            if (!map.containsKey(unitCode)) {
                map.put(unitCode, new ArrayList<>());
            }
            map.get(unitCode).add(item);
        }

        map.forEach((key, value) -> {
            McDataUploadEnum uploadEnum = McDataUploadEnum.getByCode(key);
            if (Objects.isNull(uploadEnum)) {
                log.error("McDataUploadEnum uploadEnum get null...key:{}", key);
                return;
            }
            McDataUploadExecute execute = dataUploadFactory.getExecute(uploadEnum);
            if (Objects.isNull(execute)) {
                log.error("McDataUploadExecute execute get null...uploadEnum:{}", uploadEnum);
                return;
            }
            execute.execute(dataUploadReqDTO.getTime(), value);
        });
    }


    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public ProtocolCodeEnum getCode() {
        return ProtocolCodeEnum.DATA_UPLOAD;
    }
}
