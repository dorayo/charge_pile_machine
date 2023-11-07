package com.huamar.charge.pile.server.service.receiver.execute;

import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.pile.entity.dto.command.McChargeCommandDTO;
import com.huamar.charge.pile.entity.dto.command.MessageCommonRespDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.PileChargeControlDTO;
import com.huamar.charge.pile.enums.McCommandEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.enums.MessageCommonResultEnum;
import com.huamar.charge.pile.server.service.factory.McCommandFactory;
import com.huamar.charge.pile.server.service.command.MessageCommandRespService;
import com.huamar.charge.pile.server.service.receiver.PileMessageExecute;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

/**
 * 充电控制
 * DATE: 2023.08.07
 *
 * @author TiAmo(13721682347 @ 163.com)
 **/
@Log4j
@Component
@RequiredArgsConstructor
public class PileStartChargeExecute implements PileMessageExecute {


    private final McCommandFactory mcCommandFactory;

    /**
     * 消息应答处理
     */
    private final MessageCommandRespService messageCommandRespService;

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public MessageCodeEnum getCode() {
        return MessageCodeEnum.PILE_START_CHARGE;
    }

    /**
     * 充电控制启动
     *
     * @param body body
     */
    @SuppressWarnings({"ExtractMethodRecommender", "DuplicatedCode"})
    @Override
    public void execute(MessageData<String> body) {
        PileChargeControlDTO chargeControl = JSONParser.parseObject(body.getData(), PileChargeControlDTO.class);
        log.info("开启充电参数下发：" + chargeControl);
        try {

            McChargeCommandDTO chargeCommand = new McChargeCommandDTO();
            chargeCommand.setChargeControl((byte) 1);
            chargeCommand.setChargeEndType(chargeControl.getChargeEndType().byteValue());
            chargeCommand.setChargeEndValue(chargeControl.getChargeEndValue().intValue());
            chargeCommand.setGunSort(chargeControl.getGunSort().byteValue());
            chargeCommand.setOrderSerialNumber(chargeControl.getOrderSerialNumber().getBytes());
            chargeCommand.setBalance(chargeControl.getBalance().intValue());
            chargeCommand.setIdCode(chargeControl.getIdCode());
            log.info("开启充电参数下发：" + chargeCommand);
            mcCommandFactory.getExecute(McCommandEnum.CHARGE).execute(chargeCommand);
            Boolean commandState = chargeCommand.headCommandState();

            MessageCommonRespDTO commonResp = new MessageCommonRespDTO();
            commonResp.setIdCode(chargeControl.getIdCode());
            commonResp.setCommandId(chargeControl.getCommandId());
            commonResp.setMsgResult(MessageCommonResultEnum.FAIL.getCode());
            commonResp.setMsgNumber(chargeCommand.headMessageNum().intValue());
            commonResp.setCommandTypeCode(this.getCode().getCode());
            messageCommandRespService.put(commonResp);

            if (!commandState) {

                messageCommandRespService.sendCommonResp(commonResp);
            }

        } catch (Exception e) {
            MessageCommonRespDTO commonResp = new MessageCommonRespDTO();
            commonResp.setIdCode(chargeControl.getIdCode());
            commonResp.setCommandId(chargeControl.getCommandId());
            commonResp.setMsgResult(MessageCommonResultEnum.FAIL.getCode());
            messageCommandRespService.sendCommonResp(commonResp);
        }
    }
}
