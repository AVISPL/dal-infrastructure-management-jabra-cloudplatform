/*
 * Copyright (c) 2026 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.webhook;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.avispl.symphony.api.dal.webhook.WebhookEvent;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.JabraCloudCommunicator;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Parser for the improvised Jabra Plus webhook payload, and digest() robustness. No network. */
class JabraWebhookEventTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void parsesStatusEvent() {
		JabraWebhookEvent e = JabraWebhookEvent.parse(mapper,
				"{\"eventType\":\"device.status.changed\",\"occurredAt\":\"2026-11-10T19:36:00Z\",\"device\":{\"id\":\"d-1\",\"serialNumber\":\"SN1\"},\"status\":\"Offline\"}");
		assertEquals(JabraWebhookEvent.Type.STATUS_CHANGED, e.getType());
		assertEquals("d-1", e.getDeviceId());
		assertEquals("SN1", e.getSerialNumber());
		assertEquals("offline", e.getStatus(), "status normalized to lower case");
		assertEquals(Instant.parse("2026-11-10T19:36:00Z"), e.getOccurredAt());
	}

	@Test
	void parsesFirmwareAndValidationEvents() {
		JabraWebhookEvent fw = JabraWebhookEvent.parse(mapper,
				"{\"eventType\":\"device.firmware.updated\",\"device\":{\"id\":\"d-1\"},\"firmwareVersion\":\"2.1.0\"}");
		assertEquals(JabraWebhookEvent.Type.FIRMWARE_UPDATED, fw.getType());
		assertEquals("2.1.0", fw.getFirmwareVersion());
		assertNull(fw.getOccurredAt());

		JabraWebhookEvent v = JabraWebhookEvent.parse(mapper, "{\"eventType\":\"webhook.validation\",\"validationUrl\":\"https://api.cloud.jabra.com/validate/x\"}");
		assertEquals(JabraWebhookEvent.Type.VALIDATION, v.getType());
		assertEquals("https://api.cloud.jabra.com/validate/x", v.getValidationUrl());
	}

	@Test
	void unknownTypeAndNonObjectBodies() {
		assertEquals(JabraWebhookEvent.Type.UNKNOWN, JabraWebhookEvent.parse(mapper, "{\"eventType\":\"room.created\"}").getType());
		assertThrows(IllegalArgumentException.class, () -> JabraWebhookEvent.parse(mapper, "not json"));
		assertThrows(IllegalArgumentException.class, () -> JabraWebhookEvent.parse(mapper, "[1,2]"));
		assertNull(JabraWebhookEvent.parse(mapper, "{\"eventType\":\"device.status.changed\",\"device\":{\"id\":{\"nested\":1}}}").getDeviceId(),
				"non-scalar where a scalar is expected: treated as absent");
	}

	@Test
	void digestNeverFailsTheBatchOnBadOrForeignEvents() {
		JabraCloudCommunicator adapter = new JabraCloudCommunicator();
		Date now = new Date();
		assertDoesNotThrow(() -> adapter.digest(Arrays.asList(
				new WebhookEvent(1, now, "garbage"),
				new WebhookEvent(2, now, "{\"eventType\":\"device.status.changed\",\"device\":{\"id\":\"not-ours\"},\"status\":\"offline\"}"),
				new WebhookEvent(3, now, "{\"eventType\":\"webhook.validation\",\"validationUrl\":\"http://169.254.169.254/latest/meta-data\"}"),
				new WebhookEvent(4, now, "{\"eventType\":\"webhook.validation\",\"validationUrl\":\"https://evil.example.com/jabra.com\"}"))));
		// redelivery of the same batch: skipped by sequence, still no exception
		assertDoesNotThrow(() -> adapter.digest(Arrays.asList(new WebhookEvent(1, now, "garbage"))));
	}
}
