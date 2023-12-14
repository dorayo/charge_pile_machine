package com.huamar.charge.pile.entity.dto.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 远程控制-背光灯下发
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ADLampCommandDTO extends McBaseCommandDTO {

	private byte onHour;

	private byte onMinute;

	private byte offHour;

	private byte offMinute;

}
