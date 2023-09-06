package com.huamar.charge.pile.entity.dto.event;

import com.huamar.charge.common.common.BaseDTO;
import com.huamar.charge.common.common.codec.BCD;
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
	private String idCode;

	/**
	 * 事件状态  0x01表示事件开始    0x02表示事件结束  0xFF 表示事件异常结束，例如掉电、重启等
	 */
	private byte eventState;

	/**
	 * 事件类型
	 */
	private byte eventType;

	/**
	 * 事件开始时间
	 */
	private BCD eventStartTime;

	/**
	 * 事件结束时间
	 */
	private BCD eventEndTime;

	/**
	 * 事件数据
	 */
	private byte[] eventData;

}
