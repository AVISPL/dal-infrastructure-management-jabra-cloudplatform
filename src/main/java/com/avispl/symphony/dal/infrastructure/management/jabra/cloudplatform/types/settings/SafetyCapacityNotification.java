/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases.BaseSetting;

/**
 * Represents the options of safety capacity notification for the device settings.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public enum SafetyCapacityNotification implements BaseSetting {
	ALWAYS("Always", "always"),
	WHEN_VIDEOS_IS_ENABLE("Only during a video conference", "whenVideoIsEnabled");

	private final String name;
	private final String value;

	SafetyCapacityNotification(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Retrieves {@link #value}
	 *
	 * @return value of {@link #value}
	 */
	@Override
	public String getValue() {
		return value;
	}
}
