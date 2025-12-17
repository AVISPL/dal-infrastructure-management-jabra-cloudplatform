/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases.BaseProperty;

/**
 * Represents client properties of an aggregated device.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public enum ClientProperty implements BaseProperty {
	TYPE("Type"),
	NAME("Name"),
	VERSION("Version");

	private final String name;

	ClientProperty(String name) {
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
