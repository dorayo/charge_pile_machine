package com.huamar.charge.common.common.constant;

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
	 * pile.common.queue
	 * pile.common.queue_local
	 */
	String PILE_COMMON_QUEUE = "pile.common.biz.queue";

	/**
	 * 平台业务消息队列
	 */
	String PILE_PLATFORM_QUEUE = "pile.platform.queue";

	/**
	 * 锁时长
	 */
	Duration LOCK_TIMEOUT = Duration.ofSeconds(10);
}
