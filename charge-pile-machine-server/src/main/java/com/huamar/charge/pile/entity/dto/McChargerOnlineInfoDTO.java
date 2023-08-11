package com.huamar.charge.pile.entity.dto;
import com.huamar.charge.pile.common.codec.BCD;
import com.huamar.charge.pile.enums.PileStateEnum;
import lombok.Data;

/**
 * 充电机实时状态信息
 * 地面充电机实时状态信息格式(每个充电枪一个状态)
 * 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@Data
public class McChargerOnlineInfoDTO {

	/**
	 * 充电枪编号 充电桩/补电车中充电枪对应编号 范围 1-254，0xff 表示无效
	 */
	public byte gunSort;
	/**
	 * 充电枪状态
	 * 0.空闲未连接
	 * 1.空闲已连接
	 * 2.握手
	 * 3.配置
	 * 4.充电中
	 * 5.结束
	 * 6.故障
	 */
	public byte gunState = PileStateEnum.FREE.getCode();
	/**
	 * 充电开始时间
	 */
	public BCD startTime;
	/**
	 * 累计充电时间 单位 1Min/bit，有效值 1-65535，偏移0
	 */
	public int cumulativeTime;
	/**
	 * 当前已充电的金额,电费，0.01元/bit，0xFFFFFFFF表示无效
	 */
	public int curMoney;
	/**
	 * 服务费，0.01元/bit，0xFFFFFFFF表示无效
	 */
	public int serviceMoney;
	/**
	 * 实际充电量 单位：0.1Kw.h/bit，偏移 0
	 */
	public int curChargeQuantity;
	/**
	 * 故障码1
	 */
	public int faultCode1;
	/**
	 * 故障码2
	 */
	public int faultCode2;
	/**
	 * 充电枪数量(用来兼容老协议)
	 */
	public byte gunNum;


}
