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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息执行器-电价下发
 * DATE: 2023.08.07
 *
 * @author TiAmo(13721682347 @ 163.com)
 **/
@Slf4j
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
        Map<Integer, ChargPriceDTO> collect = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            collect.put(i, list.get(i));
        }
//        Map<Integer, ChargPriceDTO> collect = list.stream().collect(Collectors.toMap(ChargPriceDTO::getSortNum, item -> item, (k1, k2) -> k1));
        McElectricityPriceCommandDTO commandDTO = buildPrice(collect);
//        byte[] timePriceBucket = new byte[48];
        String[] timePriceBucket = new String[48];
//          byte zero = '0';
        Arrays.fill(timePriceBucket, "0");


        list.forEach(item -> {
            LocalTime startTime = LocalTime.parse(item.getStartTime());
            LocalTime endTime = LocalTime.parse(item.getEndTime());
            int startHour = startTime.getHour();
            int startMinute = startTime.getMinute();
            int endHour = endTime.getHour();
            int endMinute = endTime.getMinute() + 1;
            int start = Math.max(startHour * 2 + (startMinute / 30), 0);
            int end = Math.max(0, endHour * 2 + (endMinute / 30) - 1);
            BigDecimal unit = new BigDecimal("10000");
            if (start > end) {
                return;
            }
            for (; start <= end; start++) {
                if (start > 47) {
                    return;
                }
                short price1 = commandDTO.getPrice1();
                short service1 = commandDTO.getServicePrice1();
                short price2 = commandDTO.getPrice2();
                short service2 = commandDTO.getServicePrice2();
                short price3 = commandDTO.getPrice3();
                short service3 = commandDTO.getServicePrice3();
                short price4 = commandDTO.getPrice4();
                short service4 = commandDTO.getServicePrice4();
                short price5 = commandDTO.getPrice5();
                short service5 = commandDTO.getServicePrice5();
                short price6 = commandDTO.getPrice6();
                short service6 = commandDTO.getServicePrice6();

                short charge = item.getCharge().multiply(unit).shortValue();
                short serviceCharge = item.getServiceCharge().multiply(unit).shortValue();
                if (charge == price1 && serviceCharge == service1) {
                    timePriceBucket[start] = "0";
                    continue;
                }
                if (charge == price2 && serviceCharge == service2) {
                    timePriceBucket[start] = "1";
                    continue;
                }
                if (charge == price3 && serviceCharge == service3) {
                    timePriceBucket[start] = "2";
                    continue;
                }
                if (charge == price4 && serviceCharge == service4) {
                    timePriceBucket[start] = "3";
                    continue;
                }
                if (charge == price5 && serviceCharge == service5) {
                    timePriceBucket[start] = "4";
                    continue;
                }
                if (charge == price6 && serviceCharge == service6) {
                    timePriceBucket[start] = "5";
                    continue;
                }
            }


        });
        commandDTO.setGunSort((byte) 0);
        commandDTO.setIdCode(pileElectricityPriceDTO.getIdCode());
//        commandDTO.setTimeStage(new NumberFixStr(  timePriceBucket ));
        commandDTO.setTimeStage(new NumberFixStr(StringUtils.join(timePriceBucket).getBytes()));
        log.info("commandDTO{}", commandDTO);
        mcCommandFactory.getExecute(McCommandEnum.ELECTRICITY_PRICE).execute(commandDTO);
    }


    /**
     * 填充价格
     *
     * @param priceMap priceMap
     * @return McElectricityPriceCommandDTO
     */
    private McElectricityPriceCommandDTO buildPrice(Map<Integer, ChargPriceDTO> priceMap) {
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


    private ChargPriceDTO defaultPrice() {
        ChargPriceDTO defaultPrice = new ChargPriceDTO();
        defaultPrice.setSortNum(0);
        defaultPrice.setCharge(new BigDecimal("0.00001"));
        defaultPrice.setServiceCharge(new BigDecimal("0.00001"));
        return defaultPrice;
    }

    /**
     * 计算时间区间
     *
     * @param time time
     * @return int
     */
    private int getTimeBucket(String time) {
        LocalTime parse = LocalTime.parse(time);
        int hour = parse.getHour();
        int minute = parse.getMinute();
        return (hour * 2 + (minute >= 30 ? 1 : 0));
    }

}
