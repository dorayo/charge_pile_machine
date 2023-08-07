package com.huamar.charge.pile.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 开始升升级事件
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McPileUpgradeEventDTO extends McEventBaseDTO {

	/**
	 * 是否开始升级  0x01:开始升级   0x02:停止升级
	 */
	public byte uploadType;

}
