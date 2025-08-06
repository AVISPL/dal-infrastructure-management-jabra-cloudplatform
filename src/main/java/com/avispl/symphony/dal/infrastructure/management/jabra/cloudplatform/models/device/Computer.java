/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents computer information of the {@link Device}.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Computer {
	private String computerName;
	private String operatingSystem;
	private String userName;
	private String ipAddress;
	private String macAddress;

	public Computer() {
		//	Default constructor required for JSON deserialization.
	}

	/**
	 * Retrieves {@link #computerName}
	 *
	 * @return value of {@link #computerName}
	 */
	public String getComputerName() {
		return computerName;
	}

	/**
	 * Sets {@link #computerName} value
	 *
	 * @param computerName new value of {@link #computerName}
	 */
	public void setComputerName(String computerName) {
		this.computerName = computerName;
	}

	/**
	 * Retrieves {@link #operatingSystem}
	 *
	 * @return value of {@link #operatingSystem}
	 */
	public String getOperatingSystem() {
		return operatingSystem;
	}

	/**
	 * Sets {@link #operatingSystem} value
	 *
	 * @param operatingSystem new value of {@link #operatingSystem}
	 */
	public void setOperatingSystem(String operatingSystem) {
		this.operatingSystem = operatingSystem;
	}

	/**
	 * Retrieves {@link #userName}
	 *
	 * @return value of {@link #userName}
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets {@link #userName} value
	 *
	 * @param userName new value of {@link #userName}
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Retrieves {@link #ipAddress}
	 *
	 * @return value of {@link #ipAddress}
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * Sets {@link #ipAddress} value
	 *
	 * @param ipAddress new value of {@link #ipAddress}
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * Retrieves {@link #macAddress}
	 *
	 * @return value of {@link #macAddress}
	 */
	public String getMacAddress() {
		return macAddress;
	}

	/**
	 * Sets {@link #macAddress} value
	 *
	 * @param macAddress new value of {@link #macAddress}
	 */
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
}
