package com.huamar.charge.pile.common.codec;

import com.huamar.charge.pile.util.HexExtUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

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
