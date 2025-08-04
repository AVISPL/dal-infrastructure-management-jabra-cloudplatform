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
public enum AGeneralProperty implements BaseProperty {
	ADDED_AT("AddedAt"),
	FIRMWARE_UPDATE_IN_PROGRESS("FirmwareUpdateInProgress"),
	FIRMWARE_VERSION("FirmwareVersion"),
	GROUP_ID("GroupID"),
	LAST_SEEN_AT("LastSeenAt"),
	PRODUCT_ID("ProductID"),
	PRODUCT_NAME("ProductName"),
	VARIANT_TYPE("VariantType");

	private final String name;

	AGeneralProperty(String name) {
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
