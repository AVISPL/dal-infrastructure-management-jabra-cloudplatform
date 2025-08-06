/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents device information.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Device {
	private String id;
	private String serialNumber;
	private String name;
	private Integer productId;
	private String variantType;
	private String productName;
	private String firmwareVersion;
	private Boolean firmwareUpdateInProgress;
	private String groupId;
	private String addedAt;
	private String lastSeenAt;
	@JsonProperty("isConnected")
	private Boolean isConnected;
	private String deviceConnectionStatus;
	private Computer computer;
	private JabraClient jabraClient;
	private String roomName;
	private String roomType;
	private String roomLocation;

	public Device() {
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
	 * Retrieves {@link #serialNumber}
	 *
	 * @return value of {@link #serialNumber}
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	/**
	 * Sets {@link #serialNumber} value
	 *
	 * @param serialNumber new value of {@link #serialNumber}
	 */
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
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
	 * Retrieves {@link #productId}
	 *
	 * @return value of {@link #productId}
	 */
	public Integer getProductId() {
		return productId;
	}

	/**
	 * Sets {@link #productId} value
	 *
	 * @param productId new value of {@link #productId}
	 */
	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	/**
	 * Retrieves {@link #variantType}
	 *
	 * @return value of {@link #variantType}
	 */
	public String getVariantType() {
		return variantType;
	}

	/**
	 * Sets {@link #variantType} value
	 *
	 * @param variantType new value of {@link #variantType}
	 */
	public void setVariantType(String variantType) {
		this.variantType = variantType;
	}

	/**
	 * Retrieves {@link #productName}
	 *
	 * @return value of {@link #productName}
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * Sets {@link #productName} value
	 *
	 * @param productName new value of {@link #productName}
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

	/**
	 * Retrieves {@link #firmwareVersion}
	 *
	 * @return value of {@link #firmwareVersion}
	 */
	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	/**
	 * Sets {@link #firmwareVersion} value
	 *
	 * @param firmwareVersion new value of {@link #firmwareVersion}
	 */
	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	/**
	 * Retrieves {@link #firmwareUpdateInProgress}
	 *
	 * @return value of {@link #firmwareUpdateInProgress}
	 */
	public Boolean getFirmwareUpdateInProgress() {
		return firmwareUpdateInProgress;
	}

	/**
	 * Sets {@link #firmwareUpdateInProgress} value
	 *
	 * @param firmwareUpdateInProgress new value of {@link #firmwareUpdateInProgress}
	 */
	public void setFirmwareUpdateInProgress(Boolean firmwareUpdateInProgress) {
		this.firmwareUpdateInProgress = firmwareUpdateInProgress;
	}

	/**
	 * Retrieves {@link #groupId}
	 *
	 * @return value of {@link #groupId}
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * Sets {@link #groupId} value
	 *
	 * @param groupId new value of {@link #groupId}
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * Retrieves {@link #addedAt}
	 *
	 * @return value of {@link #addedAt}
	 */
	public String getAddedAt() {
		return addedAt;
	}

	/**
	 * Sets {@link #addedAt} value
	 *
	 * @param addedAt new value of {@link #addedAt}
	 */
	public void setAddedAt(String addedAt) {
		this.addedAt = addedAt;
	}

	/**
	 * Retrieves {@link #lastSeenAt}
	 *
	 * @return value of {@link #lastSeenAt}
	 */
	public String getLastSeenAt() {
		return lastSeenAt;
	}

	/**
	 * Sets {@link #lastSeenAt} value
	 *
	 * @param lastSeenAt new value of {@link #lastSeenAt}
	 */
	public void setLastSeenAt(String lastSeenAt) {
		this.lastSeenAt = lastSeenAt;
	}

	/**
	 * Retrieves {@link #isConnected}
	 *
	 * @return value of {@link #isConnected}
	 */
	public Boolean getConnected() {
		return isConnected;
	}

	/**
	 * Sets {@link #isConnected} value
	 *
	 * @param connected new value of {@link #isConnected}
	 */
	public void setConnected(Boolean connected) {
		isConnected = connected;
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
	 * Retrieves {@link #computer}
	 *
	 * @return value of {@link #computer}
	 */
	public Computer getComputer() {
		return computer;
	}

	/**
	 * Sets {@link #computer} value
	 *
	 * @param computer new value of {@link #computer}
	 */
	public void setComputer(Computer computer) {
		this.computer = computer;
	}

	/**
	 * Retrieves {@link #jabraClient}
	 *
	 * @return value of {@link #jabraClient}
	 */
	public JabraClient getJabraClient() {
		return jabraClient;
	}

	/**
	 * Sets {@link #jabraClient} value
	 *
	 * @param jabraClient new value of {@link #jabraClient}
	 */
	public void setJabraClient(JabraClient jabraClient) {
		this.jabraClient = jabraClient;
	}

	/**
	 * Retrieves {@link #roomName}
	 *
	 * @return value of {@link #roomName}
	 */
	public String getRoomName() {
		return roomName;
	}

	/**
	 * Sets {@link #roomName} value
	 *
	 * @param roomName new value of {@link #roomName}
	 */
	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	/**
	 * Retrieves {@link #roomType}
	 *
	 * @return value of {@link #roomType}
	 */
	public String getRoomType() {
		return roomType;
	}

	/**
	 * Sets {@link #roomType} value
	 *
	 * @param roomType new value of {@link #roomType}
	 */
	public void setRoomType(String roomType) {
		this.roomType = roomType;
	}

	/**
	 * Retrieves {@link #roomLocation}
	 *
	 * @return value of {@link #roomLocation}
	 */
	public String getRoomLocation() {
		return roomLocation;
	}

	/**
	 * Sets {@link #roomLocation} value
	 *
	 * @param roomLocation new value of {@link #roomLocation}
	 */
	public void setRoomLocation(String roomLocation) {
		this.roomLocation = roomLocation;
	}
}
