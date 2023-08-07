package com.huamar.charge.pile.dto.command;

import com.huamar.charge.pile.protocol.NumberFixStr;
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
public class McElectricityPriceCommandDTO extends McBaseCommandDTO {

	/**
	 * 枪号
	 */
	private byte gunSort;

	/**
	 * 电价 1~6 WORD 非零值，单位 0.01 元
	 */
	private short price1;
	private short price2;
	private short price3;
	private short price4;
	private short price5;
	private short price6;


	/**
	 * 服务费 1~6 WORD 非零值，单位 0.01 元
	 */
	private short servicePrice1;
	private short servicePrice2;
	private short servicePrice3;
	private short servicePrice4;
	private short servicePrice5;
	private short servicePrice6;

	/**
	 *	费率时间段 STRING[48]
	 *	全天每半小时为 1 个时间段，总共 48 个时间段每个时间段取值为 0~3，对应电价 1~4
	 */
	private NumberFixStr timeStage;



	public McElectricityPriceCommandDTO() {
		// 设置命令行字节长度
		this.fieldsByteLength = (byte) (1 + 2 + 2 + 2 + 2 + 2 + 2 +2 + 2 + 2 + 2 + 2 + 2 + 48);
	}
}
