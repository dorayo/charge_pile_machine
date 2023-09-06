package com.huamar.charge.common.protocol;


import com.huamar.charge.common.util.ByteExtUtil;

/**
 * desc
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public class UnsignedInt extends DataType {

	public static final byte LENGTH = 2;

	/**
	 * 十六进制
	 */
	private byte[] data;

	public UnsignedInt(byte[] data) {
		if (data.length != LENGTH) {
			return;
		}
		this.data = data;
	}
	
	public UnsignedInt(String dataStr) {
		this.data = dataStr.getBytes();
	}
	
	public UnsignedInt(short data) {
		
	}

	@Override
	public String toString() {
		return toHex() + " ";
	}

	@Override
	public byte[] getData() {
		return data;
	}

	public short shortValue() {
		return ByteExtUtil.getShort(data, 0);
	}
}
