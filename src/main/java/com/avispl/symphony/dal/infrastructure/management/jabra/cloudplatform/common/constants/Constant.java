/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.constants;


import java.time.Duration;

import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.dal.util.ControllablePropertyFactory;

/**
 * Utility class that defines constant values used across the application.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public class Constant {
	private Constant() {
		// Prevent instantiation
	}

	//	Formats
	public static final String GROUP_FORMAT = "%s_%02d";
	public static final String PROPERTY_FORMAT = "%s#%s";
	public static final String READABLE_DATE_TIME_FORMAT = "MMM d, yyyy, h:mm a";
	public static final String MAC_ADDRESS_REGEX = "^([0-9A-Fa-f]{2}([-:])){5}([0-9A-Fa-f]{2})$";

	//	Special characters
	public static final String COMMA = ",";
	public static final String HASH_SYMBOL = "#";
	public static final String UNDERSCORE = "_";
	public static final String EMPTY = "";

	//	Groups
	public static final String GENERAL_GROUP = "General";
	public static final String ROOM_GROUP = "JabraRoom";
	public static final String AGGREGATED_COMPUTER_GROUP = "Computer";
	public static final String AGGREGATED_CLIENT_GROUP = "JabraClient";
	public static final String AGGREGATED_SETTINGS_GROUP = "Settings";

	//	Values
	public static final String NOT_AVAILABLE = "N/A";
	public static final AdvancedControllableProperty DUMMY_CONTROLLER = ControllablePropertyFactory.createText(null, null);
	public static final long DEFAULT_INTERVAL_MS = Duration.ofSeconds(30).toMillis();

	//	Info messages
	public static final String INITIAL_INTERNAL_INFO = "Initialing internal state of instance: ";
	public static final String DESTROY_INTERNAL_INFO = "Destroying internal state of instance: ";

	//	Warning messages
	public static final String CONTROLLABLE_PROPS_EMPTY_WARNING = "ControllableProperties list is null or empty, skipping control operation";
	public static final String FETCHED_DATA_NULL_WARNING = "Fetched data is null. Endpoint: %s, ResponseClass: %s";
	public static final String LIST_EMPTY_WARNING = "The list of %s is empty, returning empty collection";
	public static final String OBJECT_EMPTY_WARNING = "The %s is null, returning empty collection";
	public static final String UNSUPPORTED_MAP_PROPERTY_WARNING = "Unsupported %s with property %s";
	public static final String STATISTICS_EMPTY_WARNING = "The statistics are empty, returning empty map.";

	//	Fail messages
	public static final String READ_PROPERTIES_FILE_FAILED = "Failed to load properties file: ";
	public static final String SET_CLIENT_TYPE_FAILED = "Invalid clientTypeFilter value. Possible values are: %s.";
	public static final String FETCH_DATA_FAILED = "Exception while fetching data. Endpoint: %s, ResponseClass: %s";
	public static final String LOGIN_FAILED = "Failed to login, please check the credentials";
	public static final String MAP_TO_UPTIME_FAILED = "Failed to mapToUptime with uptime: ";
	public static final String MAP_TO_UPTIME_MIN_FAILED = "Failed to mapToUptimeMin with uptime: ";
	public static final String MAP_TO_READABLE_DATETIME_FAILED = "Failed to mapToUptimeMin with datetime: ";
	public static final String REQUEST_APIS_FAILED = "Unable to process requested API sections: [%s], error reported: [%s]";
}
