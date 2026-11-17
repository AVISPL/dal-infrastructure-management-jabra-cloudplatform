/*
 * Copyright (c) 2026 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.webhook;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * One Jabra Plus webhook delivery, parsed.
 * <p>
 * <b>PoC payload — improvised.</b> Jabra publishes only the event categories (device status: online / offline / rebooted;
 * firmware: update completed) and the activation handshake (a validation event carrying {@code validationUrl}, to be
 * called with GET within 10 minutes); the payload schema itself is downloadable only from Jabra Plus Management. Until it
 * is in hand, this class assumes:
 *
 * <pre>
 * {"eventType":"webhook.validation","validationUrl":"https://...jabra.com/..."}
 * {"eventType":"device.status.changed","occurredAt":"2026-11-10T19:36:00Z","device":{"id":"...","serialNumber":"..."},"status":"online|offline|rebooted"}
 * {"eventType":"device.firmware.updated","occurredAt":"...","device":{"id":"...","serialNumber":"..."},"firmwareVersion":"1.2.3"}
 * </pre>
 *
 * Swap the field paths here when the real schema arrives; nothing else in the adapter depends on them.
 * <p>
 * Bodies are untrusted internet input: parsed as a plain JSON tree only (no type information, no binding to classes).
 */
public final class JabraWebhookEvent {

	public enum Type {
		VALIDATION, STATUS_CHANGED, FIRMWARE_UPDATED, UNKNOWN
	}

	private final Type type;
	private final String deviceId;
	private final String serialNumber;
	/** online | offline | rebooted (lower case), for {@link Type#STATUS_CHANGED} */
	private final String status;
	private final String firmwareVersion;
	private final String validationUrl;
	/** vendor event time, or {@code null} when absent / unparseable */
	private final Instant occurredAt;
	private final String rawType;

	private JabraWebhookEvent(Type type, String rawType, String deviceId, String serialNumber, String status, String firmwareVersion,
			String validationUrl, Instant occurredAt) {
		this.type = type;
		this.rawType = rawType;
		this.deviceId = deviceId;
		this.serialNumber = serialNumber;
		this.status = status;
		this.firmwareVersion = firmwareVersion;
		this.validationUrl = validationUrl;
		this.occurredAt = occurredAt;
	}

	/**
	 * @param mapper a plain mapper (default typing off)
	 * @param body raw delivery body
	 * @return the parsed event
	 * @throws IllegalArgumentException if the body is not a JSON object
	 */
	public static JabraWebhookEvent parse(ObjectMapper mapper, String body) {
		JsonNode root;
		try {
			root = mapper.readTree(body == null ? "" : body);
		} catch (Exception e) {
			throw new IllegalArgumentException("not JSON: " + e.getMessage(), e);
		}
		if (root == null || !root.isObject()) {
			throw new IllegalArgumentException("not a JSON object");
		}
		String rawType = text(root, "eventType");
		Type type;
		if (rawType == null) {
			type = Type.UNKNOWN;
		} else if (rawType.equalsIgnoreCase("webhook.validation") || root.hasNonNull("validationUrl")) {
			type = Type.VALIDATION;
		} else if (rawType.toLowerCase().startsWith("device.status")) {
			type = Type.STATUS_CHANGED;
		} else if (rawType.toLowerCase().startsWith("device.firmware")) {
			type = Type.FIRMWARE_UPDATED;
		} else {
			type = Type.UNKNOWN;
		}
		JsonNode device = root.path("device");
		String status = text(root, "status");
		return new JabraWebhookEvent(type, rawType, text(device, "id"), text(device, "serialNumber"), status == null ? null : status.toLowerCase(),
				text(root, "firmwareVersion"), text(root, "validationUrl"), instant(text(root, "occurredAt")));
	}

	private static String text(JsonNode node, String field) {
		JsonNode v = node == null ? null : node.get(field);
		return v == null || v.isNull() || !v.isValueNode() ? null : v.asText();
	}

	private static Instant instant(String iso) {
		if (iso == null) {
			return null;
		}
		try {
			return Instant.parse(iso);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	public Type getType() {
		return type;
	}

	public String getRawType() {
		return rawType;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public String getStatus() {
		return status;
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public String getValidationUrl() {
		return validationUrl;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}
}
