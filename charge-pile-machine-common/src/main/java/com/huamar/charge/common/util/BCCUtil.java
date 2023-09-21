package com.huamar.charge.common.util;

/**
 * BCC校验工具
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public class BCCUtil {



	/**
	 * bcc 校验, 包含起始位置与结束位置
	 *
	 * @param data data
	 * @param startIndex startIndex
	 * @param endIndex endIndex
	 * @return byte
	 */
	public static byte calculateBCC(byte[] data, int startIndex, int endIndex) {
		if (startIndex < 0 || endIndex >= data.length || startIndex > endIndex) {
			throw new IllegalArgumentException("无效的开始和结束位置");
		}

		byte bcc = 0;

		for (int i = startIndex; i <= endIndex; i++) {
			bcc ^= data[i];
		}

		return bcc;
	}


}
