package com.huamar.charge.pile.dto.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 远程控制-卡查询结果
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McCardQueryCommandDTO extends McBaseCommandDTO {

	/**
	 * 查询结果 BYTE 0x01 查询成功 0x02 查询失败
	 */
	private byte result;

	/**
	 * 卡状态 BYTE 0x01 在用 0x02 挂失 0x03 库存 0xFF 未知
	 */
	private byte cardState;

	/**
	 * 卡金额 DWORD 0.01 元/bit
	 */
	private int money;

	/**
	 * 备注长度 WORD 如果查询结果是 0x01，备注长度为 0；
	 */
	private short descLen;

	/**
	 * 备注 STRING 查询失败原因
	 */
	private String desc;

	/**
	 * 32 订单流水号 STRING[32] 唯一标识当前充电的业务单号，不足则在?尾以‘\0’填充
	 */
	public String orderSerialNumber;

	/**
	 * 充电枪号
	 */
	public byte gunSort;

	public McCardQueryCommandDTO() {
		// 设置命令行字节长度
		this.fieldsByteLength = (byte) (1 + 1 + 4 + 2 + descLen + 32 + 1);
	}
}
