package com.huamar.charge.pile.server.service.receiver.execute;

import cn.hutool.core.util.ByteUtil;
import com.huamar.charge.common.util.ByteExtUtil;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.common.util.netty.NUtils;
import com.huamar.charge.pile.entity.dto.command.McChargeCommandDTO;
import com.huamar.charge.pile.entity.dto.command.MessageCommonRespDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.PileChargeControlDTO;
import com.huamar.charge.pile.enums.*;
import com.huamar.charge.pile.server.service.command.MessageCommandRespService;
import com.huamar.charge.pile.server.service.factory.McCommandFactory;
import com.huamar.charge.pile.server.service.receiver.PileMessageExecute;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import com.huamar.charge.pile.utils.binaryBuilder.BinaryBuilders;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 充电控制
 * DATE: 2023.08.07
 *
 * @author TiAmo(13721682347 @ 163.com)
 **/
@Slf4j
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
        ChannelHandlerContext ctx = session.channel();

        final byte type = 0x34;
//        Integer orderV = ctx.channel().attr(NAttrKeys.PROTOCOL_C_LATEST_ORDER_V).get();
//        orderV++;
//        ctx.channel().attr(NAttrKeys.PROTOCOL_C_LATEST_ORDER_V).set(orderV);

        Short number = NAttrKeys.getSerialNumber(session);
        byte[] serialNumber = ByteExtUtil.shortToBytes(number, ByteUtil.DEFAULT_ORDER);


        ConcurrentHashMap<Integer, Integer> orderMap = ctx.channel().attr(NAttrKeys.GUN_ORDER_MAP).get();
        byte[] idBody = ctx.channel().attr(NAttrKeys.ID_BODY).get();
        if (orderMap == null) {
            orderMap = new ConcurrentHashMap<Integer, Integer>();
            ctx.channel().attr(NAttrKeys.GUN_ORDER_MAP).set(orderMap);
        }
        orderMap.put((int) chargeCommand.getGunSort(), number.intValue());


        ByteBuf responseBody = ByteBufAllocator.DEFAULT.heapBuffer(16 + 7 + 1 + 8 + 8 + 4);
        byte[] serialN = BinaryViews.numberStrToBcd(chargeCommand.getOrderSerialNumber());
        responseBody.writeBytes(serialN);
        responseBody.writeBytes(idBody);
        responseBody.writeByte(chargeCommand.getGunSort());
        responseBody.writeBytes(empty);
        responseBody.writeIntLE(chargeCommand.getBalance());
//        responseBody.writeIntLE(3000);
        ByteBuf response = BinaryBuilders.protocolCLeResponseBuilder(NUtils.nBFToBf(responseBody), serialNumber, type);
        String str = BinaryViews.bfToHexStr(response);
        ctx.writeAndFlush(response).addListener((f) -> {
            if (f.isSuccess()) {
                log.info("YKC 开机充电 0x34 hex packet:{} success", str);
            } else {
                log.error("YKC 开机充电 0x34 error:{}", ExceptionUtils.getMessage(f.cause()), f.cause());
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
