package com.huamar.charge.pile.api.dto;

import lombok.Data;

import java.io.Serializable;


/**
 * 充电桩信息
 * 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@Data
public class PileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Integer id;
    /**
     * 充电站
     */
    private Integer stationId;
    /**
     * 充电桩编码
     */
    private String pileCode;
    /**
     * 枪数量
     */
    private Integer gunNum;
    /**
     * 状态
     */
    private Integer pileStatus;
    /**
     * 桩类型
     */
    private Integer pileType;
    /**
     * 电流类型
     */
    private String electricType;
    /**
     * 输出端类型
     */
    private Integer outType;
    /**
     * 服务类型
     */
    private Integer serviceType;
    /**
     * 刷卡类型
     */
    private Integer cardType;
    /**
     * 充电桩版本
     */
    private String pileVersion;
    /**
     * 充电桩型号
     */
    private String version;
    /**
     * mac地址
     */
    private String macAddress;
    /**
     * 物联网卡号
     */
    private String lotCard;
    /**
     * 额定电压
     */
    private String ratedV;
    /**
     * 额定功率
     */
    private String ratedW;
    /**
     * 生产厂家
     */
    private String vender;
    /**
     * 通讯协议
     */
    private String communicationProtocol;
    /**
     * 背光灯
     */
    private String backlight;

}