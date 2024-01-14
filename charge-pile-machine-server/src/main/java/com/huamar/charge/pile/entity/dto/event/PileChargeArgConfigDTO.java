package com.huamar.charge.pile.entity.dto.event;

import com.alibaba.fastjson.annotation.JSONField;
import com.huamar.charge.common.common.codec.BCD;
import com.huamar.charge.common.util.json.BCDHexValueSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 事件汇报请求-充电参数配置请求
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PileChargeArgConfigDTO extends PileEventBaseDTO {
	
	// 单体动力蓄电池最高允许充电电压 WORD 单位：0.001V/bit， 偏移 0V
	public int maxValidVoltage;

	// 最高允许充电电流 WORD 单位：0.1A/bit， 偏移-1600A
	public int maxValidElectricity;

	// 动力蓄电池标称总能量 D WORD 单位：0.1KW.h/bit，偏移 0
	public int maxTotalPower;

	// 最高允许充电总电压 WORD 单位：0.1V/bit，偏移 0
	public int maxValidTotalVoltage;

	// 最高允许温度 BYTE 单位：1℃/bit，偏移-40℃
	public byte maxValidTemperature;

	// 整车动力蓄电池荷电状态 BYTE 有效值范围：0～100（表示 0%～100%） 单位：1%/bit，偏移 0
	public byte carElectricityState;

	// 整车动力蓄电池总电压 WORD 单位：0.1V/bit，偏移 0V
	public int carTotalVoltage;

	// 充电机同步时间 BCD[6] 见表 14，年月日时分秒
	@JSONField(serializeUsing = BCDHexValueSerializer.class)
	public BCD synTime;

	// 充电机最高输出电压 WORD 单位：0.1V/bit，偏移 0V
	public int chargerMaxOutVoltage;

	// 充电机最低输出电压 WORD 单位：0.1V/bit，偏移 0V
	public int chargerMinOutVoltage;

	// 充电机最大输出电流 WORD 单位 0.1A/bit，偏移-1600A
	public int chargerMaxOutElectricity;

	// BMS 充电准备就绪状态 BYTE 0 未知，1 准备就绪，2 故障
	public byte bmsReadyState;

	// 充电机输出准备就绪状态 BYTE 0 未知，1 准备就绪，2 故障
	public byte chargerOutReadyState;

	// 充电枪编号 BYTE 充电桩/补电车中充电枪对应编号 范围 1-254，0xff 表示无效
	public byte gunSort;

}
