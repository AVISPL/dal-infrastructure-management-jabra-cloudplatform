/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases;

import java.util.Arrays;

/**
 * Marker interface for property enums that define group attributes.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public interface BaseProperty {
	/**
	 * Returns the name of the property.
	 *
	 * @return the name of the property
	 */
	String getName();

	/**
	 * Returns an array of all property names defined in the specified enum class.
	 *
	 * @param enumClass the enum class
	 * @param <E> the type of the enum
	 * @return an array of property names
	 */
	static <E extends BaseProperty> String[] getNames(Class<E> enumClass) {
		return Arrays.stream(enumClass.getEnumConstants()).map(E::getName).toArray(String[]::new);
	}

	/**
	 * Returns the enum constant of the specified enum class that matches the given name.
	 *
	 * @param enumClass the enum class
	 * @param name the property name
	 * @param <E> the type of the enum
	 * @return the enum constant matching the name, or {@code null} if not found
	 */
	static <E extends BaseProperty> E getByName(Class<E> enumClass, String name) {
		E[] enums = enumClass.getEnumConstants();
		return Arrays.stream(enums).filter(e -> e.getName().equals(name)).findFirst().orElse(null);
	}
}
