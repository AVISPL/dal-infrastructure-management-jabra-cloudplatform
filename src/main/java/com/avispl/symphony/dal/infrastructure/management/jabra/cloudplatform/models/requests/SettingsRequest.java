/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.requests;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.constants.ApiConstant;

/**
 * Request object representing the settings to be applied to a device. Used to send settings changes via the API.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public class SettingsRequest {
	private String deviceId;
	private long expiryTime;
	private Map<String, OptionDetail> settings;

	public SettingsRequest(String deviceId, long expiryTime, Map<String, OptionDetail> settings) {
		this.deviceId = deviceId;
		this.expiryTime = expiryTime;
		this.settings = new HashMap<>(settings);
	}

	/**
	 * Represents details of a setting option, including the selected value and lock status.
	 */
	public static class OptionDetail {
		private boolean isLocked;
		private String selected;

		public OptionDetail(String selected) {
			this.isLocked = false;
			this.selected = selected;
		}

		/**
		 * Retrieves {@link #isLocked}
		 *
		 * @return value of {@link #isLocked}
		 */
		public boolean isLocked() {
			return isLocked;
		}

		/**
		 * Sets {@link #isLocked} value
		 *
		 * @param locked new value of {@link #isLocked}
		 */
		public void setLocked(boolean locked) {
			isLocked = locked;
		}

		/**
		 * Retrieves {@link #selected}
		 *
		 * @return value of {@link #selected}
		 */
		public String getSelected() {
			return selected;
		}

		/**
		 * Sets {@link #selected} value
		 *
		 * @param selected new value of {@link #selected}
		 */
		public void setSelected(String selected) {
			this.selected = selected;
		}
	}

	/**
	 * Retrieves {@link #deviceId}
	 *
	 * @return value of {@link #deviceId}
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * Sets {@link #deviceId} value
	 *
	 * @param deviceId new value of {@link #deviceId}
	 */
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * Retrieves {@link #expiryTime}
	 *
	 * @return value of {@link #expiryTime}
	 */
	public long getExpiryTime() {
		return expiryTime;
	}

	/**
	 * Sets {@link #expiryTime} value
	 *
	 * @param expiryTime new value of {@link #expiryTime}
	 */
	public void setExpiryTime(long expiryTime) {
		this.expiryTime = expiryTime;
	}

	/**
	 * Retrieves {@link #settings}
	 *
	 * @return value of {@link #settings}
	 */
	public Map<String, OptionDetail> getSettings() {
		return settings;
	}

	/**
	 * Sets {@link #settings} value
	 *
	 * @param settings new value of {@link #settings}
	 */
	public void setSettings(Map<String, OptionDetail> settings) {
		this.settings = new HashMap<>(settings);
	}

	/**
	 * Retrieves the request body for {@link ApiConstant#PATCH_DEVICE_SETTINGS_ENDPOINT}
	 *
	 * @return the request body(map) of {@link ApiConstant#PATCH_DEVICE_SETTINGS_ENDPOINT}
	 */
	public Map<String, Map<String, OptionDetail>> getRequest() {
		return Collections.singletonMap(ApiConstant.SETTINGS_FIELD, this.settings);
	}
}
