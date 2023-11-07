package com.huamar.charge.pile.server.service.handler.c;

import com.alibaba.druid.sql.visitor.functions.Bin;
import com.huamar.charge.common.protocol.c.ProtocolCPacket;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.api.dto.PileDTO;
import com.huamar.charge.pile.entity.dto.fault.McHeartbeatReqDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.PileHeartbeatDTO;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.enums.NAttrKeys;
import com.huamar.charge.pile.server.service.factory.McAnswerFactory;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import com.huamar.charge.pile.utils.binaryBuilder.BinaryBuilders;
import com.huamar.charge.pile.utils.views.BinaryViews;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class MachineCHandlers {
    /**
     * 设备终端上下文
     */
    private final McAnswerFactory answerFactory;

    /**
     * 消息投递
     */
    private final PileMessageProduce pileMessageProduce;

    public void handler0x05(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        McHeartbeatReqDTO reqDTO = null;
        try {
            int requestBodyLen = packet.getBody().length;
            byte requestBodyType = packet.getBodyType();
            byte responseBodyType = (byte) (requestBodyType + 1);
            byte[] responseBody = Arrays.copyOf(packet.getBody(), requestBodyLen + 1);
            responseBody[requestBodyLen] = 0;
            ByteBuf response = BinaryBuilders.protocolCLeResponseBuilder(responseBody, packet.getOrderVBf(), responseBodyType);
            ctx.channel().writeAndFlush(response).addListener((f) -> {
                if (f.isSuccess()) {
                    log.info("write {}  success", responseBodyType);
                } else {
                    log.error("write {}  error", responseBodyType);
                    f.cause().printStackTrace();
                }
            });
        } catch (Exception e) {
        }
    }

    public void handler0x09(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        McHeartbeatReqDTO reqDTO = null;
        try {
            PileDTO update = new PileDTO();
            ctx.channel().attr(NAttrKeys.PROTOCOL_C_0x09_PACKET).set(packet);
            update.setPileCode(BinaryViews.bcdViewsLe(packet.getIdBody()));
            pileMessageProduce.send(new MessageData<>(MessageCodeEnum.ELECTRICITY_PRICE, update));
        } catch (Exception e) {
        }
    }

    public void handler0x33(ProtocolCPacket packet, ChannelHandlerContext ctx) {
        McHeartbeatReqDTO reqDTO = null;
        try {
            PileDTO update = new PileDTO();
            ctx.channel().attr(NAttrKeys.PROTOCOL_C_0x09_PACKET).set(packet);
            update.setPileCode(BinaryViews.bcdViewsLe(packet.getIdBody()));
            pileMessageProduce.send(new MessageData<>(MessageCodeEnum.ELECTRICITY_PRICE, update));
        } catch (Exception e) {
        }
    }
}
