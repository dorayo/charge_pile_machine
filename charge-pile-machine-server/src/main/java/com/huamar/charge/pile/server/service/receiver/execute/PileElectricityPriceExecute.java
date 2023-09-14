package com.huamar.charge.pile.server.service.receiver.execute;

import com.alibaba.fastjson.JSON;
import com.huamar.charge.pile.entity.dto.command.McElectricityPriceCommandDTO;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.ChargPriceDTO;
import com.huamar.charge.pile.entity.dto.platform.PileElectricityPriceDTO;
import com.huamar.charge.pile.enums.McCommandEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.common.protocol.NumberFixStr;
import com.huamar.charge.pile.server.service.factory.McCommandFactory;
import com.huamar.charge.pile.server.service.receiver.PileMessageExecute;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息执行器-电价下发
 *  DATE: 2023.08.07
 * @author TiAmo(13721682347@163.com)
 **/
@Component
@RequiredArgsConstructor
public class PileElectricityPriceExecute implements PileMessageExecute {

    private final McCommandFactory mcCommandFactory;

    /**
     * 协议编码
     *
     * @return ProtocolCodeEnum
     */
    @Override
    public MessageCodeEnum getCode() {
        return MessageCodeEnum.ELECTRICITY_PRICE;
    }

    /**
     * 电价下发
     *
     * @param body body
     */
    @Override
    public void execute(MessageData<String> body) {
        PileElectricityPriceDTO pileElectricityPriceDTO = JSON.parseObject(body.getData(), PileElectricityPriceDTO.class);
        List<ChargPriceDTO> list = pileElectricityPriceDTO.getList();
        Map<Integer, ChargPriceDTO> collect = list.stream().collect(Collectors.toMap(ChargPriceDTO::getSortNum, item -> item, (k1, k2) -> k1));
        McElectricityPriceCommandDTO commandDTO = buildPrice(collect);
        String[] timePriceBucket = new String[48];
        Arrays.fill(timePriceBucket, "0");
        list.forEach(item -> {
            int start = getTimeBucket(item.getStartTime());
            int end = getTimeBucket(item.getEndTime());
            for (int i = start; i < end; i++) {
                timePriceBucket[i] = String.valueOf(item.getSortNum());
            }
        });
        commandDTO.setGunSort((byte) 0);
        commandDTO.setIdCode(body.getBusinessId());
        commandDTO.setTimeStage(new NumberFixStr(StringUtils.join(timePriceBucket).getBytes()));
        mcCommandFactory.getExecute(McCommandEnum.ELECTRICITY_PRICE).execute(commandDTO);
    }

    /**
     * 填充价格
     * @param priceMap priceMap
     * @return McElectricityPriceCommandDTO
     */
    private McElectricityPriceCommandDTO buildPrice(Map<Integer, ChargPriceDTO> priceMap){
        ChargPriceDTO defaultPrice = this.defaultPrice();
        BigDecimal unit = new BigDecimal("10000");

        McElectricityPriceCommandDTO priceCommandDTO = new McElectricityPriceCommandDTO();
        ChargPriceDTO price = priceMap.getOrDefault(0, defaultPrice);
        priceCommandDTO.setPrice1(price.getCharge().multiply(unit).shortValue());
        priceCommandDTO.setServicePrice1(price.getServiceCharge().multiply(unit).shortValue());

        price = priceMap.getOrDefault(1, defaultPrice);
        priceCommandDTO.setPrice2(price.getCharge().multiply(unit).shortValue());
        priceCommandDTO.setServicePrice2(price.getServiceCharge().multiply(unit).shortValue());

        price = priceMap.getOrDefault(2, defaultPrice);
        priceCommandDTO.setPrice3(price.getCharge().multiply(unit).shortValue());
        priceCommandDTO.setServicePrice3(price.getServiceCharge().multiply(unit).shortValue());

        price = priceMap.getOrDefault(3, defaultPrice);
        priceCommandDTO.setPrice4(price.getCharge().multiply(unit).shortValue());
        priceCommandDTO.setServicePrice4(price.getServiceCharge().multiply(unit).shortValue());

        price = priceMap.getOrDefault(4, defaultPrice);
        priceCommandDTO.setPrice5(price.getCharge().multiply(unit).shortValue());
        priceCommandDTO.setServicePrice5(price.getServiceCharge().multiply(unit).shortValue());

        price = priceMap.getOrDefault(5, defaultPrice);
        priceCommandDTO.setPrice6(price.getCharge().multiply(unit).shortValue());
        priceCommandDTO.setServicePrice6(price.getServiceCharge().multiply(unit).shortValue());

        return priceCommandDTO;
    }


    private ChargPriceDTO defaultPrice(){
        ChargPriceDTO defaultPrice = new ChargPriceDTO();
        defaultPrice.setSortNum(0);
        defaultPrice.setCharge(new BigDecimal("0.00001"));
        defaultPrice.setServiceCharge(new BigDecimal("0.00001"));
        return defaultPrice;
    }

    /**
     * 计算时间区间
     * @param time time
     * @return int
     */
    private int getTimeBucket(String time){
        LocalTime parse = LocalTime.parse(time);
        int hour = parse.getHour();
        int minute = parse.getMinute();
        return (hour * 2 + (minute >= 30 ? 1 : 0));
    }

}
