package com.huamar.charge.pile.common.constant;

import java.time.Duration;

/**
 * 队列常量配置
 * Date: 2023/07/24
 *
 * @author TiAmo(13721682347 @ 163.com)
 */
public interface QueueConstant {

	/**
	 * 正常状态
	 */
	String PILE_COMMON_QUEUE = "pile.common.queue";

	/**
	 * 锁时长
	 */
	Duration LOCK_TIMEOUT = Duration.ofSeconds(10);
}
