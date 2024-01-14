package com.huamar.charge.common.protocol;


import com.huamar.charge.common.util.HexExtUtil;
import lombok.Data;

/**
 * 网络消息包
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@SuppressWarnings("unused")
@Data
public class PacketBuilder {

	private String msgBodyAttr = "00";

	private DataPacket dataPacket;


	private PacketBuilder() {

	}

	/**
	 * builder
	 *
	 * @return DataPacketBuilder
	 */
	public static PacketBuilder builder(){
		return new PacketBuilder().answerBuilder();
	}

	/**
	 * 构建应答对象
	 * @return DataPacketBuilder
	 */
	public PacketBuilder answerBuilder(){
		this.dataPacket = new DataPacket();
		this.dataPacket.setTag(DataPacket.TAG);
		this.dataPacket.setMsgBodyAttr(HexExtUtil.decodeHex(msgBodyAttr)[0]);
		this.dataPacket.setTagEnd(DataPacket.TAG);
		return this;
	}

	public PacketBuilder messageNumber(int number){
		this.dataPacket.setMsgNumber(number);
		return this;
	}

	/**
	 * 设置IdCode
	 * @param msgId msgId
	 * @return DataPacketBuilder
	 */
	public PacketBuilder messageId(String msgId){
		this.dataPacket.setMsgId(HexExtUtil.decodeHex(msgId)[0]);
		return this;
	}

	/**
	 * idCode
	 * @param idCode idCode
	 * @return DataPacketBuilder
	 */
	public PacketBuilder idCode(String idCode){
		this.dataPacket.setIdCode(idCode.getBytes());
		return this;
	}


	/**
	 * 设置请求体
	 * @param writer writer
	 * @return DataPacketBuilder
	 */
	public PacketBuilder body(DataPacketWriter writer){
		byte[] byteArray = writer.toByteArray();
		dataPacket.setMsgBodyLen(byteArray.length);
		dataPacket.setMsgBody(byteArray);
		return this;
	}

	/**
	 * 构建DataPacket
	 * @return DataPacket
	 */
	public DataPacket build(){
		return this.dataPacket;
	}

}
