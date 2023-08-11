package com.huamar.charge.pile;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 设置电价(桩---->后台)
 * 
 * @author wude
 * @version 1.0
 * @date 2018年1月11日
 */
public class SetElectrovalenceVo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** 电价1 */
	private BigDecimal electricCharge1;
	/** 电价2 */
	private BigDecimal electricCharge2;
	/** 电价3 */
	private BigDecimal electricCharge3;
	/** 电价4 */
	private BigDecimal electricCharge4;
	/** 电价5 */
	private BigDecimal electricCharge5;
	/** 电价6 */
	private BigDecimal electricCharge6;
	/** 服务费1 */
	private BigDecimal serviceCharge1;
	/** 服务费2 */
	private BigDecimal serviceCharge2;
	/** 服务费3 */
	private BigDecimal serviceCharge3;
	/** 服务费4 */
	private BigDecimal serviceCharge4;
	/** 服务费5 */
	private BigDecimal serviceCharge5;
	/** 服务费6 */
	private BigDecimal serviceCharge6;
	/**费率时间段*/
	private String electricTime;
	/** 服务费  0.01元*/
	private Integer serviceMoney;
	public BigDecimal getElectricCharge1() {
		return electricCharge1;
	}
	public void setElectricCharge1(BigDecimal electricCharge1) {
		this.electricCharge1 = electricCharge1;
	}
	public BigDecimal getElectricCharge2() {
		return electricCharge2;
	}
	public void setElectricCharge2(BigDecimal electricCharge2) {
		this.electricCharge2 = electricCharge2;
	}
	public BigDecimal getElectricCharge3() {
		return electricCharge3;
	}
	public void setElectricCharge3(BigDecimal electricCharge3) {
		this.electricCharge3 = electricCharge3;
	}
	public BigDecimal getElectricCharge4() {
		return electricCharge4;
	}
	public void setElectricCharge4(BigDecimal electricCharge4) {
		this.electricCharge4 = electricCharge4;
	}
	public String getElectricTime() {
		return electricTime;
	}
	public void setElectricTime(String electricTime) {
		this.electricTime = electricTime;
	}
	public Integer getServiceMoney() {
		return serviceMoney;
	}
	public void setServiceMoney(Integer serviceMoney) {
		this.serviceMoney = serviceMoney;
	}
	public BigDecimal getElectricCharge5() {
		return electricCharge5;
	}
	public void setElectricCharge5(BigDecimal electricCharge5) {
		this.electricCharge5 = electricCharge5;
	}
	public BigDecimal getElectricCharge6() {
		return electricCharge6;
	}
	public void setElectricCharge6(BigDecimal electricCharge6) {
		this.electricCharge6 = electricCharge6;
	}
	public BigDecimal getServiceCharge1() {
		return serviceCharge1;
	}
	public void setServiceCharge1(BigDecimal serviceCharge1) {
		this.serviceCharge1 = serviceCharge1;
	}
	public BigDecimal getServiceCharge2() {
		return serviceCharge2;
	}
	public void setServiceCharge2(BigDecimal serviceCharge2) {
		this.serviceCharge2 = serviceCharge2;
	}
	public BigDecimal getServiceCharge3() {
		return serviceCharge3;
	}
	public void setServiceCharge3(BigDecimal serviceCharge3) {
		this.serviceCharge3 = serviceCharge3;
	}
	public BigDecimal getServiceCharge4() {
		return serviceCharge4;
	}
	public void setServiceCharge4(BigDecimal serviceCharge4) {
		this.serviceCharge4 = serviceCharge4;
	}
	public BigDecimal getServiceCharge5() {
		return serviceCharge5;
	}
	public void setServiceCharge5(BigDecimal serviceCharge5) {
		this.serviceCharge5 = serviceCharge5;
	}
	public BigDecimal getServiceCharge6() {
		return serviceCharge6;
	}
	public void setServiceCharge6(BigDecimal serviceCharge6) {
		this.serviceCharge6 = serviceCharge6;
	}
}
