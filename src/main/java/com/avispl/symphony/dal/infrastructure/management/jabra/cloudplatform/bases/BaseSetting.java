/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases;

import java.util.Arrays;

/**
 * Base interface for setting enums that provide both name and value.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public interface BaseSetting extends BaseProperty {
	/**
	 * Returns the value of the setting.
	 *
	 * @return the setting value
	 */
	String getValue();


	/**
	 * Retrieves the enum constant of the specified enum class that matches the given value.
	 *
	 * @param enumClass the enum class
	 * @param value     the value to match
	 * @param <E>       the type of the enum
	 * @return the matching enum constant, or {@code null} if no match is found
	 */
	static <E extends BaseSetting> E getByValue(Class<E> enumClass, String value) {
		if (enumClass == null || value == null) {
			return null;
		}
		E[] enums = enumClass.getEnumConstants();
		return Arrays.stream(enums).filter(e -> e.getValue().equals(value)).findFirst().orElse(null);
	}
}
