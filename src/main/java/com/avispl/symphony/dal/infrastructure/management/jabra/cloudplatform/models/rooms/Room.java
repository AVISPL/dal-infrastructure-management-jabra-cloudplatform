/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.rooms;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a room information that groups multiple {@link DeviceOverview}.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Room {
	private String id;
	private String name;
	private List<DeviceOverview> devices;
	private String createdAt;
	@JsonProperty("roomType")
	private String type;
	private String locationName;
	private Integer seatCount;
	private String status;
	@JsonProperty("deviceGroupId")
	private String groupId;

	public Room() {
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
	 * Retrieves {@link #devices}
	 *
	 * @return value of {@link #devices}
	 */
	public List<DeviceOverview> getDevices() {
		return devices;
	}

	/**
	 * Sets {@link #devices} value
	 *
	 * @param devices new value of {@link #devices}
	 */
	public void setDevices(List<DeviceOverview> devices) {
		this.devices = devices;
	}

	/**
	 * Retrieves {@link #createdAt}
	 *
	 * @return value of {@link #createdAt}
	 */
	public String getCreatedAt() {
		return createdAt;
	}

	/**
	 * Sets {@link #createdAt} value
	 *
	 * @param createdAt new value of {@link #createdAt}
	 */
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Retrieves {@link #type}
	 *
	 * @return value of {@link #type}
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets {@link #type} value
	 *
	 * @param type new value of {@link #type}
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Retrieves {@link #locationName}
	 *
	 * @return value of {@link #locationName}
	 */
	public String getLocationName() {
		return locationName;
	}

	/**
	 * Sets {@link #locationName} value
	 *
	 * @param locationName new value of {@link #locationName}
	 */
	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	/**
	 * Retrieves {@link #seatCount}
	 *
	 * @return value of {@link #seatCount}
	 */
	public Integer getSeatCount() {
		return seatCount;
	}

	/**
	 * Sets {@link #seatCount} value
	 *
	 * @param seatCount new value of {@link #seatCount}
	 */
	public void setSeatCount(Integer seatCount) {
		this.seatCount = seatCount;
	}

	/**
	 * Retrieves {@link #status}
	 *
	 * @return value of {@link #status}
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets {@link #status} value
	 *
	 * @param status new value of {@link #status}
	 */
	public void setStatus(String status) {
		this.status = status;
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
}
