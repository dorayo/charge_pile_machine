package com.huamar.charge.pile.server.service.receiver.execute;

import cn.hutool.core.util.ByteUtil;
import com.huamar.charge.common.protocol.DataPacket;
import com.huamar.charge.common.util.ByteExtUtil;
import com.huamar.charge.common.util.JSONParser;
import com.huamar.charge.common.util.netty.NUtils;
import com.huamar.charge.pile.entity.dto.command.McCardQueryCommandDTO;
import com.huamar.charge.pile.entity.dto.command.McChargeCommandDTO;
import com.huamar.charge.pile.entity.dto.command.MessageCommonRespDTO;
import com.huamar.charge.pile.entity.dto.event.PileSwipeCardEventDTO;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 刷卡查询参数下发
 * DATE: 2023.08.07
 *
 * @author TiAmo(13721682347 @ 163.com)
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class PileSwipeCardExecute implements PileMessageExecute {


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
        return MessageCodeEnum.SWIPE_CARD_QUERY;
    }

    static public byte[] empty = new byte[16];

    /**
     * 充电控制启动
     *
     * @param body body
     */
    @SuppressWarnings({"ExtractMethodRecommender", "DuplicatedCode"})
    @Override
    public void execute(MessageData<String> body) {
        McCardQueryCommandDTO chargeCommand = JSONParser.parseObject(body.getData(), McCardQueryCommandDTO.class);
        log.info("开启刷卡查询结果参数下发：" + chargeCommand);
        try {
            SimpleSessionChannel session = (SimpleSessionChannel) SessionManager.get(chargeCommand.getIdCode());
            Assert.notNull(session, "开启刷卡查询结果参数下发 error 设备不在线");

            mcCommandFactory.getExecute(McCommandEnum.CARD_QUERY).execute(chargeCommand);
            Boolean commandState = chargeCommand.headCommandState();

            MessageCommonRespDTO commonResp = new MessageCommonRespDTO();
            commonResp.setIdCode(chargeCommand.getIdCode());
            commonResp.setCommandId(chargeCommand.getCommandId());
            commonResp.setMsgResult(MessageCommonResultEnum.SUCCESS.getCode());
            commonResp.setMsgNumber(chargeCommand.headMessageNum().intValue());
            commonResp.setCommandTypeCode(this.getCode().getCode());
            messageCommandRespService.put(commonResp);

            if (!commandState) {
                messageCommandRespService.sendCommonResp(commonResp);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("开启刷卡查询结果参数下发 error:{}",e.getMessage());
            MessageCommonRespDTO commonResp = new MessageCommonRespDTO();
            commonResp.setIdCode(chargeCommand.getIdCode());
            commonResp.setCommandId(chargeCommand.getCommandId());
            commonResp.setMsgResult(MessageCommonResultEnum.FAIL.getCode());
            messageCommandRespService.sendCommonResp(commonResp);
        }
    }
}
