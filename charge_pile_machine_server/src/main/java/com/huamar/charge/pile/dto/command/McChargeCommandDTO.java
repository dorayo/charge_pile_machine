package com.huamar.charge.pile.dto.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 充电控制
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McChargeCommandDTO extends McBaseCommandDTO {

	public McChargeCommandDTO() {
		// 设置命令行字节长度
		this.fieldsByteLength = 43;
	}

	/**
	 * 充电控制 BYTE 0x00：结束充电， 0x01：开启充电
	 */
	private byte chargeControl;

	/**
	 * 充电枪编号
	 */
	private byte gunSort;

	/**
	 * 充电结束方式 BYTE 0x00：充满为止 0x01：电量控制 0x02：时间控制 0x03：金额控制
	 */
	private byte chargeEndType;


	/**
	 * 充电结束值 DWORD
	 * 	充电结束方式=0x00：无意义
	 *	充电结束方式=0x01：表示电量值，单位 0.01kWh
	 *	充电结束方式=0x02：表示时间值，单位 1min
	 *	充电结束方式=0x03：表示金额值，单位 0.01 元
	 */
	private int chargeEndValue;

	/**
	 * 订单流水号 STRING[32] 唯一标识当前充电的业务单号，不足则在末尾以‘ \0’ 填充
 	 */
	private byte[] orderSerialNumber = new byte[32];

	/**
	 * 当前账号余额 0.01元/bit
	 */
	private int balance;

}
