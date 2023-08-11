package com.huamar.charge.pile;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 设置电价辅助类
 * 
 * @author wude
 * @version 1.0
 * @date 2018年1月3日
 */
public class SetElectrovalenceUtil {
	/** 小时单位 */
	public static final String HOUR = "时";
	/** 分钟单位 */
	public static final String MIN = "分";

	
	/**
	 * 根据时间,获取对应48个时间段的值
	 * @param times
	 * @param isEnd  标识  是否为结束时间
	 * @return
	 */
	public static int getIntervalVal(String times){
		//解决开始时间和结束时间重叠的问题
		String[] split = times.split(":",-1);
		
		// 取出小时
	    int startHour = Integer.valueOf(split[0]);
		// 取出分钟
		int startMin = Integer.valueOf(split[1]);
		
		
		
		int startLen = startHour * 2 + (startMin >= 30 ? 1 : 0);
		return startLen;
	}
	
	/**
	 * 获取需要替换区间的值
	 * @param start   开始值
	 * @param end     结束值
	 * @param electPrices   电价时间段值  0-3
	 * @return
	 */
	public static String getReplaceStr(int start,int end,int electPrices){
		StringBuilder str=new StringBuilder();
		//解决开始时间为00:00会解析异常
		if(start == 0){
			start = 1;
		}
		int len = end - start +1;
		for (int i = 0; i < len; i++) {
			str.append(electPrices);
		}
		return str.toString();
	}
	
	/**
	 * 获取ecTime
	 * @param startTime   电价开始时间
	 * @param endTime     电价结束时间
	 * @param ecTime      字符串
	 * @param electPrices 电价值 0-3
	 * @return
	 */
	public static StringBuilder getEcTime(String startTime,String endTime,StringBuilder ecTime,int electPrices){
		int start = getIntervalVal(startTime);
		int end = getIntervalVal(endTime);
		String replaceStr = getReplaceStr(start, end, electPrices);
		if(start == 0){
			start=1;
		}
		ecTime = ecTime.replace(start - 1, end, replaceStr);
		return ecTime;
	}

//	public static void main(String[] args) {
//		StringBuilder ecTime = new StringBuilder("000000000000000000000000000000000000000000000000");
//		ecTime = getEcTime("00:12", "00:45", ecTime,1);
//		System.out.println("替换后的值为:" + ecTime);
//		System.out.println("长度为:" + ecTime.length());
//
//		ecTime = getEcTime("01:00", "02:00", ecTime,1);
//		System.out.println("替换后的值为:" + ecTime);
//		System.out.println("长度为:" + ecTime.length());
//
//		ecTime = getEcTime("02:00", "03:00", ecTime,2);
//		System.out.println("替换后的值为:" + ecTime);
//		System.out.println("长度为:" + ecTime.length());
//
//		ecTime = getEcTime("12:00", "20:00", ecTime,5);
//		System.out.println("替换后的值为:" + ecTime);
//		System.out.println("长度为:" + ecTime.length());
//
//
//		BigDecimal decimal = new BigDecimal(1);
//
//		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;
//		System.out.println(formatter);
//		;
//		System.out.println(LocalTime.parse("00:00:00"));
//		System.out.println(LocalTime.parse("02:00:00"));
//	}
}
