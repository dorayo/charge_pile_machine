package com.huamar.charge.pile.entity.dto.command;

import com.huamar.charge.common.protocol.NumberFixStr;
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
	 * SLX
	 * 电价 1~6 WORD 非零值，单位 0.01 元
	 */
	private short price1;
	private short price2;
	private short price3;
	private short price4;
	private short price5;
	private short price6;


	/**
	 * SLX
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


	/**
	 * YKC 云快冲
	 */
	private int jPrice = 0;
	private int fPrice = 0;
	private int pPrice = 0;
	private int gPrice = 0;

	/**
	 * YKC 云快冲 服务费
	 */
	private int jPriceS = 0;
	private int fPriceS = 0;
	private int pPriceS = 0;
	private int gPriceS = 0;

	//ykc 峰谷4时段电价
	private byte[] priceBucketJFPG = new byte[48];


	private int[] slxChargePrice = new int[6];

	private int[] slxServicePrice = new int[6];

	private String priceStage = "";

	public McElectricityPriceCommandDTO() {
		// 设置命令行字节长度，SLX 协议使用
		this.fieldsByteLength = (byte) (1 + 2 + 2 + 2 + 2 + 2 + 2 +2 + 2 + 2 + 2 + 2 + 2 + 48);
	}
}
