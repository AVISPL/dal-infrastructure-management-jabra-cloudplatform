/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases.BaseSetting;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.constants.Constant;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.device.Computer;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.device.Device;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.device.JabraClient;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.rooms.Room;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.SettingDetail;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.Settings;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated.AGeneralProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated.ClientProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated.ComputerProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated.SettingProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregator.GeneralProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregator.RoomProperty;
import com.avispl.symphony.dal.util.StringUtils;

/**
 * Utility class for this adapter. This class includes helper methods to extract and convert properties.
 * <p>This class is non-instantiable and provides only static utility methods.</p>
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public class Util {
	private static final Log LOGGER = LogFactory.getLog(Util.class);

	private Util() {
		// Prevent instantiation
	}

	/**
	 * Maps a general aggregator property to its string representation.
	 * <p>Handles various value types. Returns {@link Constant#NOT_AVAILABLE} if the property is null or unsupported.</p>
	 *
	 * @param property          the {@link GeneralProperty} to map
	 * @param versionProperties the {@link Properties} object containing version-related data
	 * @return the string representation of the property, or {@link Constant#NOT_AVAILABLE} if unavailable
	 */
	public static String mapToGeneralProperty(GeneralProperty property, Properties versionProperties) {
		if (property == null) {
			return Constant.NOT_AVAILABLE;
		}

		switch (property) {
			case ADAPTER_BUILD_DATE:
			case ADAPTER_VERSION:
			case MONITORED_DEVICES_TOTAL:
				return mapToValue(versionProperties.getProperty(property.getProperty()));
			case ADAPTER_UPTIME:
				return mapToUptime(versionProperties.getProperty(property.getProperty()));
			case ADAPTER_UPTIME_MIN:
				return mapToUptimeMin(versionProperties.getProperty(property.getProperty()));
			case LAST_MONITORING_CYCLE_DURATION:
				return mapToMonitoringCycleDuration(versionProperties.getProperty(property.getProperty()));
			default:
				LOGGER.warn(String.format(Constant.UNSUPPORTED_MAP_PROPERTY_WARNING, "mapToGeneralProperty", property));
				return null;
		}
	}

	/**
	 * Maps a room property for the Aggregator device.
	 * <p>Handles formatting of dates, status-based values, and standard fields.
	 * Returns a default {@link Constant#NOT_AVAILABLE} value if the room is null or the property is unsupported.</p>
	 *
	 * @param property the {@link RoomProperty} to map
	 * @param room the {@link Room} object containing property values
	 * @return the string representation of the property, or {@link Constant#NOT_AVAILABLE} if unavailable
	 */
	public static String mapToRoomProperty(RoomProperty property, Room room) {
		if (room == null) {
			return Constant.NOT_AVAILABLE;
		}

		switch (property) {
			case DATE_AND_TIME_CREATED:
				return mapToReadableDateTime(room.getCreatedAt(), room.getTimeZoneId());
			case REBOOT_ALL_DEVICES:
				return room.getStatus().equals("Connected") ? Constant.NOT_AVAILABLE : "Reboot";
			case GROUP_ID:
				return mapToValue(room.getGroupId());
			case LOCATION:
				return mapToValue(room.getLocationName());
			case ID:
				return mapToValue(room.getId());
			case NAME:
				return mapToValue(room.getName());
			case TYPE:
				return mapToValue(room.getType());
			case STATUS:
				return mapToValue(room.getStatus());
			default:
				LOGGER.warn(String.format(Constant.UNSUPPORTED_MAP_PROPERTY_WARNING, "mapToRoomProperty", property));
				return Constant.NOT_AVAILABLE;
		}
	}

	/**
	 * Maps a general property for the Aggregated device.
	 * <p>Handles standard device fields. Returns {@code null} if the device is null or the property is unsupported.</p>
	 *
	 * @param property the {@link AGeneralProperty} to map
	 * @param device   the {@link Device} object containing property values
	 * @return the string representation of the property, or {@code null} if unavailable
	 */
	public static String mapToAGeneralProperty(AGeneralProperty property, Device device) {
		if (device == null) {
			return null;
		}

		switch (property) {
			case ADDED_AT:
				return mapToReadableDateTime(device.getAddedAt(), null);
			case FIRMWARE_UPDATE_IN_PROGRESS:
				return mapToValue(device.getFirmwareUpdateInProgress());
			case FIRMWARE_VERSION:
				return mapToValue(device.getFirmwareVersion());
			case GROUP_ID:
				return mapToValue(device.getGroupId());
			case LAST_SEEN_AT:
				return mapToReadableDateTime(device.getLastSeenAt(), null);
			case PRODUCT_ID:
				return mapToValue(device.getProductId());
			case PRODUCT_NAME:
				return mapToValue(device.getProductName());
			case VARIANT_TYPE:
				return mapToValue(device.getVariantType());
			case DEVICE_CONNECTION_STATUS:
				return mapToValue(device.getDeviceConnectionStatus());
			default:
				LOGGER.warn(String.format(Constant.UNSUPPORTED_MAP_PROPERTY_WARNING, "mapToAGeneralProperty", property));
				return null;
		}
	}

	/**
	 * Maps a computer property for the Aggregated device.
	 * <p>Handles standard computer fields. Returns {@code null} if the computer is null or the property is unsupported.</p>
	 *
	 * @param property the {@link ComputerProperty} to map
	 * @param computer the {@link Computer} object containing property values
	 * @return the string representation of the property, or {@code null} if unavailable
	 */
	public static String mapToAComputerProperty(ComputerProperty property, Computer computer) {
		if (computer == null) {
			return null;
		}

		switch (property) {
			case NAME:
				return mapToValue(computer.getComputerName());
			case IP_ADDRESS:
				return mapToValue(computer.getIpAddress());
			case MAC_ADDRESS:
				return mapToMacAddress(computer.getMacAddress());
			case OPERATING_SYSTEM:
				return mapToValue(computer.getOperatingSystem());
			case USERNAME:
				return mapToValue(computer.getUserName());
			default:
				LOGGER.warn(String.format(Constant.UNSUPPORTED_MAP_PROPERTY_WARNING, "mapToAComputerProperty", property));
				return null;
		}
	}

	/**
	 * Maps a client property for the Aggregated device.
	 * <p>Handles client fields. Returns {@code null} if the client is null or the property is unsupported.</p>
	 *
	 * @param property the {@link ClientProperty} to map
	 * @param client   the {@link JabraClient} object containing property values
	 * @return the string representation of the property, or {@code null} if unavailable
	 */
	public static String mapToAClientProperty(ClientProperty property, JabraClient client) {
		if (client == null) {
			return null;
		}

		switch (property) {
			case TYPE:
				return mapToValue(client.getClient());
			case NAME:
				return mapToValue(client.getClientName());
			case VERSION:
				return mapToValue(client.getClientVersion());
			default:
				LOGGER.warn(String.format(Constant.UNSUPPORTED_MAP_PROPERTY_WARNING, "mapToAClientProperty", property));
				return null;
		}
	}

	/**
	 * Maps a settings property for the Aggregated device.
	 * <p>Returns the display name of the selected value for each configurable setting.
	 * Returns {@code null} if the settings object is null or the property is unsupported.</p>
	 *
	 * @param property the {@link SettingProperty} to map
	 * @param settings the {@link Settings} object containing selected setting details
	 * @return the display name of the selected setting, or {@code null} if unavailable
	 */
	public static String mapToASettingsProperty(SettingProperty property, Settings settings) {
		if (settings == null) {
			return null;
		}

		switch (property) {
			case AUTOMATIC_ZOOM_MODE:
				return getSelectedSetting(settings.getAutomaticZoomMode(), property.getType());
			case AUTOMATIC_ZOOM_SPEED:
				return getSelectedSetting(settings.getAutomaticZoomSpeed(), property.getType());
			case DYNAMIC_COMPOSITION:
				return getSelectedSetting(settings.getDynamicComposition(), property.getType());
			case FIELD_OF_VIEW:
				return getSelectedSetting(settings.getFieldOfView(), property.getType());
			case SAFETY_CAPACITY_NOTIFICATION:
				return getSelectedSetting(settings.getSafetyCapacityNotification(), property.getType());
			case SETTINGS_REVERT_TO_DEFAULT:
				return getSelectedSetting(settings.getSettingsRevertToDefault(), property.getType());
			case VIDEO_STITCHING:
				return getSelectedSetting(settings.getVideoStitching(), property.getType());
			default:
				LOGGER.warn(String.format(Constant.UNSUPPORTED_MAP_PROPERTY_WARNING, "mapToASettingsValue", property));
				return null;
		}
	}

	/**
	 * Delays execution for a specified duration.
	 *
	 * @param milliseconds The duration in milliseconds.
	 */
	public static void delayExecution(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Converts the given value to a String:
	 * <ul>
	 *   <li>Returns the value itself if it is a non-null, non-empty String.</li>
	 *   <li>Returns String representation if value is Boolean or Integer.</li>
	 *   <li>Returns null otherwise.</li>
	 * </ul>
	 *
	 * @param value input value to convert
	 * @return String value or null
	 */
	private static String mapToValue(Object value) {
		if (value instanceof String) {
			String str = (String) value;
			return StringUtils.isNotNullOrEmpty(str) ? str : null;
		}
		if (value instanceof Boolean || value instanceof Integer) {
			return value.toString();
		}
		return null;
	}

	/**
	 * Retrieves the display name of the selected setting from the given detail.
	 *
	 * @param detail the setting detail containing the selected value
	 * @param clazz the class type of the setting, extending BaseSetting
	 * @param <T> the type of setting
	 * @return the name of the selected setting, or null if not found
	 */
	private static <T extends BaseSetting> String getSelectedSetting(SettingDetail detail, Class<T> clazz) {
		String selected = Optional.ofNullable(detail).map(SettingDetail::getSelected).orElse(null);
		T setting = BaseSetting.getByValue(clazz, selected);

		return Optional.ofNullable(setting).map(BaseSetting::getName).orElse(null);
	}

	/**
	 * Returns the elapsed uptime between the current system time and the given timestamp in milliseconds.
	 * <p>
	 * The input timestamp represents the start time in milliseconds (typically from {@link System#currentTimeMillis()}).
	 * The returned string represents the absolute duration in the format:
	 * "X day(s) Y hour(s) Z minute(s) W second(s)", omitting any zero-value units except seconds.
	 *
	 * @param uptime the start time in milliseconds as a string (e.g., "1717581000000")
	 * @return a formatted duration string like "2 day(s) 3 hour(s) 15 minute(s) 42 second(s)", or null if parsing fails
	 */
	private static String mapToUptime(String uptime) {
		try {
			if (StringUtils.isNullOrEmpty(uptime)) {
				return null;
			}

			long uptimeSecond = (System.currentTimeMillis() - Long.parseLong(uptime)) / 1000;
			long seconds = uptimeSecond % 60;
			long minutes = uptimeSecond % 3600 / 60;
			long hours = uptimeSecond % 86400 / 3600;
			long days = uptimeSecond / 86400;
			StringBuilder rs = new StringBuilder();
			if (days > 0) {
				rs.append(days).append(" day(s) ");
			}
			if (hours > 0) {
				rs.append(hours).append(" hour(s) ");
			}
			if (minutes > 0) {
				rs.append(minutes).append(" minute(s) ");
			}
			rs.append(seconds).append(" second(s)");

			return rs.toString().trim();
		} catch (Exception e) {
			LOGGER.error(Constant.MAP_TO_UPTIME_FAILED + uptime, e);
			return null;
		}
	}

	/**
	 * Returns the elapsed uptime in **whole minutes** between the current system time and the given timestamp in milliseconds.
	 * <p>
	 * The input timestamp represents the start time in milliseconds (typically from {@link System#currentTimeMillis()}).
	 * The returned string is the total number of minutes that have elapsed, excluding seconds.
	 *
	 * @param uptime the start time in milliseconds as a string (e.g., "1717581000000")
	 * @return a string representing the total number of elapsed minutes (e.g., "125"), or null if parsing fails
	 */
	private static String mapToUptimeMin(String uptime) {
		try {
			if (StringUtils.isNullOrEmpty(uptime)) {
				return null;
			}

			long uptimeSecond = (System.currentTimeMillis() - Long.parseLong(uptime)) / 1000;
			long minutes = uptimeSecond / 60;

			return String.valueOf(minutes);
		} catch (Exception e) {
			LOGGER.error(Constant.MAP_TO_UPTIME_MIN_FAILED + uptime, e);
			return null;
		}
	}

	/**
	 * Converts a duration in milliseconds to seconds.
	 * If >= 1000ms, returns integer seconds; otherwise, returns a decimal with 2 digits.
	 *
	 * @param value duration in milliseconds as string
	 * @return duration in seconds as string, or {@link Constant#NOT_AVAILABLE} if input is null or empty
	 */
	private static String mapToMonitoringCycleDuration(String value) {
		if (StringUtils.isNullOrEmpty(value)) {
			return Constant.NOT_AVAILABLE;
		}
		long duration = Long.parseLong(value);
		return duration == 0 || duration >= 1000
				? String.valueOf((int) (duration / 1000))
				: String.format("%.2f", Math.round(duration / 1000.0 * 100) / 100.0);
	}

	/**
	 * Converts an ISO datetime string to a readable format with the given time zone.
	 *
	 * @param datetime   ISO-8601 datetime string
	 * @param timeZoneId optional time zone ID
	 * @return formatted datetime string, or null if conversion fails
	 */
	private static String mapToReadableDateTime(String datetime, String timeZoneId) {
		try {
			ZoneId zoneId = ZoneId.of(Optional.ofNullable(timeZoneId).orElse(ZoneId.systemDefault().getId()));
			ZonedDateTime zonedDateTime = ZonedDateTime.parse(datetime).withZoneSameInstant(zoneId);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constant.READABLE_DATE_TIME_FORMAT, Locale.ENGLISH);

			return zonedDateTime.format(formatter);
		} catch (Exception e) {
			LOGGER.error(Constant.MAP_TO_READABLE_DATETIME_FAILED + datetime, e);
			return null;
		}
	}

	/**
	 * Formats a MAC address by inserting colons and converting to uppercase.
	 *
	 * @param macAddress raw MAC address string
	 * @return formatted MAC address, or null if input is null or empty
	 */
	private static String mapToMacAddress(String macAddress) {
		if (StringUtils.isNullOrEmpty(macAddress)) {
			return null;
		}
		return macAddress.replaceAll("(.{2})(?!$)", "$1:").toUpperCase();
	}
}
