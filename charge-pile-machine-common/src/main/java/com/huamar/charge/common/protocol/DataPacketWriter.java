package com.huamar.charge.common.protocol;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * DataPacketWriter
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public class DataPacketWriter {

	private static final Logger log = LoggerFactory.getLogger(DataPacketReader.class);

	public final Charset charset = StandardCharsets.UTF_8;

	public final Charset GBK = Charset.forName("GBK");

	@Getter
	private final ByteBuffer buffer;

	public DataPacketWriter(int size) {
		buffer = ByteBuffer.allocate(size);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
	}

	public DataPacketWriter() {
		buffer = ByteBuffer.allocate(2048);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
	}

	public void write(byte data) {
		try {
			buffer.put(data);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public void write(short data) {
		try {
			buffer.putShort(data);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public void write(int data) {
		try {
			buffer.putInt(data);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public void write(byte[] data) {
		try {
			buffer.put(data);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	public void write(String data) {
		try {
			buffer.put(data.getBytes(GBK));
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	public void write(String data, int len) {
		byte[] bytes = data.getBytes();
		if (bytes.length < len) {
			byte[] bytes2 = new byte[len];
			System.arraycopy(bytes, 0, bytes2, 0, bytes.length);
			bytes = bytes2;
		}
		write(bytes);
	}

	public void write(DataType data) {
		write(data.getData());
	}


	public byte[] toByteArray() {
		int position = buffer.position();
		int limit = buffer.limit();
		buffer.flip();
		byte[] bytes = new byte[position];
		buffer.get(bytes, 0, position);
		buffer.limit(limit);
		buffer.position(position);
		return bytes;
	}

}
