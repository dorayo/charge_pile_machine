package com.huamar.charge.pile.server.service.receiver.execute;

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
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    static public byte[] empty = new byte[16];

    public void handleProtocolC(String idCode, McChargeCommandDTO chargeCommand) {
        SimpleSessionChannel session = (SimpleSessionChannel) SessionManager.get(idCode);
        byte type = 0x34;
        Integer orderV = session.channel().attr(NAttrKeys.PROTOCOL_C_LATEST_ORDER_V).get();
        orderV++;
        session.channel().attr(NAttrKeys.PROTOCOL_C_LATEST_ORDER_V).set(orderV);
        ConcurrentHashMap<Integer, Integer> orderMap = session.channel().attr(NAttrKeys.GUN_ORDER_MAP).get();
        if (orderMap == null) {
            orderMap = new ConcurrentHashMap<Integer, Integer>();
            session.channel().attr(NAttrKeys.GUN_ORDER_MAP).set(orderMap);
        }
        orderMap.put((int) chargeCommand.getGunSort(), orderV);
        ProtocolCPacket packet = session.channel().attr(NAttrKeys.PROTOCOL_C_LATEST_PACKET).get();
        ByteBuf responseBody = ByteBufAllocator.DEFAULT.heapBuffer(16 + 7 + 1 + 8 + 8 + 4);
        responseBody.writeBytes(BinaryViews.bcdStringToByte(new String(chargeCommand.getOrderSerialNumber()).substring(0, 32)));
        responseBody.writeBytes(packet.getIdBody());
        responseBody.writeByte(chargeCommand.getGunSort());
        responseBody.writeBytes(empty);
        responseBody.writeIntLE(chargeCommand.getBalance());
        ByteBuf response = BinaryBuilders.protocolCLeResponseBuilder(NUtils.nBFToBf(responseBody), orderV, type);
        session.channel().writeAndFlush(response).addListener((f) -> {
            if (f.isSuccess()) {
                log.info("0x34 success");
            } else {
                log.error("0x34  error");
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
            SimpleSessionChannel session = (SimpleSessionChannel) SessionManager.get(chargeControl.getIdCode());
//        command.getTypeCode()
            if (session.getType() == McTypeEnum.C) {
                handleProtocolC(chargeControl.getIdCode(), chargeCommand);
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
