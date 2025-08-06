/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases.BaseProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases.BaseSetting;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.AutomaticZoomMode;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.AutomaticZoomSpeed;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.DynamicComposition;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.FieldOfView;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.SafetyCapacityNotification;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.SettingsRevertToDefault;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.VideoStitching;

/**
 * Represents setting properties of an aggregated device.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public enum SettingProperty implements BaseProperty {
	AUTOMATIC_ZOOM_MODE("AutomaticZoomMode", AutomaticZoomMode.class, "automaticZoomMode"),
	AUTOMATIC_ZOOM_SPEED("AutomaticZoomSpeed", AutomaticZoomSpeed.class, "automaticZoomSpeed"),
	DYNAMIC_COMPOSITION("DynamicComposition", DynamicComposition.class, "dynamicComposition"),
	FIELD_OF_VIEW("FieldOfView(deg)", FieldOfView.class, "fieldOfView"),
	SAFETY_CAPACITY_NOTIFICATION("SafetyCapacityNotification", SafetyCapacityNotification.class, "safetyCapacityNotification"),
	SETTINGS_REVERT_TO_DEFAULT("SettingsRevertToDefault", SettingsRevertToDefault.class, "settingsRevertToDefault"),
	VIDEO_STITCHING("VideoStitching", VideoStitching.class, "videoStiching");

	private final String name;
	private final Class<? extends BaseSetting> type;
	private final String apiField;

	SettingProperty(String name, Class<? extends BaseSetting> type, String apiField) {
		this.name = name;
		this.type = type;
		this.apiField = apiField;
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
	 * Retrieves {@link #type}
	 *
	 * @return value of {@link #type}
	 */
	public Class<? extends BaseSetting> getType() {
		return type;
	}

	/**
	 * Retrieves {@link #apiField}
	 *
	 * @return value of {@link #apiField}
	 */
	public String getApiField() {
		return apiField;
	}
}
