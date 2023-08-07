package com.huamar.charge.pile.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class PileDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	
    private Integer id;

    private String number;

    private Integer stationId;

    private Integer inType;

    private String version;

    private Integer outType;

    private Integer serviceType;

    private Integer cardType;

    private Integer openTime;

    private Double log;

    private Double lat;

    private String faultCode;

    private String ip;

    private Integer port;

    private Date joinTime;

    private Boolean isSetCharge;
    /** mac地址*/
    private String macAddress;
    /** 桩版本号 */
    private String pileVersion;

}