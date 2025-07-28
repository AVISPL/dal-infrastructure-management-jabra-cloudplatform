/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.requests;

import java.util.Collections;
import java.util.Map;

/**
 * Request object representing the settings to be applied to a device. Used to send settings changes via the API.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public class SettingsRequest {
	private Map<String, OptionDetail> settings;

	public SettingsRequest(String settingField, String selectedValue) {
		this.settings = Collections.singletonMap(settingField, new OptionDetail(selectedValue));
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
		this.settings = settings;
	}
}
