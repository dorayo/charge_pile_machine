package com.huamar.charge.pile.protocol;


import com.huamar.charge.pile.enums.ProtocolCodeEnum;
import com.huamar.charge.pile.server.service.MachineContext;
import com.huamar.charge.pile.util.HexExtUtil;
import lombok.Data;

/**
 * 网络消息包
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
@SuppressWarnings("unused")
@Data
public class DataPacketBuilder {

	private String msgBodyAttr = "00";

	private DataPacket dataPacket;

	private MachineContext machineContext;

	private DataPacketBuilder() {
	}

	/**
	 * builder
	 *
	 * @param machineContext machineContext
	 */
	private DataPacketBuilder(MachineContext machineContext) {
		this.machineContext = machineContext;
	}

	/**
	 * builder
	 *
	 * @return DataPacketBuilder
	 */
	public static DataPacketBuilder builder(MachineContext context){
		return new DataPacketBuilder(context).answerBuilder();
	}

	/**
	 * 构建应答对象
	 * @return DataPacketBuilder
	 */
	public DataPacketBuilder answerBuilder(){
		this.dataPacket = new DataPacket();
		this.dataPacket.setTag(DataPacket.TAG);
		// TODO:临时写死消息体属性
		this.dataPacket.setMsgBodyAttr(HexExtUtil.decodeHex(msgBodyAttr)[0]);
		this.dataPacket.setMsgNumber(machineContext.getMessageNumber());
		this.dataPacket.setTagEnd(DataPacket.TAG);
		return this;
	}

	/**
	 * 设置IdCode
	 * @param protocolCodeEnum protocolCodeEnum
	 * @return DataPacketBuilder
	 */
	public DataPacketBuilder messageId(ProtocolCodeEnum protocolCodeEnum){
		this.dataPacket.setMsgId(HexExtUtil.decodeHex(protocolCodeEnum.getCode())[0]);
		return this;
	}

	/**
	 * idCode
	 * @param idCode idCode
	 * @return DataPacketBuilder
	 */
	public DataPacketBuilder idCode(String idCode){
		this.dataPacket.setIdCode(idCode.getBytes());
		return this;
	}


	/**
	 * 设置请求体
	 * @param writer writer
	 * @return DataPacketBuilder
	 */
	public DataPacketBuilder body(DataPacketWriter writer){
		byte[] byteArray = writer.toByteArray();
		dataPacket.setMsgBodyLen((short) byteArray.length);
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
