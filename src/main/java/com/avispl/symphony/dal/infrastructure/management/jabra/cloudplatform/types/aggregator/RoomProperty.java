/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregator;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases.BaseProperty;

/**
 * Represents room properties of an aggregator device.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public enum RoomProperty implements BaseProperty {
	DATE_AND_TIME_CREATED("DateAndTimeCreated(UTC)"),
	GROUP_ID("GroupID"),
	LOCATION("Location"),
	ID("ID"),
	NAME("Name"),
	TYPE("Type"),
	STATUS("Status");

	private final String name;

	RoomProperty(String name) {
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
