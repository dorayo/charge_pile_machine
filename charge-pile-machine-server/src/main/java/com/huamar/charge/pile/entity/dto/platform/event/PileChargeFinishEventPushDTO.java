package com.huamar.charge.pile.entity.dto.platform.event;

import com.huamar.charge.common.common.codec.BCD;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 充电结束统计事件
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PileChargeFinishEventPushDTO extends PileEventPushBaseDTO {

    /**
     * 1 中止荷电状态 SOC BYTE 有效值范围：0～100（表示 0%～100%）单位：1%/bit，偏移 0
     */
    public Integer terminationElectricityState;

    /**
     * 2 动力蓄电池单体最低电压 WORD 单位：0.001V/bit， 偏移 0V
     */
    public Integer batteryMinVoltage;

    /**
     * 2 动力蓄电池单体最高电压 WORD 单位：0.001V/bit，偏移 0V
     */
    public short batteryMaxVoltage;

    /**
     * 1 动力蓄电池最低温度 BYTE 单位：1℃/bit，偏移-40℃
     */
    public Integer batteryMinTemperature;

    /**
     * 1 动力蓄电池最高温度 BYTE 单位：1℃/bit，偏移-40℃
     */
    public Integer batteryMaxTemperature;

    /**
     * 用于保存管理员输入密码启动充电订单开始时间,结束时间暂时用开始时间+充电时长
     */
    public BCD startTime;

    /**
     * 4 累计充电时间 WORD 单位：，1Min/bit， 偏移 0
     */
    public Integer cumulativeChargeTime;

    /**
     * 2 输出能量 WORD 有效值范围：0～9999（表示 0 kW•h～999.9 kW•h）单位：0.01kW•h/bit，偏移 0缺省值：0xFFFE；无效值：0xFFFF
     */
    public Integer outPower;

    /**
     * 1 充电枪编号 BYTE 充电桩/补电车中充电枪对应编号范围 1-254，0xff 表示无效
     */
    public Integer gunSort;

    /**
     * 4 充电金额 DWORD 当前已充电的金额，0.01 元/bit，0xFFFFFFFF 表示无效
     */
    public Integer chargeMoney;

    /**
     * 4 服务费 DWORD 当前已充电的金额，0.01 元/bit，0xFFFFFFFF 表示无效
     */
    public Integer serviceMoney;

    /**
     * 17 车辆识别码 BYTE[17] 默认 ASCII 码，不足 17 位，后面补’\0’
     */
    public String carIdentificationCode;

    /**
     * 32 订单流水号 STRING[32] 唯一标识当前充电的业务单号，不足则在?尾以‘\0’填充
     */
    public String orderSerialNumber;

    /**
     * 2  结束原因
     */
    public Integer endReason;

    /**
     * 1 开始soc
     */
    public Integer startSoc;
}
