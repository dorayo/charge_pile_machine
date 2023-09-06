package com.huamar.charge.common.util;

/**
 * BCC校验工具
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public class BCCUtil {

	/**
	 * 获取BCC校验码
	 * @param data data
	 * @return String
	 */
	public static String bcc(byte[] data, int start, int end) {
		String ret = "";
		byte[] bcc = new byte[1];
		for (int i = start; i < data.length; i++) {
			if (end == i) {
				break;
			}
			bcc[0] = (byte) (bcc[0] ^ data[i]);
		}
		String hex = Integer.toHexString(bcc[0] & 0xFF);
		if (hex.length() == 1) {
			hex = '0' + hex;
		}
		ret += hex.toUpperCase();
		return ret;
	}
}
