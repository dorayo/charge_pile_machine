package com.huamar.charge.pile.entity.dto.command;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 云快冲 Charge price
 * date 2023/07/25
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class YKCChargePrice extends McBaseCommandDTO {

    /**
     * YKC 云快冲
     */
    private int jPrice = 0;
    private int fPrice = 0;
    private int pPrice = 0;
    private int gPrice = 0;

    /**
     * YKC 云快冲 服务费
     */
    private int jPriceS = 0;
    private int fPriceS = 0;
    private int pPriceS = 0;
    private int gPriceS = 0;

    //ykc 峰谷4时段电价
    private byte[] priceBucketJFPG = new byte[48];


    @SuppressWarnings("DuplicatedCode")
    public int getCharge(byte index) {
        switch (index) {

            case 1:
                return fPrice;

            case 2:
                return pPrice;

            case 3:
                return gPrice;

            default:
                return jPrice;
        }
    }


    @SuppressWarnings("DuplicatedCode")
    public int getChargeS(byte index) {
        switch (index) {

            case 1:
                return fPriceS;

            case 2:
                return pPriceS;

            case 3:
                return gPriceS;

            default:
                return jPriceS;
        }
    }


}
