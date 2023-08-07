package com.huamar.charge.pile.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 充电桩主动请求开机
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McStartupChargeEvent extends McEventBaseDTO {

	/**
	 * 枪索引 1-10
	 */
	public byte gunSort;
	/**
	 * 重新开机原因 1 重启开机 2 异常跳枪......
	 */
	public byte restartReason;
	/**
	 * 上次充电结束SOC
	 */
	public byte lastSoc;
	/**
	 * 上次充电的订单流水号 STRING[32]
	 */
	public String orderSerialNumber;
	/**
	 * 预留字节1
	 */
	public int reserve1;
	/**
	 * 预留字节2
	 */
	public int reserve2;

}
