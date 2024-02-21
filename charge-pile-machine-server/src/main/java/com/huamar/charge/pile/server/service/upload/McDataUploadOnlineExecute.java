package com.huamar.charge.pile.server.service.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.huamar.charge.common.common.codec.BCD;
import com.huamar.charge.common.protocol.DataPacketReader;
import com.huamar.charge.net.core.SessionChannel;
import com.huamar.charge.pile.entity.dto.MachineDataUpItem;
import com.huamar.charge.pile.entity.dto.McChargerOnlineInfoDTO;
import com.huamar.charge.pile.entity.dto.command.McElectricityPriceCommandDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.PileHeartbeatDTO;
import com.huamar.charge.pile.enums.ConstEnum;
import com.huamar.charge.pile.enums.McDataUploadEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.pile.server.service.produce.PileMessageProduce;
import com.huamar.charge.pile.server.session.SessionManager;
import com.huamar.charge.pile.server.session.SimpleSessionChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/**
 * 充电桩实时状态信息 One
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McDataUploadOnlineExecute implements McDataUploadExecute {

    /**
     * 消息投递
     */
    private final PileMessageProduce pileMessageProduce;

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
        list.forEach(item -> this.execute(time, item));
    }

    /**
     * Execute.
     *
     * @param time time
     * @param item the item
     */
    public void execute(BCD time, MachineDataUpItem item) {
        if(log.isTraceEnabled()){
            log.trace("充电桩实时状态信息 time:{} ", time);
        }

        McChargerOnlineInfoDTO parse = this.parse(item);
        log.info("充电桩实时状态信息 data:{}", parse);
        this.sendMessage(parse);
    }

    /**
     * 发送设备端消息
     *
     * @param onlineInfoDTO onlineInfoDTO
     */
    private void sendMessage(McChargerOnlineInfoDTO onlineInfoDTO) {
        try {
            Assert.notNull(onlineInfoDTO, "McChargerOnlineInfoDTO noNull");
            MessageData<McChargerOnlineInfoDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_ONLINE, onlineInfoDTO);
            messageData.setBusinessId(onlineInfoDTO.getIdCode());
            messageData.setMessageId(IdUtil.simpleUUID());
            messageData.setRequestId(IdUtil.simpleUUID());
            pileMessageProduce.send(messageData);
        } catch (Exception e) {
            log.error("sendMessage send error e:{}", e.getMessage(), e);
        }
    }

    /**
     * 解析对象
     *
     * @param data data
     * @return McChargerOnlineInfoDto
     */
    @SuppressWarnings("DuplicatedCode")
    private McChargerOnlineInfoDTO parse(MachineDataUpItem data) {
        McChargerOnlineInfoDTO onlineInfoDto = new McChargerOnlineInfoDTO();
        onlineInfoDto.setIdCode(data.getIdCode());
        DataPacketReader reader = new DataPacketReader(data.getData());

        // SLX 老协议
        if (reader.getBuffer().array().length == 24) {
            this.parse(onlineInfoDto, reader);
            return onlineInfoDto;
        }

        // SLX 新协议
        if (reader.getBuffer().array().length == 32) {
            this.parse(onlineInfoDto, reader);
            onlineInfoDto.setFaultCode1(reader.readInt());
            onlineInfoDto.setFaultCode2(reader.readInt());
            return onlineInfoDto;
        }

        // 国花协议
        if (reader.getBuffer().array().length == 56) {
            //adopte machine b
            this.parseGH(onlineInfoDto, reader);
            return onlineInfoDto;
        }


        if (onlineInfoDto.getGunNum() == 0) {
            return onlineInfoDto;
        }

        // 新版本多个
        if (reader.getBuffer().array().length == (24 * onlineInfoDto.getGunNum()) + 2) {
            this.parse(onlineInfoDto, reader);
            return onlineInfoDto;
        }

        //老版本多个
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
     *
     * @param onlineInfoDto onlineInfoDto
     * @param reader        reader
     */
    private void parse(McChargerOnlineInfoDTO onlineInfoDto, DataPacketReader reader) {
        //noinspection DuplicatedCode
        onlineInfoDto.setGunSort(reader.readByte());
        onlineInfoDto.setGunState(reader.readByte());
        onlineInfoDto.setStartTime(reader.readBCD());
        onlineInfoDto.setCumulativeTime(reader.readInt());
        onlineInfoDto.setCurMoney(reader.readInt());
        onlineInfoDto.setServiceMoney(reader.readInt());
        onlineInfoDto.setCurChargeQuantity(reader.readInt());
    }

    /**
     * 解析对象
     *
     * @param onlineInfoDto onlineInfoDto
     * @param reader        reader
     */
    private void parseGH(McChargerOnlineInfoDTO onlineInfoDto, DataPacketReader reader) {
        // 国花协议
        onlineInfoDto.setGunSort(reader.readByte());
        onlineInfoDto.setGunState(reader.readByte());
        onlineInfoDto.setStartTime(reader.readBCD());
        onlineInfoDto.setCumulativeTime(reader.readInt());
        onlineInfoDto.setCurMoney(reader.readInt() * 100);
        onlineInfoDto.setServiceMoney(reader.readInt() * 100);
        onlineInfoDto.setCurChargeQuantity(reader.readInt() * 100);

        Assert.notNull(onlineInfoDto.getIdCode(), "parseGH idCode is null");
        SessionChannel sessionChannel = SessionManager.get(onlineInfoDto.getIdCode());
        Assert.notNull(onlineInfoDto.getIdCode(), "parseGH sessionChannel is null + idCode:" + onlineInfoDto.getIdCode());

        if(sessionChannel instanceof SimpleSessionChannel){
            //noinspection DuplicatedCode
            SimpleSessionChannel simpleSessionChannel = (SimpleSessionChannel) sessionChannel;
            ChannelHandlerContext context = simpleSessionChannel.channel();
            Channel channel = context.channel();

            AttributeKey<Integer> elePriceType = AttributeKey.valueOf(ConstEnum.ELE_CHARG_TYPE.getCode());
            Integer priceType = channel.attr(elePriceType).get();


            AttributeKey<McElectricityPriceCommandDTO> priceAttr = AttributeKey.valueOf(ConstEnum.COMMON_CHARGE_PRICE.getCode());
            McElectricityPriceCommandDTO priceCommandDTO = channel.attr(priceAttr).get();

            Assert.notNull(priceType, "parseGH priceType is null");
            Assert.notNull(priceCommandDTO, "parseGH priceCommandDTO is null");

            // v2014/02/20 当前版本国花协议服务费固定费率, so 电价=总价-服务费*度数，暂时简化处理
            if(Objects.equals(priceType, 1)){
                BigDecimal chargePower = BigDecimal.valueOf(onlineInfoDto.getCurChargeQuantity());
                chargePower = chargePower.divide(BigDecimal.valueOf(1000), RoundingMode.HALF_UP);

                BigDecimal money = BigDecimal.valueOf(onlineInfoDto.getCurMoney());
                BigDecimal serviceMoney = chargePower.multiply(BigDecimal.valueOf(priceCommandDTO.getSlxServicePrice()[0]));
                BigDecimal chargePrice = money.subtract(serviceMoney);

                onlineInfoDto.setCurMoney(chargePrice.intValue());
                onlineInfoDto.setServiceMoney(serviceMoney.intValue());
                return;
            }

            //v2014/02/20 当前版本国花交流协议服务费是硕力新协议格式，信任设备上报的服务费
            if(Objects.equals(priceType, 2)){
                return;
            }

            throw new RuntimeException("parseGH priceType is invalid");
        }

    }

    /**
     * 兼容国华协议
     *
     * @param time time
     * @param item item
     */
    public void chargerExecute(BCD time, MachineDataUpItem item) {
        if(log.isTraceEnabled()){
            log.trace("地面充电机数据汇报 time:{} item:{}", time, item);
        }

        if (item.getData().length > 2 && (item.getData().length - 2) % 56 == 0) {
            byte[] data = Arrays.copyOfRange(item.getData(), 2, item.getData().length);
            int num = (item.getData().length - 2) / 56;
            for (int i = 0; i < num; i++) {
                byte[] newScores = Arrays.copyOfRange(data, i * 56, (i + 1) * 56);
                item.setData(newScores);
                McChargerOnlineInfoDTO parse = this.parse(item);
                this.sendMessage(parse);
                this.sendHeart(parse);
            }
        }
    }

    private void sendHeart(McChargerOnlineInfoDTO reqDTO) {
        PileHeartbeatDTO pileHeartbeatDTO = new PileHeartbeatDTO();
        pileHeartbeatDTO.setProtocolNumber(null);
        pileHeartbeatDTO.setIdCode(reqDTO.getIdCode());
        pileHeartbeatDTO.setDateTime(LocalDateTime.now());
        pileHeartbeatDTO.setTime(DateUtil.now());
        MessageData<PileHeartbeatDTO> messageData = new MessageData<>(MessageCodeEnum.PILE_HEART_BEAT, pileHeartbeatDTO);
        pileMessageProduce.send(messageData);
    }
}
