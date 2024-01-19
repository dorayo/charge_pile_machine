package com.huamar.charge.pile.server.service.receiver.execute;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huamar.charge.common.common.StringPool;
import com.huamar.charge.common.util.HexExtUtil;
import com.huamar.charge.pile.entity.dto.command.McElectricityPriceCommandDTO;
import com.huamar.charge.pile.entity.dto.command.YKCChargePrice;
import com.huamar.charge.pile.entity.dto.mq.MessageData;
import com.huamar.charge.pile.entity.dto.platform.ChargPriceDTO;
import com.huamar.charge.pile.entity.dto.platform.PileElectricityPriceDTO;
import com.huamar.charge.pile.enums.McCommandEnum;
import com.huamar.charge.pile.enums.MessageCodeEnum;
import com.huamar.charge.common.protocol.NumberFixStr;
import com.huamar.charge.pile.server.service.charge.ChargeInfoService;
import com.huamar.charge.pile.server.service.factory.McCommandFactory;
import com.huamar.charge.pile.server.service.receiver.PileMessageExecute;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

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

    private final ChargeInfoService chargeInfoService;

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
        if(list.isEmpty()){
            log.warn("event:{} price list is none", MessageCodeEnum.ELECTRICITY_PRICE);
            return;
        }


        Map<Integer, ChargPriceDTO> collect = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            collect.put(i, list.get(i));
        }

        McElectricityPriceCommandDTO commandDTO = buildPrice(collect);

        // 峰谷电价
        try {
            YKCChargePrice ykcChargePrice = this.buildJFPGPrice(commandDTO, pileElectricityPriceDTO);
            ChargPriceDTO next = list.iterator().next();
            chargeInfoService.putPriceInfoForCache(next.getStationId(), next.getChargType(), ykcChargePrice);
        } catch (Exception e) {
            log.error("YKC ykcChargePrice build error:{}", ExceptionUtils.getMessage(e), e);
        }

        try {
            this.buildSLXChargePrice(commandDTO, pileElectricityPriceDTO);
        }catch (Exception e){
            log.error("SLX ChargePrice build error:{}", ExceptionUtils.getMessage(e), e);
        }



        String[] timePriceBucket = new String[48];
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
                }
            }
        });

        commandDTO.setGunSort((byte) 0);
        commandDTO.setIdCode(pileElectricityPriceDTO.getIdCode());
        commandDTO.setTimeStage(new NumberFixStr(StringUtils.join(timePriceBucket).getBytes()));

        if(log.isDebugEnabled()){
            log.debug("commandDTO: {}", JSON.toJSONString(commandDTO));
        }

        mcCommandFactory.getExecute(McCommandEnum.ELECTRICITY_PRICE).execute(commandDTO);
    }


    /**
     * SLX 协议电价
     *
     * @param commandJFPGDTO commandJFPGDTO
     * @param pileElectricityPriceDTO pileElectricityPriceDTO
     */
    private void buildSLXChargePrice(McElectricityPriceCommandDTO commandJFPGDTO, PileElectricityPriceDTO pileElectricityPriceDTO){
        Assert.notNull(pileElectricityPriceDTO, "电价下发失败");
        Assert.isTrue(CollectionUtils.isNotEmpty(pileElectricityPriceDTO.getList()), "电价下发失败");
        BigDecimal unit = new BigDecimal("10000");

        List<ChargPriceDTO> objects = pileElectricityPriceDTO.getList();

        // 转换集合
        List<BigDecimal> priceList = objects.stream()
                .map(ChargPriceDTO::getCharge)
                .collect(Collectors.toList());

        // 去重 计算 间峰平谷 电价 index 0 1 2 3
        List<BigDecimal> distinctPrice = priceList.stream()
                .distinct().sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        // 解决重复键的冲突策略
        Map<BigDecimal, ChargPriceDTO> personMap = objects.stream()
                .collect(Collectors.toMap(ChargPriceDTO::getCharge, var -> var, (existing, incoming) -> existing));

        // 间峰平谷电下标 0 1 2 3
        Map<BigDecimal, Byte> indexMap = new HashMap<>();

        if(!distinctPrice.isEmpty()){
            ChargPriceDTO chargPriceDTO = personMap.get(distinctPrice.get(0));
            BigDecimal charge = chargPriceDTO.getCharge();
            BigDecimal serviceCharge = chargPriceDTO.getServiceCharge();
            commandJFPGDTO.getSlxChargePrice()[0] = charge.multiply(unit).intValue();
            commandJFPGDTO.getSlxServicePrice()[0] = serviceCharge.multiply(unit).intValue();
            indexMap.put(charge, (byte) 0);
        }

        if(distinctPrice.size() >= 2){
            ChargPriceDTO chargPriceDTO = personMap.get(distinctPrice.get(1));
            BigDecimal charge = chargPriceDTO.getCharge();
            BigDecimal serviceCharge = chargPriceDTO.getServiceCharge();
            commandJFPGDTO.getSlxChargePrice()[1] = charge.multiply(unit).intValue();
            commandJFPGDTO.getSlxServicePrice()[1] = serviceCharge.multiply(unit).intValue();
            indexMap.put(charge, (byte) 1);
        }

        if(distinctPrice.size() >= 3){
            ChargPriceDTO chargPriceDTO = personMap.get(distinctPrice.get(2));
            BigDecimal charge = chargPriceDTO.getCharge();
            BigDecimal serviceCharge = chargPriceDTO.getServiceCharge();
            commandJFPGDTO.getSlxChargePrice()[2] = charge.multiply(unit).intValue();
            commandJFPGDTO.getSlxServicePrice()[2] = serviceCharge.multiply(unit).intValue();
            indexMap.put(charge, (byte) 2);
        }

        if(distinctPrice.size() >= 4){
            ChargPriceDTO chargPriceDTO = personMap.get(distinctPrice.get(3));
            BigDecimal charge = chargPriceDTO.getCharge();
            BigDecimal serviceCharge = chargPriceDTO.getServiceCharge();
            commandJFPGDTO.getSlxChargePrice()[3] = charge.multiply(unit).intValue();
            commandJFPGDTO.getSlxServicePrice()[3] = serviceCharge.multiply(unit).intValue();
            indexMap.put(charge, (byte) 3);
        }

        if(distinctPrice.size() >= 5){
            ChargPriceDTO chargPriceDTO = personMap.get(distinctPrice.get(4));
            BigDecimal charge = chargPriceDTO.getCharge();
            BigDecimal serviceCharge = chargPriceDTO.getServiceCharge();
            commandJFPGDTO.getSlxChargePrice()[4] = charge.multiply(unit).intValue();
            commandJFPGDTO.getSlxServicePrice()[4] = serviceCharge.multiply(unit).intValue();
            indexMap.put(charge, (byte) 4);
        }

        if(distinctPrice.size() >= 6){
            ChargPriceDTO chargPriceDTO = personMap.get(distinctPrice.get(5));
            BigDecimal charge = chargPriceDTO.getCharge();
            BigDecimal serviceCharge = chargPriceDTO.getServiceCharge();
            commandJFPGDTO.getSlxChargePrice()[5] = charge.multiply(unit).intValue();
            commandJFPGDTO.getSlxServicePrice()[5] = serviceCharge.multiply(unit).intValue();
            indexMap.put(charge, (byte) 5);
        }

        String[] timePriceBucket = new String[48];
        Arrays.fill(timePriceBucket, "0");

        //noinspection DuplicatedCode
        objects.forEach(var -> {
            LocalTime startTime = LocalTime.parse(var.getStartTime());
            LocalTime endTime = LocalTime.parse(var.getEndTime());

            int startPeriodIndex = startTime.toSecondOfDay() / 1800;
            int endPeriodIndex = endTime.toSecondOfDay() / 1800;

            // 开始时段紧跟结束时段 ，fill 填充 包含开始不包含结束
            Byte index = indexMap.getOrDefault(var.getCharge(), (byte) 0);
            Arrays.fill(timePriceBucket, startPeriodIndex, endPeriodIndex, index.toString());

            //锁定在一个时间段 Arrays.fill无法填充
            if(startPeriodIndex == endPeriodIndex){
                timePriceBucket[startPeriodIndex] = index.toString();
            }

            // 结尾电价处理 23:30:00
            if(endPeriodIndex == 47){
                timePriceBucket[endPeriodIndex] = index.toString();
            }
        });

        String priceStage = StringUtils.join(timePriceBucket);
        commandJFPGDTO.setPriceStage(priceStage);

        if(log.isDebugEnabled()){
            log.debug("电价下发 SLX price: {}-{}-{}", commandJFPGDTO.getSlxChargePrice(), commandJFPGDTO.getSlxServicePrice(), priceStage);
        }

    }


    /**
     * 峰谷电价
     *
     * @param pileElectricityPriceDTO pileElectricityPriceDTO
     */
    private YKCChargePrice buildJFPGPrice(McElectricityPriceCommandDTO commandJFPGDTO, PileElectricityPriceDTO pileElectricityPriceDTO) {
        Assert.notNull(pileElectricityPriceDTO, "电价下发失败");
        Assert.isTrue(CollectionUtils.isNotEmpty(pileElectricityPriceDTO.getList()), "电价下发失败");
        BigDecimal unit = new BigDecimal("10000");

        List<ChargPriceDTO> objects = pileElectricityPriceDTO.getList();

//        List<ChargPriceDTO> sortedList = objects.stream()
//                .sorted((obj1, obj2) -> obj2.getCharge().compareTo(obj1.getCharge()))
//                .collect(Collectors.toList());

        // 转换集合
        List<BigDecimal> priceList = objects.stream()
                .map(ChargPriceDTO::getCharge)
                .collect(Collectors.toList());

        // 去重 计算 间峰平谷 电价 index 0 1 2 3
        List<BigDecimal> distinctPrice = priceList.stream()
                .distinct().sorted(Comparator.reverseOrder()).collect(Collectors.toList());

        // 解决重复键的冲突策略
        Map<BigDecimal, ChargPriceDTO> personMap = objects.stream()
                .collect(Collectors.toMap(ChargPriceDTO::getCharge, var -> var, (existing, incoming) -> existing));

        // 间峰平谷电下标 0 1 2 3
        Map<BigDecimal, Byte> jfpgIndexMap = new HashMap<>();


        JSONObject jsonLog = new JSONObject();
        if(!distinctPrice.isEmpty()){
            ChargPriceDTO chargPriceDTO = personMap.get(distinctPrice.get(0));
            BigDecimal charge = chargPriceDTO.getCharge();
            BigDecimal serviceCharge = chargPriceDTO.getServiceCharge();
            commandJFPGDTO.setJPrice(charge.multiply(unit).intValue());
            commandJFPGDTO.setJPriceS(serviceCharge.multiply(unit).intValue());
            jfpgIndexMap.put(charge, (byte) 0);

            jsonLog.put("j", charge);
            jsonLog.put("jS", serviceCharge);
        }

        if(distinctPrice.size() >= 2){
            ChargPriceDTO chargPriceDTO = personMap.get(distinctPrice.get(1));
            BigDecimal charge = chargPriceDTO.getCharge();
            BigDecimal serviceCharge = chargPriceDTO.getServiceCharge();
            commandJFPGDTO.setFPrice(charge.multiply(unit).intValue());
            commandJFPGDTO.setFPriceS(serviceCharge.multiply(unit).intValue());
            jfpgIndexMap.put(charge, (byte) 1);

            jsonLog.put("f", charge);
            jsonLog.put("fS", serviceCharge);
        }

        if(distinctPrice.size() >= 3){
            ChargPriceDTO chargPriceDTO = personMap.get(distinctPrice.get(2));
            BigDecimal charge = chargPriceDTO.getCharge();
            BigDecimal serviceCharge = chargPriceDTO.getServiceCharge();
            commandJFPGDTO.setPPrice(charge.multiply(unit).intValue());
            commandJFPGDTO.setPPriceS(serviceCharge.multiply(unit).intValue());
            jfpgIndexMap.put(charge, (byte) 2);

            jsonLog.put("p", charge);
            jsonLog.put("pS", serviceCharge);
        }

        if(distinctPrice.size() >= 4){
            ChargPriceDTO chargPriceDTO = personMap.get(distinctPrice.get(3));
            BigDecimal charge = chargPriceDTO.getCharge();
            BigDecimal serviceCharge = chargPriceDTO.getServiceCharge();
            commandJFPGDTO.setGPrice(charge.multiply(unit).intValue());
            commandJFPGDTO.setGPriceS(serviceCharge.multiply(unit).intValue());
            jfpgIndexMap.put(charge, (byte) 3);

            jsonLog.put("g", charge);
            jsonLog.put("gS", serviceCharge);
        }

        byte[] priceBucketJFPG = new byte[48];
        Arrays.fill(priceBucketJFPG, (byte) 0);

        objects.forEach(var -> {
            LocalTime startTime = LocalTime.parse(var.getStartTime());
            LocalTime endTime = LocalTime.parse(var.getEndTime());

            int startPeriodIndex = startTime.toSecondOfDay() / 1800;
            int endPeriodIndex = endTime.toSecondOfDay() / 1800;

            // 开始时段紧跟结束时段 ，fill 填充 包含开始不包含结束
            Byte index = jfpgIndexMap.getOrDefault(var.getCharge(), (byte) 0);
            Arrays.fill(priceBucketJFPG, startPeriodIndex, endPeriodIndex, index);

            //锁定在一个时间段 Arrays.fill无法填充
            if(startPeriodIndex == endPeriodIndex){
                priceBucketJFPG[startPeriodIndex] = index;
            }

            // 结尾电价处理 23:30:00
            if(endPeriodIndex == 47){
                priceBucketJFPG[endPeriodIndex] = index;
            }
        });

        jsonLog.put("timeBucket >>> ", HexExtUtil.encodeHexStrFormat(priceBucketJFPG, StringPool.SPACE));
        commandJFPGDTO.setPriceBucketJFPG(priceBucketJFPG);

        if(log.isDebugEnabled()){
            log.info("尖峰平谷 电价：{}", jsonLog);
        }

        YKCChargePrice ykcChargePrice = new YKCChargePrice();
        ykcChargePrice.setJPrice(commandJFPGDTO.getJPrice());
        ykcChargePrice.setFPrice(commandJFPGDTO.getFPrice());
        ykcChargePrice.setPPrice(commandJFPGDTO.getPPrice());
        ykcChargePrice.setGPrice(commandJFPGDTO.getGPrice());

        ykcChargePrice.setJPriceS(commandJFPGDTO.getJPriceS());
        ykcChargePrice.setFPriceS(commandJFPGDTO.getFPriceS());
        ykcChargePrice.setPPriceS(commandJFPGDTO.getPPriceS());
        ykcChargePrice.setGPriceS(commandJFPGDTO.getGPriceS());
        ykcChargePrice.setPriceBucketJFPG(commandJFPGDTO.getPriceBucketJFPG());
        return ykcChargePrice;
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


}
