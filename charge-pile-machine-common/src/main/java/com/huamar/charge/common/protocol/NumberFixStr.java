package com.huamar.charge.common.protocol;

import lombok.Setter;

/**
 * 定长数字字符串
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Setter
public class NumberFixStr extends DataType {

	private byte[] data;

	public NumberFixStr() {
		data = new byte[0];
	}

	public NumberFixStr(byte[] data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return new String(data);
	}

	@Override
	public byte[] getData() {
		return data;
	}
	
}
