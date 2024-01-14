package com.huamar.charge.pile.entity.dto;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 充电阶段信息
 *
 * @author TiAmo(13721682347@163.com)
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChargeStageDataDTO extends BaseReqDTO {

	/**
	 * 2 电池充电电压需求 WORD 单位：0.1V/bit，偏移 0
	 */
	private int batteryChargeVoltage;

	/**
	 * 2 电池充电电流需求 WORD 单位：0.1A/bit，偏移-1600A
	 */
	private int batteryChargeElectricity;

	/**
	 * 1 当前荷电状态 SOC BYTE 有效值范围：0～100（表示 0%～100%） 单位：1%/bit，偏移 0
	 */
	private byte electricityState;

	/**
	 * 2 最高单体动力蓄电池电压 WORD 单位：0.001V/bit，偏移 0
	 */
	private int maxVoltage;

	/**
	 * 2 最高单体蓄电池电压编号 WORD 范围 1-65535
	 */
	private int maxVoltageNumber;

	/**
	 * 1 最高动力蓄电池温度 BYTE 单位：1℃/bit，偏移-40℃
	 */
	private short maxTemperature;

	/**
	 * 最高温度检测点编号 BYTE 范围 1-255
	 */
	private short maxTemperatureNumber;

	/**
	 * 1 最低动力蓄电池温度 BYTE 单位：1℃/bit，偏移-40℃
	 */
	private short minTemperature;

	/**
	 * 1 最低温度检测点编号 BYTE 范围 1-255
	 */
	private short minTemperatureNumber;

	/**
	 * 2 估算剩余充电时间 WORD 单位：1Min/bit， 范围 1-65535
	 */
	private int remainChargeTime;

	/**
	 * 2 充电桩电压输出值 WORD 单位：0.1V/bit，偏移 0V
	 */
	private int pileVoltageOutValue;

	/**
	 * 2 充电桩电流输出值 WORD 单位：0.1A/bit， 偏移-1600A
	 */
	private int pileElectricityOutValue;

	/**
	 * 1 充电枪编号 BYTE 充电桩/补电车中充电枪对应编号 范围 1-254，0xff 表示无效
	 */
	private byte gunSort;

}
