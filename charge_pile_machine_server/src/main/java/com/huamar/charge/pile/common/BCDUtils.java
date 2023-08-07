package com.huamar.charge.pile.common;

import com.huamar.charge.pile.common.codec.BCD;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * BCD
 * 2023/07/24
 *
 * @author TiAmo(13721682347@163.com)
 */
public class BCDUtils {

    /**
     * bcdTime
     *
     * @return byte[]
     */
    public static BCD bcdTime() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yy,MM,dd,HH,mm,ss");
        String text = fmt.format(LocalDateTime.now());
        byte[] b = new byte[6];
        String[] c = text.split(StringPool.COMMA);
        for (int i = 0; i < c.length; i++) {
            int dd = Integer.parseInt(c[i]);
            b[i] = (byte) ((dd / 10) * 16 + dd % 10);
        }
        return new BCD(b);
    }

    //	/**
//	 * bcdTime
//	 * @return byte[]
//	 */
//	public byte[] bcdTime(){
//		SimpleDateFormat df = new SimpleDateFormat("yy,MM,dd,HH,mm,ss");
//		String a = df.format(new Date());
//		byte[] b = new byte[6];
//		String[] c = a.split(",");
//		for (int i = 0; i < c.length; i++) {
//			int dd = Integer.parseInt(c[i]);
//			b[i] = (byte) ((dd/10)*16 + dd%10);
//		}
//		return b;
//	}
}
