/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.constants.Constant;

/**
 * Manages request timeout settings to control when a request can be executed again after a defined interval.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public class RequestTimeoutSetting {
	private final String name;
	private final long retrievalTimeout;
	private long validRetrievalTimestamp;

	public RequestTimeoutSetting(String name) {
		this.name = name;
		this.retrievalTimeout = Constant.DEFAULT_MS_TIMEOUT;
		this.validRetrievalTimestamp = System.currentTimeMillis();
	}

	public RequestTimeoutSetting(String name, long retrievalTimeout) {
		this.name = name;
		this.retrievalTimeout = Math.max(Constant.DEFAULT_MS_TIMEOUT, retrievalTimeout);
		this.validRetrievalTimestamp = System.currentTimeMillis() + this.retrievalTimeout;
	}

	/**
	 * Retrieves {@link #retrievalTimeout}
	 *
	 * @return value of {@link #retrievalTimeout}
	 */
	public long getRetrievalTimeout() {
		return retrievalTimeout;
	}

	/**
	 * Checks whether the request is valid to execute at the current time.
	 * <p>
	 * - If the current time is before {@code validRetrievalTimestamp}, returns {@code false}.<br>
	 * - If the current time is greater or equal, returns {@code true} and updates
	 *   {@code validRetrievalTimestamp} to {@code now + retrievalTimeout}.
	 *
	 * @return {@code true} if the request can be executed, {@code false} otherwise
	 */
	public boolean isValid() {
		boolean isValid = System.currentTimeMillis() >= this.validRetrievalTimestamp;
		if (isValid) {
			this.validRetrievalTimestamp = System.currentTimeMillis() + this.retrievalTimeout;
		}
		return isValid;
	}

	/**
	 * Returns a message describing how many seconds remain
	 * until the request becomes available again.
	 *
	 * @return formatted info string using {@link Constant#RETRIEVAL_AVAILABLE_INFO}
	 */
	public String getRetrievalAvailableInfo() {
		long seconds = Math.max((this.validRetrievalTimestamp - System.currentTimeMillis()) / 1000, 0);
		return String.format(Constant.RETRIEVAL_AVAILABLE_INFO, this.name, seconds);
	}
}
