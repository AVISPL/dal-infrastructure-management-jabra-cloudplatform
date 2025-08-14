/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	private static final long POLLING_CYCLE_INTERVAL = Duration.ofMinutes(1).toMillis();
	private static final long RETRIEVE_STATISTICS_TIMEOUT = Duration.ofMinutes(3).toMillis();

	private final Log logger = LogFactory.getLog(this.getClass());
	private final JabraCloudCommunicator communicator;
	private final List<Device> devices;
	private final Map<String, Settings> supportedDevicesSettings;
	private final Map<String, Map<String, Map<String, Object>>> unSupportedDevicesSettings;

	private volatile boolean inProgress;
	private volatile boolean devicePaused;
	private volatile long validRetrieveStatisticsTimestamp;
	private volatile boolean flag;
	private volatile long nextCollectionTime;

	public JabraCloudDataLoader(
			JabraCloudCommunicator communicator,
			List<Device> devices,
			Map<String, Settings> supportedDevicesSettings, Map<String, Map<String, Map<String, Object>>> unSupportedDevicesSettings
	) {
		this.communicator = communicator;
		this.devices = devices;
		this.supportedDevicesSettings = supportedDevicesSettings;
		this.unSupportedDevicesSettings = unSupportedDevicesSettings;

		this.inProgress = true;
		this.devicePaused = true;
		this.nextCollectionTime = System.currentTimeMillis();
		this.flag = false;
	}

	/**
	 * Sets {@link #nextCollectionTime} value
	 *
	 * @param nextCollectionTime new value of {@link #nextCollectionTime}
	 */
	public void setNextCollectionTime(long nextCollectionTime) {
		this.nextCollectionTime = nextCollectionTime;
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
				if (this.logger.isDebugEnabled()) {
					this.logger.debug("Main data collection thread is not in progress, breaking.");
				}
				break;
			}
			this.updateAggregatorStatus();
			if (this.devicePaused) {
				if (this.logger.isDebugEnabled()) {
					this.logger.debug("The device communicator is paused, data collector is not active.");
				}
				continue;
			}

			long currentTimestamp = System.currentTimeMillis();
			if (!this.flag && this.nextCollectionTime < currentTimestamp) {
				this.collectAggregatedDeviceData();
				this.flag = true;
			}

			if (!this.inProgress) {
				if (this.logger.isDebugEnabled()) {
					this.logger.debug("Main data collection thread is not in progress, breaking.");
				}
				break;
			}
			while (this.nextCollectionTime > System.currentTimeMillis()) {
				Util.delayExecution(1000);
			}
			if (this.flag) {
				this.nextCollectionTime = System.currentTimeMillis() + POLLING_CYCLE_INTERVAL;
				this.communicator.setLastMonitoringCycleDuration(System.currentTimeMillis() - startCycle);
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
	 * Collects and updates settings data for all registered devices.
	 * <p>
	 * For each device, this method fetches its settings from a remote API and stores them
	 * in a temporary map. Devices are classified as supported or unsupported based on product ID.
	 * After fetching, the existing {@link Settings} maps are cleared and updated.
	 * </p>
	 */
	private void collectAggregatedDeviceData() {
		Map<String, Settings> newSupportedDevicesSettings = new HashMap<>();
		Map<String, Map<String, Map<String, Object>>> newUnsupportedDevicesSettings = new HashMap<>();
		for (Device device : this.devices) {
			try {
				String url = ApiConstant.GET_DEVICE_SETTINGS_ENDPOINT.replace(ApiConstant.DEVICE_ID_PARAM, device.getId());
				if (Util.isSupportedDevice(device)) {
					Settings deviceSettings = this.communicator.fetchData(url, ApiConstant.SETTINGS_FIELD, ApiConstant.SETTINGS_RES_TYPE);
					newSupportedDevicesSettings.put(device.getId(), deviceSettings);
				} else {
					Map<String, Map<String, Object>> deviceSettings = this.communicator.fetchData(url, ApiConstant.SETTINGS_FIELD, ApiConstant.COMMON_SETTINGS_RES_TYPE);
					newUnsupportedDevicesSettings.put(device.getId(), deviceSettings);
				}
			} catch (Exception e) {
				this.logger.error(e.getMessage(), e);
			}
		}
		this.supportedDevicesSettings.clear();
		this.supportedDevicesSettings.putAll(newSupportedDevicesSettings);
		this.unSupportedDevicesSettings.clear();
		this.unSupportedDevicesSettings.putAll(newUnsupportedDevicesSettings);
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
