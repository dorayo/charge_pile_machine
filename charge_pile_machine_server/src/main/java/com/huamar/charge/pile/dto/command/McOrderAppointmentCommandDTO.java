package com.huamar.charge.pile.dto.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 远程控制-电价下发
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McOrderAppointmentCommandDTO extends McBaseCommandDTO {

	/**
	 * 订单流水号 STRING[32] 唯一标识当前充电的业务单号，不足则在?尾以‘\0’填充
	 */
	public String orderSerialNumber;

	/**
	 * 当前已充电的金额，0.01元/bit，0xFFFFFFFF表示无效
	 */
	public int curMoney;

	/**
	 * 服务费  0.01元/bit，
	 */
	private int serviceMoney;

	/**
	 * 电量 0.01kw*h/bit
	 */
	private int outPower;

	/**
	 * 枪号
	 */
	public byte gunSort;

	/**
	 * 累积充电时间
	 */
	private int cumulativeTime;

	public McOrderAppointmentCommandDTO() {
		// 设置命令行字节长度
		this.fieldsByteLength = (byte) (32 + 4 + 4 + 1);
	}
}
