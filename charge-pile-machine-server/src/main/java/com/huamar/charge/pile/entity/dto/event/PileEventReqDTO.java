package com.huamar.charge.pile.entity.dto.event;

import com.huamar.charge.pile.common.BaseDTO;
import com.huamar.charge.pile.common.codec.BCD;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 事件汇报请求
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PileEventReqDTO extends BaseDTO {

	/**
	 * idCode
	 */
	public String idCode;

	/**
	 * 事件状态  0x01表示事件开始    0x02表示事件结束  0xFF 表示事件异常结束，例如掉电、重启等
	 */
	public byte eventState;

	/**
	 * 事件类型
	 */
	public byte eventType;

	/**
	 * 事件开始时间
	 */
	public BCD eventStartTime;
	/**
	 * 事件结束时间
	 */
	public BCD eventEndTime;

	/**
	 * 事件数据
	 */
	private byte[] eventData;

}
