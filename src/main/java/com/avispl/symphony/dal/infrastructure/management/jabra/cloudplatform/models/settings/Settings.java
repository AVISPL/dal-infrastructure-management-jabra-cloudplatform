/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings;

import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a collection of device settings, where each field corresponds to a specific configurable setting option.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings {
	private SettingDetail automaticZoomMode;
	private SettingDetail automaticZoomSpeed;
	private SettingDetail dynamicComposition;
	private SettingDetail fieldOfView;
	private SettingDetail safetyCapacityNotification;
	private SettingDetail settingsRevertToDefault;
	@JsonProperty("videoStiching")
	private SettingDetail videoStitching;

	public Settings() {
		//	Default constructor required for JSON deserialization.
	}

	/**
	 * Retrieves {@link #automaticZoomMode}
	 *
	 * @return value of {@link #automaticZoomMode}
	 */
	public SettingDetail getAutomaticZoomMode() {
		return automaticZoomMode;
	}

	/**
	 * Sets {@link #automaticZoomMode} value
	 *
	 * @param automaticZoomMode new value of {@link #automaticZoomMode}
	 */
	public void setAutomaticZoomMode(SettingDetail automaticZoomMode) {
		this.automaticZoomMode = automaticZoomMode;
	}

	/**
	 * Retrieves {@link #automaticZoomSpeed}
	 *
	 * @return value of {@link #automaticZoomSpeed}
	 */
	public SettingDetail getAutomaticZoomSpeed() {
		return automaticZoomSpeed;
	}

	/**
	 * Sets {@link #automaticZoomSpeed} value
	 *
	 * @param automaticZoomSpeed new value of {@link #automaticZoomSpeed}
	 */
	public void setAutomaticZoomSpeed(SettingDetail automaticZoomSpeed) {
		this.automaticZoomSpeed = automaticZoomSpeed;
	}

	/**
	 * Retrieves {@link #dynamicComposition}
	 *
	 * @return value of {@link #dynamicComposition}
	 */
	public SettingDetail getDynamicComposition() {
		return dynamicComposition;
	}

	/**
	 * Sets {@link #dynamicComposition} value
	 *
	 * @param dynamicComposition new value of {@link #dynamicComposition}
	 */
	public void setDynamicComposition(SettingDetail dynamicComposition) {
		this.dynamicComposition = dynamicComposition;
	}

	/**
	 * Retrieves {@link #fieldOfView}
	 *
	 * @return value of {@link #fieldOfView}
	 */
	public SettingDetail getFieldOfView() {
		return fieldOfView;
	}

	/**
	 * Sets {@link #fieldOfView} value
	 *
	 * @param fieldOfView new value of {@link #fieldOfView}
	 */
	public void setFieldOfView(SettingDetail fieldOfView) {
		this.fieldOfView = fieldOfView;
	}

	/**
	 * Retrieves {@link #safetyCapacityNotification}
	 *
	 * @return value of {@link #safetyCapacityNotification}
	 */
	public SettingDetail getSafetyCapacityNotification() {
		return safetyCapacityNotification;
	}

	/**
	 * Sets {@link #safetyCapacityNotification} value
	 *
	 * @param safetyCapacityNotification new value of {@link #safetyCapacityNotification}
	 */
	public void setSafetyCapacityNotification(SettingDetail safetyCapacityNotification) {
		this.safetyCapacityNotification = safetyCapacityNotification;
	}

	/**
	 * Retrieves {@link #settingsRevertToDefault}
	 *
	 * @return value of {@link #settingsRevertToDefault}
	 */
	public SettingDetail getSettingsRevertToDefault() {
		return settingsRevertToDefault;
	}

	/**
	 * Sets {@link #settingsRevertToDefault} value
	 *
	 * @param settingsRevertToDefault new value of {@link #settingsRevertToDefault}
	 */
	public void setSettingsRevertToDefault(SettingDetail settingsRevertToDefault) {
		this.settingsRevertToDefault = settingsRevertToDefault;
	}

	/**
	 * Retrieves {@link #videoStitching}
	 *
	 * @return value of {@link #videoStitching}
	 */
	public SettingDetail getVideoStitching() {
		return videoStitching;
	}

	/**
	 * Sets {@link #videoStitching} value
	 *
	 * @param videoStitching new value of {@link #videoStitching}
	 */
	public void setVideoStitching(SettingDetail videoStitching) {
		this.videoStitching = videoStitching;
	}

	/**
	 * Checks whether all setting fields are null.
	 *
	 * @return {@code true} if all settings are null; otherwise, {@code false}.
	 */
	public boolean isNull() {
		return Stream.of(
				this.automaticZoomMode,
				this.automaticZoomSpeed,
				this.dynamicComposition,
				this.fieldOfView,
				this.safetyCapacityNotification,
				this.settingsRevertToDefault,
				this.videoStitching
		).allMatch(Objects::isNull);
	}
}
