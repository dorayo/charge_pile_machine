package com.huamar.charge.pile.server.service.receiver.execute;

import com.alibaba.druid.sql.visitor.functions.Bin;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.common.util.netty.NUtils;
import com.huamar.charge.pile.entity.dto.command.McChargeCommandDTO;
import com.huamar.charge.pile.entity.dto.command.MessageCommonRespDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.PileChargeControlDTO;
import com.huamar.charge.pile.enums.*;
import com.huamar.charge.pile.server.service.factory.McCommandFactory;
import com.huamar.charge.pile.server.service.command.MessageCommandRespService;
import com.huamar.charge.pile.server.service.receiver.PileMessageExecute;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import com.huamar.charge.pile.utils.binaryBuilder.BinaryBuilders;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 充电控制
 * DATE: 2023.08.07
 *
 * @author TiAmo(13721682347 @ 163.com)
 **/
@Component
@RequiredArgsConstructor
@Slf4j
public class PileStopChargeExecute implements PileMessageExecute {


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
        return MessageCodeEnum.PILE_STOP_CHARGE;
    }

    public void handleC(String idCode, McChargeCommandDTO chargeCommand) {
        byte type = 0x36;
        SimpleSessionChannel session = (SimpleSessionChannel) SessionManager.get(idCode);
        Integer latestOrderV = session.channel().attr(NAttrKeys.PROTOCOL_C_LATEST_ORDER_V).get();
        latestOrderV++;
        session.channel().attr(NAttrKeys.PROTOCOL_C_LATEST_ORDER_V).set(latestOrderV);
        byte[] idBody = session.channel().attr(NAttrKeys.ID_BODY).get();
        ByteBuf responseBody = ByteBufAllocator.DEFAULT.heapBuffer(7 + 1);
        responseBody.writeBytes(idBody);
        responseBody.writeByte(chargeCommand.getGunSort());
        ByteBuf response = BinaryBuilders.protocolCLeResponseBuilder(NUtils.nBFToBf(responseBody), latestOrderV, type);
        log.info("send 停机0x36 body={}", BinaryViews.bfToHexStr(response));
        session.channel().writeAndFlush(response).addListener((f) -> {
            if (f.isSuccess()) {
                log.info("{} success", type);
            } else {
                log.error("{}success error", type);
                f.cause().printStackTrace();
            }
        });
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
        try {
            McChargeCommandDTO chargeCommand = new McChargeCommandDTO();
            chargeCommand.setChargeControl((byte) 0);
            chargeCommand.setChargeEndType(chargeControl.getChargeEndType().byteValue());
            chargeCommand.setChargeEndValue(chargeControl.getChargeEndValue().byteValue());
            chargeCommand.setGunSort(chargeControl.getGunSort().byteValue());
            chargeCommand.setOrderSerialNumber(chargeControl.getOrderSerialNumber().getBytes());
            chargeCommand.setBalance(chargeControl.getBalance().intValue());
            chargeCommand.setIdCode(chargeControl.getIdCode());
            SimpleSessionChannel session = (SimpleSessionChannel) SessionManager.get(chargeCommand.getIdCode());

            if (session.getType() == McTypeEnum.C) {
                handleC(chargeCommand.getIdCode(), chargeCommand);
                return;
            }

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
