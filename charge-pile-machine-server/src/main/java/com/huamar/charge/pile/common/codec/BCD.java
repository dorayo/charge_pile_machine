package com.huamar.charge.pile.common.codec;

import com.huamar.charge.pile.util.HexExtUtil;
import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * 设备应答接口
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Data
@AllArgsConstructor
public class BCD  {

	private byte[] data;

	@Override
	public String toString() {
		return toHex();
	}

	public String toHex() {
		return HexExtUtil.encodeHexStr(data);
	}

}
