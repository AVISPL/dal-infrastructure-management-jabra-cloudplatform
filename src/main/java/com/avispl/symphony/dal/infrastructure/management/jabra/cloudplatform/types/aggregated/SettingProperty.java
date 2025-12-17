/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases.BaseProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases.BaseSetting;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.AutomaticZoomMode;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.AutomaticZoomSpeed;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.DynamicComposition;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.FieldOfView;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.SafetyCapacityNotification;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.SettingsRevertToDefault;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.VideoStitching;
import com.avispl.symphony.dal.util.StringUtils;

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
	VIDEO_STITCHING("VideoStitching", VideoStitching.class, "videoStiching"),
	APPLY("ApplySettings", null, null),
	CANCEL("CancelSettings", null, null);

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

	public static SettingProperty getByApiField(String apiField) {
		return Arrays.stream(values()).filter(property -> property.apiField.equals(apiField)).findFirst().orElse(null);
	}

	/**
	 * Returns an enum set of supported SettingProperty from a supported device.
	 *
	 * @return an EnumSet containing all supported SettingProperty values
	 */
	public static Set<SettingProperty> getSupportedProperties() {
		return EnumSet.of(
				AUTOMATIC_ZOOM_MODE, AUTOMATIC_ZOOM_SPEED, DYNAMIC_COMPOSITION, FIELD_OF_VIEW,
				SAFETY_CAPACITY_NOTIFICATION, SETTINGS_REVERT_TO_DEFAULT, VIDEO_STITCHING
		);
	}

	/**
	 * Checks if a property name is supported.
	 *
	 * @param name the name of the property to check, may be null or empty
	 * @return {@code true} if the property name is supported; {@code false} otherwise
	 */
	public static boolean isSupportedProperty(String name) {
		if (StringUtils.isNullOrEmpty(name)) {
			return false;
		}
		return getSupportedProperties().stream().anyMatch(property -> property.getName().equals(name));
	}
}
