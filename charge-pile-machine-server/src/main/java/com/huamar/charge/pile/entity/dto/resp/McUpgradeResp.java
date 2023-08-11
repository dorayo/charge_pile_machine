package com.huamar.charge.pile.entity.dto.resp;

import com.huamar.charge.pile.common.BaseResp;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 远程升级响应
 * 2023/08/01
 *
 * @author TiAmo(13721682347@163.com)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class McUpgradeResp extends BaseResp {

	/**
	 * 类型
	 * URL类型
	 * 0x0001  终端升级程序
	 * 0x0002  配置文件下载
	 */
	public byte upgradeType;

	/**
	 * 版本号字节长度
	 */
	public short versionLength;

	/**
	 * 版本号
	 */
	public String version;

	/**
	 * 模式
	 * 0x00  主动模式
	 * 0x01  被动模式
	 */
	public byte mode;

	/**
	 * URL字节长度
	 */
	public short urlLength;

	/**
	 * URL 升级文件所在位置/下载文件所在位置，上传文件所在位置
	 */
	public String url;

	/**
	 * 用户名密码是否有效 0无效 1有效  默认为0
	 */
	public byte pwdStatus;

	/**
	 * 用户名长度
	 */
	public byte userNameLength;

	/**
	 * 用户名    用户名密码是否有效字段值为1时，此数据有效
	 */
	public String username;

	/**
	 * 用户密码长度
	 */
	public byte userPwdLength;

	/**
	 * 密码  用户名密码是否有效字段值为1时，此数据有效
	 */
	public String pwd;

	/**
	 * 校验码
	 */
	public int crc;

	/**
	 * 域名长度
	 */
	public short domainLength;

	/**
	 * 域名
	 */
	public String domain;

	/**
	 * 端口长度
	 */
	public short portLength;

	/**
	 * 端口
	 */
	public String port;

}
