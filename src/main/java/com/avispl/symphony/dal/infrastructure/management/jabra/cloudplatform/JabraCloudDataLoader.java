/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform;

import java.time.Duration;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.Setting;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.valuespace.SettingsValuespace;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.adapter.ClientTypeFilter;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.adapter.RetrievalType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.Util;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.constants.ApiConstant;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.constants.Constant;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.IntervalSetting;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.device.Device;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.Settings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.UriComponentsBuilder;

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
	private final Map<String, List<Setting>> devicesSettings;
	private final Map<String, SettingsValuespace> featureModelSettingsValuespace;
	private final Map<String, String> deviceIdFeatureModelSettingsValuespace;
	private final IntervalSetting deviceSettingsInterval;
	private final IntervalSetting devicesInterval;
	private final String settingsValuespaceURLTemplate;
	private final ClientTypeFilter clientTypeFilter;
	private final int apiPageSize;

	private volatile boolean inProgress;
	private volatile boolean devicePaused;
	private volatile long validRetrieveStatisticsTimestamp;
	private volatile boolean cycleExecuted;
	private volatile long nextCollectionTime;

	public JabraCloudDataLoader(
			JabraCloudCommunicator communicator,
			List<Device> devices,
			Map<String, List<Setting>> devicesSettings, Map<String, SettingsValuespace> featureModelSettingsValuespace, Map<String, String> deviceIdFeatureModelSettingsValuespace,
			EnumMap<RetrievalType, IntervalSetting> retrievalIntervals, ClientTypeFilter clientTypeFilter, int apiPageSize, String settingsValuespaceURLTemplate
	) {
		this.communicator = communicator;
		this.devices = devices;
		this.devicesSettings = devicesSettings;
		this.deviceSettingsInterval = communicator.getIntervalSettingByType(RetrievalType.DEVICE_SETTINGS);
		this.devicesInterval = communicator.getIntervalSettingByType(RetrievalType.DEVICES);
		this.featureModelSettingsValuespace = featureModelSettingsValuespace;
		this.deviceIdFeatureModelSettingsValuespace = deviceIdFeatureModelSettingsValuespace;
		this.settingsValuespaceURLTemplate = settingsValuespaceURLTemplate;
		this.clientTypeFilter = clientTypeFilter;
		this.apiPageSize = apiPageSize;

		this.inProgress = true;
		this.devicePaused = true;
		this.nextCollectionTime = System.currentTimeMillis();
		this.cycleExecuted = false;
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

			long startCycle = System.currentTimeMillis();
			if (!this.cycleExecuted && this.nextCollectionTime < System.currentTimeMillis()) {
				if (this.devicesInterval.isValid()) {
					this.logger.info(String.format("Devices retrieval is available now. Next available: %s", this.devicesInterval.getNextAvailabilityInfo()));
					try {
						String devicesEndpoint = UriComponentsBuilder.fromPath(ApiConstant.DEVICES_ENDPOINT)
								.queryParam(ApiConstant.CLIENT_TYPE_QUERY, this.clientTypeFilter.getValue())
								.queryParam(ApiConstant.PAGE_SIZE_QUERY, this.apiPageSize)
								.toUriString();

						List<Device> fetched = this.communicator.fetchData(devicesEndpoint, ApiConstant.ITEMS_FIELD, ApiConstant.DEVICES_RES_TYPE);
						if (fetched != null) {
							Set<String> fetchedIds = fetched.stream().map(Device::getId).collect(Collectors.toSet());
							Set<String> existingIds = this.devices.stream().map(Device::getId).collect(Collectors.toSet());

							this.devices.removeIf(d -> !fetchedIds.contains(d.getId()));
							fetched.stream()
									.filter(f -> !existingIds.contains(f.getId()))
									.forEach(this.devices::add);
						}
					} catch (Exception e) {
						logger.error("Unable to retrieve devices list metadata.", e);
					}
				}

				if (this.deviceSettingsInterval.isValid() && this.communicator.shouldDisplayGroup(Constant.AGGREGATED_SETTINGS_GROUP)) {
					this.logger.info(String.format("Device settings retrieval is available now. Next available: %s", this.deviceSettingsInterval.getNextAvailabilityInfo()));
					this.collectAggregatedDeviceData();
				}
				this.cycleExecuted = true;
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
			if (this.cycleExecuted) {
				try {
					this.nextCollectionTime = System.currentTimeMillis() + (this.communicator.getMonitoringRate() * POLLING_CYCLE_INTERVAL);
				} catch (NoSuchMethodError error) {
					this.nextCollectionTime = System.currentTimeMillis() + POLLING_CYCLE_INTERVAL;
					logger.error("Unsupported feature: getMonitoringRate isn't available on current Cloud Connector version.", error);
				}
				this.communicator.setLastMonitoringCycleDuration(Math.max(System.currentTimeMillis() - startCycle, 1L));
				this.cycleExecuted = false;
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
		Map<String, List<Setting>> settingsList = new HashMap<>();
		for (Device device : this.devices) {
			try {
				String settingsValuespace = String.format(settingsValuespaceURLTemplate, device.getProductId(), device.getVariantType(), device.getFirmwareVersion());
				if (!featureModelSettingsValuespace.containsKey(settingsValuespace)) {
					SettingsValuespace valuespace = this.communicator.fetchData(settingsValuespace, new ParameterizedTypeReference<>(){});
					featureModelSettingsValuespace.put(settingsValuespace, valuespace);
				}
				deviceIdFeatureModelSettingsValuespace.put(device.getId(), settingsValuespace);

				String url = String.format(ApiConstant.DEVICE_SETTINGS_ENDPOINT, device.getId());

				List<Setting> settings = this.communicator.fetchData(url, new ParameterizedTypeReference<>() {
                });
				settingsList.put(device.getId(), settings);

			} catch (Exception e) {
				this.logger.error(e.getMessage(), e);
			}
		}
		this.devicesSettings.clear();
		this.devicesSettings.putAll(settingsList);
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
