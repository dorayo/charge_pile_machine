package com.huamar.charge.common.protocol;


import com.huamar.charge.common.util.HexExtUtil;

/**
 * 二进制数据包
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public abstract class DataType {

	public String toHex() {
		String bytes2Hex = HexExtUtil.encodeHexStr(getData());
		return bytes2Hex.toUpperCase();
	}
	
	public abstract byte[] getData();
	
}
