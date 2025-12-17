/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases.BaseProperty;

/**
 * Represents optional general properties of an aggregated device.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public enum OptionalGeneralProperty implements BaseProperty {
	DEVICE_CONNECTION_STATUS("DeviceConnectionStatus"),
	ROOM_NAME("RoomName"),
	ROOM_TYPE("RoomType"),
	ROOM_LOCATION("RoomLocation");

	private final String name;

	OptionalGeneralProperty(String name) {
		this.name = name;
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
}
