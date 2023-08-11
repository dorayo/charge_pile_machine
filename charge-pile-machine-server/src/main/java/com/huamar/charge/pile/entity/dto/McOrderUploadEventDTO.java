package com.huamar.charge.pile.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 订单上传事件
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McOrderUploadEventDTO extends McEventBaseDTO  {

	/**
	 * 卡号长度 BYTE 长度 1-255
	 */
	public byte cardNumberLen;
	/**
	 * N 卡号 STRING [n]
	 */
	public String cardNumber;
	/**
	 * 充电结束方式 BYTE 0x00：充满为止 0x01：电量控制 0x02：时间控制 0x03：金额控制
	 */
	public byte overType;
	/**
	 * 充电结束值 DWORD
	 * 充电结束方式=0x00：无意义
	 * 充电结束方式=0x01：表示电量值，单位 0.01kWh
	 * 充电结束方式=0x02：表示时间值，单位 1min
	 * 充电结束方式=0x03：表示金额值，单位 0.01 元
	 */
	public int overValue;
	/**
	 * 充电枪编号 BYTE 充电桩/补电车中充电枪对应编号 范围 1-254，0xff 表示无效
	 */
	public byte gunSort;
	/**
	 * 32 订单流水号 STRING[32] 唯一标识当前充电的业务单号，不足则在?尾以‘\0’填充
	 */
	public String orderSerialNumber;
}
