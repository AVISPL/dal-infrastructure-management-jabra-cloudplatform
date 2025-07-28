/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.constants;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.device.Device;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.Settings;

/**
 * Utility class that defines API endpoint paths and URI patterns for REST controllers.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public class ApiConstant {
	private ApiConstant() {
		// Prevent instantiation
	}

	//	API variables
	public static final String API_KEY_HEADER = "Ocp-Apim-Subscription-Key";
	public static final String API_VERSION_HEADER = "api-version";
	public static final String DEVICE_ID_PARAM = "{deviceId}";
	public static final String GROUP_ID_PARAM = "{groupId}";
	public static final String ROOM_ID_PARAM = "{roomId}";
	public static final String ITEMS_FIELD = "items";
	public static final String SETTINGS_FIELD = "settings";

	//	API types
	public static final TypeReference<Settings> SETTINGS_RES_TYPE = new TypeReference<Settings>() {
	};
	public static final TypeReference<List<Device>> DEVICES_RES_TYPE = new TypeReference<List<Device>>() {
	};

	//	Endpoints
	public static final String GET_DEVICES_ENDPOINT = "devices/api/devices";
	public static final String GET_DEVICE_SETTINGS_ENDPOINT = "assetmanagement/api/api-device/" + DEVICE_ID_PARAM + "/settings";
	public static final String PATCH_DEVICE_SETTINGS_ENDPOINT = "assetmanagement/api/api-device/" + DEVICE_ID_PARAM + "/settings";
	public static final String GET_ROOMS_ENDPOINT = "meetingroomassetmanagement/api/rooms/" + GROUP_ID_PARAM;
	public static final String POST_REBOOT_DEVICES_ENDPOINT = "meetingroomassetmanagement/api/rooms/" + ROOM_ID_PARAM + "/reboot";
}
