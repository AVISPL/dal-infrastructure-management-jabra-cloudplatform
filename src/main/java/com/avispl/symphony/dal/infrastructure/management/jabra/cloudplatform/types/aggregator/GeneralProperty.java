/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregator;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases.BaseProperty;

/**
 * Represents general properties of an aggregator device.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public enum GeneralProperty implements BaseProperty {
	ADAPTER_BUILD_DATE("AdapterBuildDate", "adapter.build.date"),
	ADAPTER_UPTIME("AdapterUptime", "adapter.uptime"),
	ADAPTER_UPTIME_MIN("AdapterUptime(min)", "adapter.uptime"),
	ADAPTER_VERSION("AdapterVersion", "adapter.version"),
	LAST_MONITORING_CYCLE_DURATION("LastMonitoringCycleDuration(s)", "adapter.cycle.duration"),
	MONITORED_DEVICES_TOTAL("MonitoredDevicesTotal", "adapter.devices.total");

	private final String name;
	private final String property;

	GeneralProperty(String name, String property) {
		this.name = name;
		this.property = property;
	}

	/**
	 * Retrieves {@link #name}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves {@link #property}
	 *
	 * @return value of {@link #property}
	 */
	public String getProperty() {
		return property;
	}
}
