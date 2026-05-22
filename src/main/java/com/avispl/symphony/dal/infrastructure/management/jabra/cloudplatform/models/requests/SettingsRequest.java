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
		private boolean requiresRestart;
		private boolean isLocked;
		private String value;

		public OptionDetail(String value, boolean requiresRestart) {
			this.isLocked = false;
			this.value = value;
			this.requiresRestart = requiresRestart;
		}

		/**
		 * Retrieves {@link #requiresRestart}
		 *
		 * @return boolean value of {@link #requiresRestart}
		 */
		public boolean requiresRestart() {
			return requiresRestart;
		}
		/**
		 * Retrieves {@link #value}
		 *
		 * @return boolean value of {@link #value}
		 */
		public boolean getIsOn() {
			return "1".equals(value);
		}

		/**
		 * Retrieves {@link #value}
		 *
		 * @return value of {@link #value}
		 */
		public String getSelected() {
			return value;
		}

		/**
		 * Retrieves {@link #value}
		 *
		 * @return int/string object value of {@link #value}
		 */
		public Object getValue() {
			try {
				return Integer.parseInt(this.value);
			} catch (NumberFormatException e) {
				try {
					return Math.round(Double.parseDouble(this.value));
				} catch (NumberFormatException e2) {
					return value;
				}
			}
		}

		/**
		 * Retrieves {@link #isLocked}
		 *
		 * @return value of {@link #isLocked}
		 */
		public boolean getIsLocked() {
			return isLocked;
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
	 * Retrieves the request body for {@link ApiConstant#DEVICE_SETTINGS_ENDPOINT}
	 *
	 * @return the request body(map) of {@link ApiConstant#DEVICE_SETTINGS_ENDPOINT}
	 */
	public Map<String, Map<String, OptionDetail>> getRequest() {
		return Collections.singletonMap(ApiConstant.SETTINGS_FIELD, this.settings);
	}
}
