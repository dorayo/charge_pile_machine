package com.huamar.charge.common.protocol;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 异常解析网络消息包
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FailMathPacket extends BasePacket {

	public static final Charset CHAR_SET = StandardCharsets.UTF_8;

	/**
	 * 十六进制=23
	 */
	public static final byte TAG = 35;


	/**
	 * 8.消息体
	 */
	private byte[] body;

	public FailMathPacket(byte[] body) {
		this.body = body;
	}

	/**
	 * 获取
	 *
	 * @return byte[]
	 */
	@Override
	byte[] getBytes() {
		return new byte[0];
	}
}
