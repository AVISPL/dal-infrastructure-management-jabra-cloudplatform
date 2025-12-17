/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases.BaseProperty;

/**
 * Represents general properties of an aggregated device.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public enum AggregatedGeneralProperty implements BaseProperty {
	ADDED_AT("AddedAt(UTC)"),
	FIRMWARE_UPDATE_IN_PROGRESS("FirmwareUpdateInProgress"),
	FIRMWARE_VERSION("FirmwareVersion"),
	GROUP_ID("GroupID"),
	LAST_SEEN_AT("LastSeenAt(UTC)"),
	PRODUCT_ID("ProductID"),
	PRODUCT_NAME("ProductName"),
	VARIANT_TYPE("VariantType"),
	IS_MEETING_DEVICE("IsMeetingDevice");

	private final String name;

	AggregatedGeneralProperty(String name) {
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
