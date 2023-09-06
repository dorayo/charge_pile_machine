package com.huamar.charge.pile.enums;

import java.time.Duration;

/**
 * 缓存Key配置
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
public interface CacheEnum {

	/**
	 * 锁时长
	 */
	Duration MESSAGE_LOCK_TIMEOUT = Duration.ofSeconds(10);

	/**
	 *
	 */
	String MESSAGE_LOCK_KEY = "mq.pile.lock:";
}
