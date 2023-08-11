package com.huamar.charge.pile.entity.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * VIN白名单事件
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McVinWhiteListEventDTO extends McEventBaseDTO {

	// 17 车辆识别码 BYTE[17] 默认 ASCII 码，不足 17 位，后面补’\0’
	public String carIdentificationCode;
	//枪序号
	public byte gunSort;

}
