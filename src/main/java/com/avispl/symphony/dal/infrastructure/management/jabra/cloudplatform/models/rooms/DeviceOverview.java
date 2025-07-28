/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.rooms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents an overview of the devices in the {@link Room}.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceOverview {
	private String id;
	private String name;
	private String deviceConnectionStatus;
	private String firmwareStatus;

	public DeviceOverview() {
		//	Default constructor required for JSON deserialization.
	}

	/**
	 * Retrieves {@link #id}
	 *
	 * @return value of {@link #id}
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets {@link #id} value
	 *
	 * @param id new value of {@link #id}
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets {@link #name} value
	 *
	 * @param name new value of {@link #name}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Retrieves {@link #deviceConnectionStatus}
	 *
	 * @return value of {@link #deviceConnectionStatus}
	 */
	public String getDeviceConnectionStatus() {
		return deviceConnectionStatus;
	}

	/**
	 * Sets {@link #deviceConnectionStatus} value
	 *
	 * @param deviceConnectionStatus new value of {@link #deviceConnectionStatus}
	 */
	public void setDeviceConnectionStatus(String deviceConnectionStatus) {
		this.deviceConnectionStatus = deviceConnectionStatus;
	}

	/**
	 * Retrieves {@link #firmwareStatus}
	 *
	 * @return value of {@link #firmwareStatus}
	 */
	public String getFirmwareStatus() {
		return firmwareStatus;
	}

	/**
	 * Sets {@link #firmwareStatus} value
	 *
	 * @param firmwareStatus new value of {@link #firmwareStatus}
	 */
	public void setFirmwareStatus(String firmwareStatus) {
		this.firmwareStatus = firmwareStatus;
	}
}
