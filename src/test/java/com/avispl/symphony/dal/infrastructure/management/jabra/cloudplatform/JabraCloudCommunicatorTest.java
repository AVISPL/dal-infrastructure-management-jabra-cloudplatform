/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.Util;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.constants.Constant;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated.SettingProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.SafetyCapacityNotification;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.SettingsRevertToDefault;


/**
 * Unit tests for the {@link JabraCloudCommunicator} class.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
class JabraCloudCommunicatorTest {
	private ExtendedStatistics extendedStatistics;
	private JabraCloudCommunicator jabraCloudCommunicator;

	@BeforeEach
	void setUp() throws Exception {
		this.jabraCloudCommunicator = new JabraCloudCommunicator();
		this.jabraCloudCommunicator.setHost("");
		this.jabraCloudCommunicator.setPort(443);
		this.jabraCloudCommunicator.setLogin("");
		this.jabraCloudCommunicator.setPassword("");
		this.jabraCloudCommunicator.init();
		this.jabraCloudCommunicator.connect();
	}

	@AfterEach
	void destroy() throws Exception {
		this.jabraCloudCommunicator.disconnect();
		this.jabraCloudCommunicator.destroy();
	}

	@Test
	void testGetMultipleStatistics() throws Exception {
		this.extendedStatistics = (ExtendedStatistics) this.jabraCloudCommunicator.getMultipleStatistics().get(0);
		Map<String, String> statistics = this.extendedStatistics.getStatistics();
		List<AdvancedControllableProperty> controllableProperties = this.extendedStatistics.getControllableProperties();

		this.verifyStatistics(statistics);
		controllableProperties.forEach(Assertions::assertNotNull);
	}

	@Test
	void testRetrieveMultipleStatistics() throws Exception {
		this.extendedStatistics = (ExtendedStatistics) this.jabraCloudCommunicator.getMultipleStatistics().get(0);
		this.jabraCloudCommunicator.retrieveMultipleStatistics();
		Util.delayExecution(10000);
		this.extendedStatistics = (ExtendedStatistics) this.jabraCloudCommunicator.getMultipleStatistics().get(0);
		this.jabraCloudCommunicator.retrieveMultipleStatistics();
		Util.delayExecution(10000);
		this.extendedStatistics = (ExtendedStatistics) this.jabraCloudCommunicator.getMultipleStatistics().get(0);
		List<AggregatedDevice> aggregatedDevices = this.jabraCloudCommunicator.retrieveMultipleStatistics();

		aggregatedDevices.forEach(aggregatedDevice -> {
			List<AdvancedControllableProperty> controllableProperties = aggregatedDevice.getControllableProperties();

			this.verifyStatistics(aggregatedDevice.getProperties());
			controllableProperties.forEach(Assertions::assertNotNull);
		});
	}

	@Test
	void testApplySettingsToDeviceControl() throws Exception {
		//	Set up the statistics
		this.jabraCloudCommunicator.getMultipleStatistics();
		this.jabraCloudCommunicator.retrieveMultipleStatistics();
		Util.delayExecution(10000);
		String deviceId = "DD9ECD6296804500D744A03C47526EA8B7C0A1C68";

		//	Testing the changes settings
		ControllableProperty settingProperty1 = new ControllableProperty();
		settingProperty1.setProperty(String.format(Constant.PROPERTY_FORMAT, Constant.AGGREGATED_SETTINGS_GROUP, SettingProperty.SETTINGS_REVERT_TO_DEFAULT.getName()));
		settingProperty1.setValue(SettingsRevertToDefault.PC_UNPLUG.getName());
		settingProperty1.setDeviceId(deviceId);
		this.jabraCloudCommunicator.controlProperty(settingProperty1);
		ControllableProperty settingProperty2 = new ControllableProperty();
		settingProperty2.setProperty(String.format(Constant.PROPERTY_FORMAT, Constant.AGGREGATED_SETTINGS_GROUP, SettingProperty.SAFETY_CAPACITY_NOTIFICATION.getName()));
		settingProperty2.setValue(SafetyCapacityNotification.WHEN_VIDEOS_IS_ENABLE.getName());
		settingProperty2.setDeviceId(deviceId);
		this.jabraCloudCommunicator.controlProperty(settingProperty2);

		Map<String, String> properties = this.jabraCloudCommunicator.retrieveMultipleStatistics().stream()
				.filter(aggregatedDevice -> aggregatedDevice.getDeviceId().equals(deviceId)).findFirst()
				.map(AggregatedDevice::getProperties)
				.map(p -> {
					Map<String, String> m = new HashMap<>();
					if (p.containsKey(settingProperty1.getProperty())) {
						m.put(settingProperty1.getProperty(), p.get(settingProperty1.getProperty()));
					}
					if (p.containsKey(settingProperty2.getProperty())) {
						m.put(settingProperty2.getProperty(), p.get(settingProperty2.getProperty()));
					}
					return m;
				}).orElseGet(Collections::emptyMap);
		this.verifyStatistics(properties);

		//	Apply the changes with settings
		ControllableProperty changesProperty = new ControllableProperty();
		changesProperty.setProperty("Settings#" + SettingProperty.APPLY.getName());
		changesProperty.setValue(Constant.NOT_AVAILABLE);
		changesProperty.setDeviceId(deviceId);
		this.jabraCloudCommunicator.controlProperty(changesProperty);
	}

	private void verifyStatistics(Map<String, String> statistics) {
		Map<String, Map<String, String>> groups = new LinkedHashMap<>();
		groups.put(Constant.GENERAL_GROUP, this.filterGroupStatistics(statistics, null));
		groups.put(Constant.ROOM_GROUP, this.filterGroupStatistics(statistics, Constant.ROOM_GROUP));
		groups.put(Constant.AGGREGATED_COMPUTER_GROUP, this.filterGroupStatistics(statistics, Constant.AGGREGATED_COMPUTER_GROUP));
		groups.put(Constant.AGGREGATED_CLIENT_GROUP, this.filterGroupStatistics(statistics, Constant.AGGREGATED_CLIENT_GROUP));
		groups.put(Constant.AGGREGATED_SETTINGS_GROUP, this.filterGroupStatistics(statistics, Constant.AGGREGATED_SETTINGS_GROUP));

		for (Map<String, String> initGroup : groups.values()) {
			for (Map.Entry<String, String> initStatistics : initGroup.entrySet()) {
				Assertions.assertNotNull(initStatistics.getValue(), "Value is null with property: " + initStatistics.getKey());
			}
		}
	}

	private Map<String, String> filterGroupStatistics(Map<String, String> statistics, String groupName) {
		return statistics.entrySet().stream()
				.filter(e -> {
					if (groupName == null) {
						return !e.getKey().contains("#");
					}
					return e.getKey().startsWith(groupName);
				})
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
