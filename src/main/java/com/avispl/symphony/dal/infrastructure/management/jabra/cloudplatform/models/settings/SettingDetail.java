/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents the detail of a setting in the {@link Settings}.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SettingDetail {
	private String selected;

	public SettingDetail() {
		//	Default constructor required for JSON deserialization.
	}

	/**
	 * Retrieves {@link #selected}
	 *
	 * @return value of {@link #selected}
	 */
	public String getSelected() {
		return selected;
	}
}
