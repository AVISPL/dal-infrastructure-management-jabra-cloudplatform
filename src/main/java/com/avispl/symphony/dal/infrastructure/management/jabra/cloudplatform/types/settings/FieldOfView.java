/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases.BaseSetting;

/**
 * Represents the options of field of view for the device settings.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public enum FieldOfView implements BaseSetting {
	D90("90", "_90deg"),
	D120("120", "_120deg"),
	D140("140", "_140deg"),
	D180("180", "_180deg");

	private final String name;
	private final String value;

	FieldOfView(String name, String value) {
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
