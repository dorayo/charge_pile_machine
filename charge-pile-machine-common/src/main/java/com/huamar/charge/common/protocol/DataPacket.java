package com.huamar.charge.common.protocol;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 网络消息包
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DataPacket extends BasePacket {

	public static final Charset CHAR_SET = StandardCharsets.UTF_8;

	/**
	 * 十六进制=23
	 */
	public static final byte TAG = 35;



	/**
	 * 1.标识位
	 */
	private byte tag;

	/**
	 * 2.消息ID（1）
	 */
	private byte msgId;

	/**
	 *
	 */
	private byte msgBodyAttr;

	/**
	 * 4.消息体长度（2）
	 */
	private short msgBodyLen;

	/**
	 * <pre>
	 * 5.消息流水号（2）
	 * 从１开始，每包消息加１，当达到 65535 后，重新从 1 开始
	 * </pre>
	 */
	private short msgNumber;

	/**
	 * 6.设备识别码（18）
	 */
	private byte[] idCode;

	/**
	 * 8.消息体
	 */
	private byte[] msgBody;

	/**
	 * 9.校验位
	 */
	private byte checkTag;

	/**
	 * 10.标识位
	 */
	private byte tagEnd;

}
