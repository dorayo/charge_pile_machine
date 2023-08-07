package com.huamar.charge.pile.dto.fault;

import com.huamar.charge.pile.dto.BaseReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 充电桩故障上报
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class McFaultPutReqDTO extends BaseReqDTO {

	/**
	 * 故障枪数量
	 */
	public byte gunCount;

	/**
	 * 故障枪号
	 */
	public byte gunNumber;

	/**
	 * 故障状态 1 故障；2 解除
	 */
	public byte status;

	/**
	 * 故障代码
	 */
	public short descId;

	/**
	 * 故障描述
	 */
	public String attributes;

}
