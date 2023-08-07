package com.huamar.charge.pile.protocol;

import lombok.Data;

/**
 * 分包
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@Data
public class SplitPackTag {

	private UnsignedInt splitNumber;
	
	private UnsignedInt packNum;
	
	private UnsignedInt packIndex;

}
