package com.huamar.charge.pile.entity.dto.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 远程控制-二维码下发
 * date 2023/07/25
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McQrCodeCommandDTO extends McBaseCommandDTO {

	/**
	 * URL字节长度
	 */
	private byte urlLength;
	/**
	 * URL
	 */
	private String url;

	public McQrCodeCommandDTO() {
		// 设置命令行字节长度
		this.fieldsByteLength = (byte) (1 + urlLength);
	}
}
