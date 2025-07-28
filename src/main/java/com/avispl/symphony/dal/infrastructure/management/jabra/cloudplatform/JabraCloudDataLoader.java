/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.avispl.symphony.api.dal.error.ResourceNotReachableException;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.Util;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.constants.ApiConstant;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.device.Device;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.Settings;

/**
 * This class implements a data loader that periodically collects settings data
 * from a list of Jabra devices via the {@link JabraCloudCommunicator}.
 * This class is thread-safe with the use of {@code volatile} for key flags.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public class JabraCloudDataLoader implements Runnable {
	private static final long POLLING_CYCLE_INTERVAL = 60000L;
	/**
	 * Timeout duration (in milliseconds) for retrieving statistics.
	 */
	private static final long RETRIEVE_STATISTICS_TIMEOUT = 3 * 60 * 1000L;

	private final Log logger = LogFactory.getLog(this.getClass());
	private final JabraCloudCommunicator communicator;
	private final List<Device> devices;
	private final Map<String, Settings> devicesSettings;

	private volatile boolean inProgress;
	private volatile boolean devicePaused;
	private volatile long validRetrieveStatisticsTimestamp;
	private volatile boolean flag;
	public volatile long nextCollectionTime;

	public JabraCloudDataLoader(JabraCloudCommunicator communicator, List<Device> devices, Map<String, Settings> devicesSettings) {
		this.communicator = communicator;
		this.devices = devices;
		this.devicesSettings = devicesSettings;

		this.inProgress = true;
		this.devicePaused = true;
		this.nextCollectionTime = System.currentTimeMillis();
		this.flag = false;
	}

	/**
	 * Starts the main loop for collecting the map of {@link Device} with {@link Settings} data.
	 */
	@Override
	public void run() {
		while (this.inProgress) {
			long startCycle = System.currentTimeMillis();
			Util.delayExecution(500);
			if (!this.inProgress) {
				this.logger.debug("Main data collection thread is not in progress, breaking.");
				break;
			}
			this.updateAggregatorStatus();
			if (this.devicePaused) {
				this.logger.debug("The device communicator is paused, data collector is not active.");
				continue;
			}

			long currentTimestamp = System.currentTimeMillis();
			if (!this.flag && this.nextCollectionTime < currentTimestamp) {
				this.collectAggregatedDeviceData();
				this.flag = true;
			}

			if (!this.inProgress) {
				this.logger.debug("Main data collection thread is not in progress, breaking.");
				break;
			}
			while (this.nextCollectionTime > System.currentTimeMillis()) {
				Util.delayExecution(1000);
			}
			if (this.flag) {
				this.nextCollectionTime = System.currentTimeMillis() + POLLING_CYCLE_INTERVAL;
				this.communicator.lastMonitoringCycleDuration = System.currentTimeMillis() - startCycle;
				this.flag = false;
			}
		}
	}

	/**
	 * Stops the data collection process.
	 */
	public void stop() {
		this.inProgress = false;
	}

	/**
	 * Updates the {@code validRetrieveStatisticsTimestamp}.
	 */
	public synchronized void updateValidRetrieveStatisticsTimestamp() {
		validRetrieveStatisticsTimestamp = System.currentTimeMillis() + RETRIEVE_STATISTICS_TIMEOUT;
		this.updateAggregatorStatus();
	}

	/**
	 * Collects and updates the aggregated settings data for all registered devices.
	 * <p>
	 * For each device in the list, this method fetches the device's settings
	 * from a remote API endpoint and stores the result into a temporary map.
	 * Once all settings are retrieved successfully, the existing {@code devicesSettings}
	 * map is cleared and updated with the new data.
	 * </p>
	 *
	 * @throws ResourceNotReachableException if any device's settings cannot be fetched.
	 */
	private void collectAggregatedDeviceData() {
		Map<String, Settings> newDevicesSettings = new HashMap<>();
		for (Device device : this.devices) {
			try {
				String url = ApiConstant.GET_DEVICE_SETTINGS_ENDPOINT.replace(ApiConstant.DEVICE_ID_PARAM, device.getId());
				Settings deviceSetting = this.communicator.fetchData(url, ApiConstant.SETTINGS_FIELD, ApiConstant.SETTINGS_RES_TYPE);

				newDevicesSettings.put(device.getId(), deviceSetting);
			} catch (Exception e) {
				throw new ResourceNotReachableException(e.getMessage(), e);
			}
		}
		this.devicesSettings.clear();
		this.devicesSettings.putAll(newDevicesSettings);
	}

	/**
	 * Updates the aggregator status based on the current timestamp.
	 */
	private synchronized void updateAggregatorStatus() {
		if (this.validRetrieveStatisticsTimestamp > 0L) {
			this.devicePaused = this.validRetrieveStatisticsTimestamp < System.currentTimeMillis();
		} else {
			this.devicePaused = false;
		}
	}
}
