/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.adapter;

import java.util.List;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases.BaseProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.constants.Constant;

/**
 * Enum representing different types of client filters.<br/>
 * Used to filter devices by client type in adapter properties.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public enum ClientTypeFilter implements BaseProperty {
	UNDEFINED("Undefined", Constant.EMPTY),
	MEETING_ROOM("MeetingRoom", "MeetingRoom"),
	PERSONAL("Personal", "Personal"),
	ALL("All", Constant.EMPTY);

	private final String name;
	private final String value;

	ClientTypeFilter(String name, String value) {
		this.name = name;
		this.value = value;
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

	/**
	 * Retrieves {@link #value}
	 *
	 * @return value of {@link #value}
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns a comma-separated string of all valid {@link ClientTypeFilter} names, excluding the {@link #UNDEFINED} value.
	 *
	 * @return a comma-separated string of valid client type names
	 */
	public static String getValues() {
		List<String> names = BaseProperty.getNames(ClientTypeFilter.class);
		names.removeIf(n -> UNDEFINED.getName().equals(n));

		return String.join(Constant.COMMA, names);
	}
}
