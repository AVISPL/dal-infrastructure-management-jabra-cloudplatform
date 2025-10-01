/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
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
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated.AggregatedGeneralProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated.ClientProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated.ComputerProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated.OptionalGeneralProperty;
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
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format(Constant.OBJECT_EMPTY_WARNING, "versionProperties"));
			}
			return null;
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
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(String.format(Constant.UNSUPPORTED_MAP_PROPERTY_WARNING, "mapToGeneralProperty", property));
				}
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
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format(Constant.OBJECT_EMPTY_WARNING, "room"));
			}
			return null;
		}

		switch (property) {
			case DATE_AND_TIME_CREATED:
				return mapToReadableDateTime(room.getCreatedAt());
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
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(String.format(Constant.UNSUPPORTED_MAP_PROPERTY_WARNING, "mapToRoomProperty", property));
				}
				return null;
		}
	}

	/**
	 * Maps a general property for the Aggregated device.
	 * <p>Handles standard device fields. Returns {@code null} if the device is null or the property is unsupported.</p>
	 *
	 * @param property the {@link AggregatedGeneralProperty} to map
	 * @param device   the {@link Device} object containing property values
	 * @return the string representation of the property, or {@code null} if unavailable
	 */
	public static String mapToAggregatedGeneralProperty(AggregatedGeneralProperty property, Device device) {
		if (device == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format(Constant.OBJECT_EMPTY_WARNING, "device"));
			}
			return null;
		}

		switch (property) {
			case ADDED_AT:
				return mapToReadableDateTime(device.getAddedAt());
			case FIRMWARE_UPDATE_IN_PROGRESS:
				return mapToValue(device.getFirmwareUpdateInProgress());
			case FIRMWARE_VERSION:
				return mapToValue(device.getFirmwareVersion());
			case GROUP_ID:
				return mapToValue(device.getGroupId());
			case LAST_SEEN_AT:
				return mapToReadableDateTime(device.getLastSeenAt());
			case PRODUCT_ID:
				return mapToValue(device.getProductId());
			case PRODUCT_NAME:
				return mapToValue(device.getProductName());
			case VARIANT_TYPE:
				return mapToValue(device.getVariantType());
			case IS_MEETING_DEVICE:
				return Optional.ofNullable(device.getJabraClient())
						.map(JabraClient::getClientType).map(type -> mapToValue(Constant.MEETING_ROOM.equalsIgnoreCase(type)))
						.orElse(null);
			default:
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(String.format(Constant.UNSUPPORTED_MAP_PROPERTY_WARNING, "mapToAggregatedGeneralProperty", property));
				}
				return null;
		}
	}

	/**
	 * Maps an optional general property for the supported aggregated {@link Device}.
	 * <p>Handles optional fields such as room data.
	 * Returns {@link Constant#NOT_AVAILABLE} if the device is {@code null} or the property is not supported.</p>
	 *
	 * @param property the {@link OptionalGeneralProperty} to map
	 * @param device the {@link Device} instance containing the property values
	 * @return a string representation of the optional property, or {@link Constant#NOT_AVAILABLE} if unavailable
	 */
	public static String mapToOptionalGeneralProperty(OptionalGeneralProperty property, Device device) {
		if (device == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format(Constant.OBJECT_EMPTY_WARNING, "device"));
			}
			return Constant.NOT_AVAILABLE;
		}

		switch (property) {
			case DEVICE_CONNECTION_STATUS:
				String connectionStatus = device.getDeviceConnectionStatus();
				return StringUtils.isNotNullOrEmpty(connectionStatus) ? connectionStatus : Constant.NOT_AVAILABLE;
			case ROOM_NAME:
				String roomName = device.getRoomName();
				return StringUtils.isNotNullOrEmpty(roomName) ? roomName : Constant.NOT_AVAILABLE;
			case ROOM_TYPE:
				String roomType = device.getRoomType();
				return StringUtils.isNotNullOrEmpty(roomType) ? roomType : Constant.NOT_AVAILABLE;
			case ROOM_LOCATION:
				String roomLocation = device.getRoomLocation();
				return StringUtils.isNotNullOrEmpty(roomLocation) ? roomLocation : Constant.NOT_AVAILABLE;
			default:
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(String.format(Constant.UNSUPPORTED_MAP_PROPERTY_WARNING, "mapToOptionalGeneralProperty", property));
				}
				return Constant.NOT_AVAILABLE;
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
	public static String mapToComputerProperty(ComputerProperty property, Computer computer) {
		if (computer == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format(Constant.OBJECT_EMPTY_WARNING, "computer"));
			}
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
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(String.format(Constant.UNSUPPORTED_MAP_PROPERTY_WARNING, "mapToComputerProperty", property));
				}
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
	public static String mapToClientProperty(ClientProperty property, JabraClient client) {
		if (client == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format(Constant.OBJECT_EMPTY_WARNING, "client"));
			}
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
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(String.format(Constant.UNSUPPORTED_MAP_PROPERTY_WARNING, "mapToClientProperty", property));
				}
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
	public static String mapToSettingsProperty(SettingProperty property, Settings settings) {
		if (settings == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format(Constant.OBJECT_EMPTY_WARNING, "settings"));
			}
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
			case APPLY:
			case CANCEL:
				return property.getName();
			default:
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(String.format(Constant.UNSUPPORTED_MAP_PROPERTY_WARNING, "mapToSettingsValue", property));
				}
				return null;
		}
	}

	/**
	 * Converts a nested settings map into a flat map of formatted setting names and their values.
	 * <p>
	 * Used specifically for unsupported devices.
	 * Determines the value based on the presence of keys: {@code isOn}, {@code selected}, or {@code value}.
	 * </p>
	 *
	 * @param settings the raw settings map
	 * @return a map of formatted setting names to their string values, or an empty map if input is null or empty
	 */
	public static Map<String, String> mapToSettingsProperties(Map<String, Map<String, Object>> settings) {
		if (MapUtils.isEmpty(settings)) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn(String.format(Constant.LIST_EMPTY_WARNING, "settings"));
			}
			return Collections.emptyMap();
		}

		return settings.entrySet().stream().map(s -> {
			String propertyName = String.format(Constant.PROPERTY_FORMAT, Constant.AGGREGATED_SETTINGS_GROUP, toTitleCase(s.getKey()));
			Map<String, Object> settingDetail = s.getValue();
			String value;
			if (settingDetail.containsKey("isOn")) {
				Object isOn = settingDetail.get("isOn");
				if (isOn == null) {
					value = Constant.NOT_AVAILABLE;
				} else if (Boolean.TRUE.equals(isOn)) {
					value = "On";
				} else {
					value = "Off";
				}
			} else if (settingDetail.containsKey("selected")) {
				value = String.valueOf(settingDetail.get("selected"));
			} else if (settingDetail.containsKey("value")) {
				value = String.valueOf(settingDetail.get("value"));
			} else {
				value = null;
			}
			if (StringUtils.isNullOrEmpty(value) || value.equals("null")) {
				value = Constant.NOT_AVAILABLE;
			} else {
				value = toTitleCase(value);
			}
			return new SimpleEntry<>(propertyName, value);
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
	 * Checks whether the given {@link Device} is supported based on its product ID.
	 * <p>This method compares the device's product ID with the predefined supported product ID</p>
	 *
	 * @param device the {@link Device} to check
	 * @return {@code true} if the device's product ID matches the supported ID, {@code false} otherwise
	 */
	public static boolean isSupportedDevice(Device device) {
		int supportedProductID = 12306;
		return Objects.equals(device.getProductId(), supportedProductID);
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
	public static String mapToValue(Object value) {
		if (value instanceof String) {
			String str = (String) value;
			if (str.equals("true") || str.equals("false")) {
				return str;
			}
			return StringUtils.isNotNullOrEmpty(str) ? toTitleCase(str) : null;
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
	 * @return formatted datetime string, or null if conversion fails
	 */
	private static String mapToReadableDateTime(String datetime) {
		try {
			if (StringUtils.isNullOrEmpty(datetime)) {
				return null;
			}
			ZonedDateTime zonedDateTime = ZonedDateTime.parse(datetime);
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
		if (macAddress.matches(Constant.MAC_ADDRESS_REGEX)) {
			return macAddress;
		}
		return macAddress.replaceAll("(.{2})(?!$)", "$1:").toUpperCase();
	}

	/**
	 * Capitalizes the first character of the input string.
	 * <p>
	 * If the input is {@code null}, empty, or the literal string {@code "null"}, this method returns {@code null}.
	 * If the input is {@code "true"} or {@code "false"}, the method returns the input unchanged.
	 * Otherwise, it returns the input string with its first character converted to uppercase.
	 * </p>
	 *
	 * @param value the input string to convert
	 * @return a string with the first character capitalized, or {@code null} if the input is invalid
	 */
	private static String toTitleCase(String value) {
		if (StringUtils.isNullOrEmpty(value) || value.equals("null")) {
			return null;
		}
		if (value.equals("true") || value.equals("false")) {
			return value;
		}

		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}
}
