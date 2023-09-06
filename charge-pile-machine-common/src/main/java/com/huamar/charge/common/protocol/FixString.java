package com.huamar.charge.common.protocol;

/**
 * 定长字符串
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public class FixString extends DataType {

	public static final String ENCODING = "GBK";

	private byte[] data;

	public FixString() {
		data = new byte[0];
	}

	public FixString(byte[] data) {
		this.data = data;
	}
	
	public void setData(byte[] data) {
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
