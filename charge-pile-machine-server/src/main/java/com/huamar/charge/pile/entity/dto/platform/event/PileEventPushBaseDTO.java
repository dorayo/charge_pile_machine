package com.huamar.charge.pile.entity.dto.platform.event;

import com.huamar.charge.common.common.BaseDTO;
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
public class PileEventPushBaseDTO extends BaseDTO {

	/**
	 * idCode
	 */
	protected String idCode;

	/**
	 * 事件状态
	 * 1: 0x01表示事件开始
	 * 2: 0x02表示事件结束
	 * -1: 0xFF 表示事件异常结束，例如掉电、重启等
	 */
	protected Integer eventState;

	/**
	 * 事件类型
	 */
	protected Integer eventType;

	/**
	 * 事件开始时间
	 */
	protected String eventStartTime;
	/**
	 * 事件结束时间
	 */
	protected String eventEndTime;

}
