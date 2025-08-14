/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.security.auth.login.FailedLoginException;
import org.apache.commons.collections.CollectionUtils;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty.Button;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;
import com.avispl.symphony.api.dal.error.ResourceNotReachableException;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.api.dal.monitor.aggregator.Aggregator;
import com.avispl.symphony.dal.communicator.RestCommunicator;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases.BaseProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases.BaseSetting;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.RequestStateHandler;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.Util;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.constants.ApiConstant;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.constants.ApiConstant.ControlMethod;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.constants.Constant;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.device.Computer;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.device.Device;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.device.JabraClient;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.requests.SettingsRequest;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.requests.SettingsRequest.OptionDetail;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.rooms.DeviceOverview;
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
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.AutomaticZoomMode;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.settings.DynamicComposition;
import com.avispl.symphony.dal.util.StringUtils;

/**
 * Main adapter class for Jabra Cloud Platform.
 * Responsible for generating monitoring, controllable, and aggregated devices.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public class JabraCloudCommunicator extends RestCommunicator implements Monitorable, Controller, Aggregator {
	private static final long UPDATED_SETTINGS_CACHE_EXPIRY_TIME = Duration.ofMinutes(5).toMillis();
	private static final long SETTING_UPDATE_TIME = Duration.ofMinutes(3).toMillis();

	/**
	 * Lock for thread-safe operations.
	 */
	private final ReentrantLock reentrantLock;
	/**
	 * Application configuration loaded from {@code version.properties}.
	 */
	private final Properties versionProperties;
	/**
	 * Jackson object mapper for JSON serialization and deserialization.
	 */
	private final ObjectMapper objectMapper;

	/**
	 * Device adapter instantiation timestamp.
	 */
	private Long adapterInitializationTimestamp;
	/**
	 * Duration (in milliseconds) of the last monitoring cycle.
	 */
	private Long lastMonitoringCycleDuration;
	/**
	 * Executes asynchronous tasks for data loader.
	 */
	private ExecutorService executorService;
	/**
	 * Loads data from APIs for aggregated devices.
	 */
	private JabraCloudDataLoader dataLoader;
	/**
	 * Stores extended statistics to be sent to the aggregator.
	 */
	private ExtendedStatistics localExtendedStatistics;
	/**
	 * Stores local representations of aggregated devices.
	 */
	private List<AggregatedDevice> localAggregatedDevices;
	/**
	 * Handles request status tracking and error detection.
	 */
	private RequestStateHandler requestStateHandler;
	/**
	 * List of devices fetched from the {@link ApiConstant#GET_DEVICES_ENDPOINT}.
	 */
	private List<Device> devices;
	/**
	 * Mapping of device IDs to their corresponding settings from the {@link ApiConstant#GET_DEVICE_SETTINGS_ENDPOINT}
	 */
	private Map<String, Settings> supportedDevicesSettings;
	/**
	 * Maps unsupported device IDs to their raw settings structure from {@link ApiConstant#GET_DEVICE_SETTINGS_ENDPOINT}.
	 */
	private Map<String, Map<String, Map<String, Object>>> unsupportedDevicesSettings;
	/**
	 * List of overview devices collected from all rooms.
	 */
	private List<DeviceOverview> devicesRooms;
	/**
	 * List of rooms retrieved from {@link ApiConstant#GET_ROOMS_ENDPOINT} based on current devices.
	 */
	private List<Room> rooms;
	/**
	 * A cache of updated settings requests for devices.
	 * <p>
	 * This cache stores the latest pending settings changes for devices.
	 * Entries are applied when triggered by {@link SettingProperty#APPLY}
	 * and removed either after being applied or when cleared by {@link SettingProperty#CANCEL}.
	 * </p>
	 */
	private Set<SettingsRequest> updatedSettingsCaches;

	public JabraCloudCommunicator() {
		this.reentrantLock = new ReentrantLock();
		this.versionProperties = new Properties();
		this.adapterInitializationTimestamp = System.currentTimeMillis();
		this.lastMonitoringCycleDuration = 0L;
		this.objectMapper = new ObjectMapper();

		this.localExtendedStatistics = new ExtendedStatistics();
		this.localAggregatedDevices = new ArrayList<>();
		this.requestStateHandler = new RequestStateHandler();
		this.devices = new ArrayList<>();
		this.devicesRooms = new ArrayList<>();
		this.supportedDevicesSettings = new HashMap<>();
		this.unsupportedDevicesSettings = new HashMap<>();
		this.rooms = new ArrayList<>();
		this.updatedSettingsCaches = new HashSet<>();
	}

	/**
	 * Sets {@link #lastMonitoringCycleDuration} value
	 *
	 * @param lastMonitoringCycleDuration new value of {@link #lastMonitoringCycleDuration}
	 */
	public void setLastMonitoringCycleDuration(Long lastMonitoringCycleDuration) {
		this.lastMonitoringCycleDuration = lastMonitoringCycleDuration;
	}

	@Override
	protected void internalInit() throws Exception {
		this.logger.info(Constant.INITIAL_INTERNAL_INFO + this);
		this.setTrustAllCertificates(true);
		this.setAuthenticationScheme(AuthenticationScheme.None);
		this.loadProperties(this.versionProperties);
		super.internalInit();
	}

	@Override
	public List<Statistics> getMultipleStatistics() throws Exception {
		this.reentrantLock.lock();
		try {
			this.setupData();
			Map<String, String> statistics = new HashMap<>();
			statistics.putAll(this.getGeneralProperties());
			statistics.putAll(this.getRoomProperties());

			ExtendedStatistics extendedStatistics = new ExtendedStatistics();
			extendedStatistics.setStatistics(statistics);
			this.localExtendedStatistics = extendedStatistics;
		} finally {
			this.reentrantLock.unlock();
		}
		return Collections.singletonList(this.localExtendedStatistics);
	}

	@Override
	public List<AggregatedDevice> retrieveMultipleStatistics() throws Exception {
		if (CollectionUtils.isEmpty(this.devices)) {
			if (this.logger.isWarnEnabled()) {
				this.logger.warn(String.format(Constant.LIST_EMPTY_WARNING, "device"));
			}
			return Collections.emptyList();
		}
		this.setupDataLoader();
		List<AggregatedDevice> aggregatedDevices = new ArrayList<>();
		this.devices.forEach(device -> {
			AggregatedDevice aggregatedDevice = new AggregatedDevice();
			aggregatedDevice.setDeviceId(device.getId());
			aggregatedDevice.setDeviceName(device.getProductName());
			aggregatedDevice.setDeviceOnline(device.getConnected());
			aggregatedDevice.setSerialNumber(device.getSerialNumber());

			Map<String, String> properties = new HashMap<>();
			properties.putAll(this.getAggregatedGeneralProperties(device));
			properties.putAll(this.getComputerProperties(device.getComputer()));
			properties.putAll(this.getClientProperties(device.getJabraClient()));
			properties.putAll(this.getSettingsProperties(device));

			List<AdvancedControllableProperty> controllableProperties = this.getSettingsControllers(device);
			Optional.of(controllableProperties).filter(List::isEmpty).ifPresent(l -> l.add(Constant.DUMMY_CONTROLLER));

			aggregatedDevice.setProperties(properties);
			aggregatedDevice.setControllableProperties(controllableProperties);
			aggregatedDevices.add(aggregatedDevice);
		});
		this.localAggregatedDevices = aggregatedDevices;
		this.versionProperties.setProperty(GeneralProperty.LAST_MONITORING_CYCLE_DURATION.getProperty(), String.valueOf(this.lastMonitoringCycleDuration));
		this.versionProperties.setProperty(GeneralProperty.MONITORED_DEVICES_TOTAL.getProperty(), String.valueOf(this.localAggregatedDevices.size()));
		return this.localAggregatedDevices;
	}

	@Override
	public List<AggregatedDevice> retrieveMultipleStatistics(List<String> list) throws Exception {
		return this.retrieveMultipleStatistics().stream()
				.filter(aggregatedDevice -> list.contains(aggregatedDevice.getDeviceId()))
				.collect(Collectors.toList());
	}

	@Override
	public void controlProperty(ControllableProperty controllableProperty) throws Exception {
		this.reentrantLock.lock();
		try {
			String[] controllerParts = controllableProperty.getProperty().split(Constant.HASH_SYMBOL);
			String[] groupParts = controllerParts[0].split(Constant.UNDERSCORE);
			String groupName = groupParts[0];
			String propertyName = controllerParts[1];

			if (!groupName.equals(Constant.AGGREGATED_SETTINGS_GROUP)) {
				this.logger.warn("Can't defined the controller: " + controllableProperty.getProperty());
				return;
			}
			if (SettingProperty.isSupportedProperty(propertyName)) {
				SettingProperty settingProperty = BaseProperty.getByName(SettingProperty.class, propertyName);
				String settingValue = BaseProperty.getByName(settingProperty.getType(), controllableProperty.getValue().toString()).getValue();
				Optional<SettingsRequest> settingsCache = this.updatedSettingsCaches.stream()
						.filter(c -> c.getDeviceId().equals(controllableProperty.getDeviceId())).findFirst();
				if (settingsCache.isPresent()) {
					settingsCache.get().getSettings().put(settingProperty.getApiField(), new OptionDetail(settingValue));
				} else {
					SettingsRequest settingsRequest = new SettingsRequest(
							controllableProperty.getDeviceId(),
							System.currentTimeMillis() + UPDATED_SETTINGS_CACHE_EXPIRY_TIME,
							Collections.singletonMap(settingProperty.getApiField(), new OptionDetail(settingValue))
					);

					this.updatedSettingsCaches.add(settingsRequest);
				}
			} else if (SettingProperty.APPLY.getName().equals(propertyName)) {
				String url = ApiConstant.PATCH_DEVICE_SETTINGS_ENDPOINT.replace(ApiConstant.DEVICE_ID_PARAM, controllableProperty.getDeviceId());
				for (SettingsRequest settingsRequest : this.updatedSettingsCaches) {
					if (settingsRequest.getDeviceId().equals(controllableProperty.getDeviceId())) {
						this.performControlOperation(ControlMethod.PATCH, url, settingsRequest.getRequest());
						this.disconnect();
						this.updatedSettingsCaches.remove(settingsRequest);
						break;
					}
				}
			} else if (SettingProperty.CANCEL.getName().equals(propertyName)) {
				this.updatedSettingsCaches.removeIf(cache -> cache.getDeviceId().equals(controllableProperty.getDeviceId()));
			}
		} finally {
			this.reentrantLock.unlock();
		}
	}

	@Override
	public void controlProperties(List<ControllableProperty> controllableProperties) throws Exception {
		if (CollectionUtils.isEmpty(controllableProperties)) {
			if (this.logger.isWarnEnabled()) {
				this.logger.warn(Constant.CONTROLLABLE_PROPS_EMPTY_WARNING);
			}
			return;
		}
		for (ControllableProperty controllableProperty : controllableProperties) {
			this.controlProperty(controllableProperty);
		}
	}

	@Override
	protected void authenticate() throws Exception {
		if (StringUtils.isNullOrEmpty(this.getPassword())) {
			throw new FailedLoginException(Constant.LOGIN_FAILED);
		}
	}

	@Override
	protected HttpHeaders putExtraRequestHeaders(HttpMethod httpMethod, String uri, HttpHeaders headers) throws Exception {
		this.authenticate();
		headers.set(ApiConstant.API_KEY_HEADER, this.getPassword());
		headers.set(ApiConstant.API_VERSION_HEADER, "2");
		headers.setContentType(MediaType.APPLICATION_JSON);

		return super.putExtraRequestHeaders(httpMethod, uri, headers);
	}

	@Override
	protected void internalDestroy() {
		this.logger.info(Constant.DESTROY_INTERNAL_INFO + this);

		this.updatedSettingsCaches = null;
		this.rooms = null;
		this.unsupportedDevicesSettings = null;
		this.supportedDevicesSettings = null;
		this.devicesRooms = null;
		this.devices = null;
		this.requestStateHandler = null;
		this.localAggregatedDevices = null;
		this.localExtendedStatistics = null;
		if (this.executorService != null) {
			this.executorService.shutdownNow();
			this.executorService = null;
		}
		if (this.dataLoader != null) {
			this.dataLoader.stop();
			this.dataLoader = null;
		}
		this.adapterInitializationTimestamp = 0L;
		this.lastMonitoringCycleDuration = 0L;
		super.internalDestroy();
	}

	/**
	 * Loads version properties and sets initial values used to create general properties
	 * for the aggregator device.
	 *
	 * @param properties the properties to load and update
	 */
	private void loadProperties(Properties properties) {
		try {
			properties.load(this.getClass().getResourceAsStream("/version.properties"));
			properties.setProperty(GeneralProperty.ADAPTER_UPTIME.getProperty(), String.valueOf(this.adapterInitializationTimestamp));
			properties.setProperty(GeneralProperty.LAST_MONITORING_CYCLE_DURATION.getProperty(), "0");
			properties.setProperty(GeneralProperty.MONITORED_DEVICES_TOTAL.getProperty(), "0");
		} catch (IOException e) {
			this.logger.error(Constant.READ_PROPERTIES_FILE_FAILED + e.getMessage());
		}
	}

	/**
	 * Initializes and sets up data for devices and rooms.
	 * <p>
	 * This method performs the following steps:
	 * <ul>
	 *   <li>Clears existing {@code devicesRooms} and {@code rooms}.</li>
	 *   <li>Fetches the list of devices from the API.</li>
	 *   <li>For each device's group ID, retrieves the associated room and its devices.</li>
	 *   <li>Populates the {@code rooms} and {@code devicesRooms} lists accordingly.</li>
	 *   <li>Validates the API request state via {@code requestStateHandler}.</li>
	 * </ul>
	 * </p>
	 *
	 * @throws FailedLoginException if authentication fails during API calls
	 */
	private void setupData() throws FailedLoginException {
		this.requestStateHandler.clearRequests();
		this.devicesRooms.clear();
		this.rooms.clear();
		this.devices = this.fetchData(ApiConstant.GET_DEVICES_ENDPOINT, ApiConstant.ITEMS_FIELD, ApiConstant.DEVICES_RES_TYPE);
		if (CollectionUtils.isEmpty(this.devices)) {
			return;
		}
		Set<String> groupIDs = this.devices.stream().map(Device::getGroupId).filter(Objects::nonNull).collect(Collectors.toSet());
		for (String groupId : groupIDs) {
			String url = ApiConstant.GET_ROOMS_ENDPOINT.replace(ApiConstant.GROUP_ID_PARAM, groupId);
			Room room = this.fetchData(url, Room.class);
			if (room == null) {
				continue;
			}

			this.rooms.add(room);
			this.devicesRooms.addAll(Optional.ofNullable(room.getDevices()).orElse(new ArrayList<>()));
		}
		this.devices.forEach(device -> {
			Room room = this.rooms.stream().filter(r -> r.getGroupId().equals(device.getGroupId())).findFirst().orElse(null);
			if (room == null) {
				return;
			}

			String connectionStatus = Optional.ofNullable(room.getDevices()).orElse(Collections.emptyList()).stream()
					.filter(deviceRoom -> deviceRoom.getId().equals(device.getId())).findFirst()
					.map(DeviceOverview::getDeviceConnectionStatus).orElse(null);
			device.setDeviceConnectionStatus(connectionStatus);
			device.setRoomName(room.getName());
			device.setRoomType(room.getType());
			device.setRoomLocation(room.getLocationName());
		});
		this.requestStateHandler.verifyRequestState();
	}

	/**
	 * Sets up the data loader to collect and update data for aggregated devices.
	 * <p>
	 * This method initializes a single-thread executor and submits a {@link JabraCloudDataLoader}
	 * task to it if not already initialized. It also updates the collection time and retrieves valid statistics.
	 * Additionally, it updates the device connection status for each device based on {@code devicesRooms}.
	 * </p>
	 */
	private void setupDataLoader() {
		if (this.executorService == null) {
			this.executorService = Executors.newFixedThreadPool(1);
			this.dataLoader = new JabraCloudDataLoader(this, this.devices, this.supportedDevicesSettings, this.unsupportedDevicesSettings);
			this.executorService.submit(this.dataLoader);
		}
		this.dataLoader.setNextCollectionTime(System.currentTimeMillis());
		this.dataLoader.updateValidRetrieveStatisticsTimestamp();
		this.updatedSettingsCaches.removeIf(settingsCaches -> settingsCaches.getExpiryTime() <= System.currentTimeMillis());
	}

	/**
	 * Retrieves general properties related to the adapter's version and status.
	 * <p>Uses {@link Util#mapToGeneralProperty(GeneralProperty, Properties)} to map each property.</p>
	 *
	 * @return a map of general property names and their corresponding values
	 */
	private Map<String, String> getGeneralProperties() {
		return this.generateProperties(
				GeneralProperty.values(),
				null,
				property -> Util.mapToGeneralProperty(property, this.versionProperties)
		);
	}

	/**
	 * Retrieves properties for each room and groups them accordingly.
	 * <p>Each room's properties are prefixed with a group name (e.g., JabraRoom_01, JabraRoom_02, ...).</p>
	 * <p>If no rooms are available, logs a warning and returns an empty map.</p>
	 *
	 * @return a map of grouped room property names and their corresponding values
	 */
	private Map<String, String> getRoomProperties() {
		if (CollectionUtils.isEmpty(this.rooms)) {
			if (this.logger.isWarnEnabled()) {
				this.logger.warn(String.format(Constant.LIST_EMPTY_WARNING, Constant.ROOM_GROUP));
			}
			return Collections.emptyMap();
		}
		Map<String, String> properties = new HashMap<>();
		for (int i = 0; i < this.rooms.size(); i++) {
			Room room = this.rooms.get(i);
			String groupName = String.format(Constant.GROUP_FORMAT, Constant.ROOM_GROUP, i + 1);
			properties.putAll(this.generateProperties(
					RoomProperty.values(), groupName, property -> Util.mapToRoomProperty(property, room)
			));
		}

		return properties;
	}

	/**
	 * Retrieves a map of general properties for the Aggregated device.
	 *
	 * @param device the {@link Device} to extract properties from
	 * @return a map of property names and their corresponding values, or an empty map if device is null
	 */
	private Map<String, String> getAggregatedGeneralProperties(Device device) {
		if (device == null) {
			if (this.logger.isWarnEnabled()) {
				this.logger.warn(String.format(Constant.OBJECT_EMPTY_WARNING, "device"));
			}
			return Collections.emptyMap();
		}
		boolean isDeviceInRoom = this.devicesRooms.stream().anyMatch(deviceOverview -> deviceOverview.getId().equals(device.getId()));
		Map<String, String> properties = new HashMap<>();
		properties.putAll(this.generateProperties(AggregatedGeneralProperty.values(), null, property -> Util.mapToAggregatedGeneralProperty(property, device)));
		if (isDeviceInRoom) {
			properties.putAll(this.generateProperties(OptionalGeneralProperty.values(), null, property -> Util.mapToOptionalGeneralProperty(property, device)));
		}

		return properties;
	}

	/**
	 * Retrieves a map of computer properties for the Aggregated device.
	 *
	 * @param computer the {@link Computer} to extract properties from
	 * @return a map of property names and their corresponding values, or an empty map if computer is null
	 */
	private Map<String, String> getComputerProperties(Computer computer) {
		if (computer == null) {
			if (this.logger.isWarnEnabled()) {
				this.logger.warn(String.format(Constant.OBJECT_EMPTY_WARNING, "device"));
			}
			return Collections.emptyMap();
		}
		return this.generateProperties(
				ComputerProperty.values(),
				Constant.AGGREGATED_COMPUTER_GROUP,
				property -> Util.mapToComputerProperty(property, computer)
		);
	}

	/**
	 * Retrieves a map of client properties for the Aggregated device.
	 *
	 * @param client the {@link JabraClient} to extract properties from
	 * @return a map of property names and their corresponding values, or an empty map if client is null
	 */
	private Map<String, String> getClientProperties(JabraClient client) {
		if (client == null) {
			if (this.logger.isWarnEnabled()) {
				this.logger.warn(String.format(Constant.OBJECT_EMPTY_WARNING, "device"));
			}
			return Collections.emptyMap();
		}
		return this.generateProperties(
				ClientProperty.values(),
				Constant.AGGREGATED_CLIENT_GROUP,
				property -> Util.mapToClientProperty(property, client)
		);
	}

	/**
	 * Returns a map of setting properties and their selected values for a given device.
	 * <p>
	 * If the device is disconnected, actual values are returned; otherwise, values are
	 * marked as {@link Constant#NOT_AVAILABLE}. Returns an empty map if the device is null.
	 * </p>
	 *
	 * @param device the target device
	 * @return a map of property names and selected values, or an empty map if unavailable
	 */
	private Map<String, String> getSettingsProperties(Device device) {
		if (device == null) {
			if (this.logger.isWarnEnabled()) {
				this.logger.warn(String.format(Constant.OBJECT_EMPTY_WARNING, "device"));
			}
			return Collections.emptyMap();
		}
		if (!Util.isSupportedDevice(device)) {
			return Util.mapToSettingsProperties(this.unsupportedDevicesSettings.get(device.getId()));
		}
		Settings settings = this.supportedDevicesSettings.get(device.getId());
		if (settings == null || settings.isNull()) {
			return Collections.emptyMap();
		}
		Map<String, OptionDetail> updatedSettingsCache = this.getUpdatedSettingsCacheByDeviceId(device.getId());
		boolean isConnectedDevice = Boolean.TRUE.equals(device.getConnected());
		String dynamicComposition = Optional.ofNullable(updatedSettingsCache.get(SettingProperty.DYNAMIC_COMPOSITION.getApiField()))
				.map(OptionDetail::getSelected)
				.orElse(Optional.ofNullable(settings.getDynamicComposition()).orElse(new SettingDetail()).getSelected());
		String automaticZoomMode = Optional.ofNullable(updatedSettingsCache.get(SettingProperty.AUTOMATIC_ZOOM_MODE.getApiField()))
				.map(OptionDetail::getSelected)
				.orElse(Optional.ofNullable(settings.getAutomaticZoomMode()).orElse(new SettingDetail()).getSelected());
		Map<String, String> properties = new HashMap<>(this.generateSettingsProperty(isConnectedDevice, settings, SettingProperty.DYNAMIC_COMPOSITION));

		if (Objects.equals(dynamicComposition, DynamicComposition.OFF.getValue())) {
			properties.putAll(this.generateSettingsProperty(isConnectedDevice, settings, SettingProperty.AUTOMATIC_ZOOM_MODE));
			if (!Objects.equals(automaticZoomMode, AutomaticZoomMode.FULL_SCREEN.getValue())) {
				properties.putAll(this.generateSettingsProperty(isConnectedDevice, settings, SettingProperty.AUTOMATIC_ZOOM_SPEED));
			}
			properties.putAll(this.generateSettingsProperty(isConnectedDevice, settings, SettingProperty.SETTINGS_REVERT_TO_DEFAULT));
			properties.putAll(this.generateSettingsProperty(isConnectedDevice, settings, SettingProperty.FIELD_OF_VIEW));
			properties.putAll(this.generateSettingsProperty(isConnectedDevice, settings, SettingProperty.VIDEO_STITCHING));
		}
		properties.putAll(this.generateSettingsProperty(isConnectedDevice, settings, SettingProperty.SAFETY_CAPACITY_NOTIFICATION));
		if (!updatedSettingsCache.isEmpty()) {
			properties.putAll(this.generateSettingsProperty(isConnectedDevice, device.getId(), SettingProperty.APPLY));
			properties.putAll(this.generateSettingsProperty(isConnectedDevice, device.getId(), SettingProperty.CANCEL));
		}

		return properties;
	}

	/**
	 * Generates dropdown and action button controls for a supported and connected device.
	 * <p>
	 * For each supported {@link SettingProperty}, a dropdown control is created
	 * with the available options and the current selected value (from the {@link this#updatedSettingsCaches} if available,
	 * otherwise from the device settings).
	 * </p>
	 * <p>
	 * If there are cached settings for the device, additional "Apply" and "Cancel" buttons
	 * are added to allow applying or discarding pending changes.
	 * </p>
	 * <p>
	 * Returns an empty list if the device is:
	 * <ul>
	 *   <li>{@code null}</li>
	 *   <li>Disconnected</li>
	 *   <li>Not supported by {@link Util#isSupportedDevice(Device)}</li>
	 *   <li>Has no settings available</li>
	 * </ul>
	 * </p>
	 *
	 * @param device the connected and supported device for which settings controls should be generated
	 * @return a list of {@link AdvancedControllableProperty} containing dropdowns for each setting,
	 *         and optionally "Apply"/"Cancel" buttons if there are cached changes;
	 *         an empty list if no controls are applicable
	 */
	private List<AdvancedControllableProperty> getSettingsControllers(Device device) {
		if (device == null || Boolean.FALSE.equals(device.getConnected()) || !Util.isSupportedDevice(device)) {
			return new ArrayList<>();
		}
		Settings settings = this.supportedDevicesSettings.get(device.getId());
		if (settings == null || settings.isNull()) {
			return new ArrayList<>();
		}
		Map<String, OptionDetail> updatedSettingsCache = this.getUpdatedSettingsCacheByDeviceId(device.getId());
		List<AdvancedControllableProperty> controllableProperties = new ArrayList<>();
		SettingProperty.getSupportedProperties().forEach(property -> {
			String propertyName = String.format(Constant.PROPERTY_FORMAT, Constant.AGGREGATED_SETTINGS_GROUP, property.getName());
			String[] options = BaseProperty.getNames(property.getType());
			OptionDetail settingDetailCache = updatedSettingsCache.get(property.getApiField());
			String currentValue;
			if (settingDetailCache == null) {
				currentValue = Util.mapToSettingsProperty(property, settings);
			} else {
				SettingProperty settingProperty = SettingProperty.getByApiField(property.getApiField());
				BaseSetting settingType = BaseSetting.getByValue(settingProperty.getType(), settingDetailCache.getSelected());
				currentValue = settingType.getName();
			}
			AdvancedControllableProperty controllableProperty = this.generateControllableDropdown(propertyName, options, options, currentValue);

			controllableProperties.add(controllableProperty);
		});
		if (!updatedSettingsCache.isEmpty()) {
			controllableProperties.add(this.generateControllableButton(
					String.format(Constant.PROPERTY_FORMAT, Constant.AGGREGATED_SETTINGS_GROUP, SettingProperty.APPLY.getName()),
					SettingProperty.APPLY.getName(), "Changing", SETTING_UPDATE_TIME
			));
			controllableProperties.add(this.generateControllableButton(
					String.format(Constant.PROPERTY_FORMAT, Constant.AGGREGATED_SETTINGS_GROUP, SettingProperty.CANCEL.getName()),
					SettingProperty.CANCEL.getName(), "Canceling", 0L
			));
		}

		return controllableProperties;
	}

	/**
	 * Generates a map of property names and their corresponding values.
	 * <p>
	 * Each property name can be optionally prefixed with a group name using a predefined format.
	 * The values are derived using the provided mapping function, with {@link Constant#NOT_AVAILABLE} as a fallback for null results.
	 * </p>
	 *
	 * @param <T> the enum type that extends {@link BaseProperty}
	 * @param properties the array of enum constants to be processed; if null, an empty map is returned
	 * @param groupName optional group name used to prefix each property's name; can be null
	 * @param mapper a function that maps each property to its corresponding string value;
	 * if null or if the result is null, {@link Constant#NOT_AVAILABLE} is used as the value
	 * @return a map where keys are (optionally grouped) property names and values are mapped strings or {@link Constant#NOT_AVAILABLE}
	 */
	private <T extends Enum<T> & BaseProperty> Map<String, String> generateProperties(T[] properties, String groupName, Function<T, String> mapper) {
		if (properties == null || mapper == null) {
			return Collections.emptyMap();
		}
		return Arrays.stream(properties).collect(Collectors.toMap(
				property -> Objects.isNull(groupName) ? property.getName() : String.format(Constant.PROPERTY_FORMAT, groupName, property.getName()),
				property -> Optional.ofNullable(mapper.apply(property)).orElse(Constant.NOT_AVAILABLE)
		));
	}

	/**
	 * Generates a supported settings property ({@link SettingProperty#getSupportedProperties()}) map for a given device setting.
	 * <p>
	 * The method constructs a property name using a predefined format and determines the corresponding value
	 * based on the device's connection status. If the device is connected, a {@link Constant#NOT_AVAILABLE} is returned.
	 * Otherwise, it attempts to extract the value from the provided {@link Settings} object using a utility method.
	 *
	 * @param isConnected whether the device is currently connected
	 * @param settings the {@link Settings} object containing the device's configuration
	 * @param property the specific {@link SettingProperty} to extract the value for
	 * @return a singleton map where the key is the generated property name and the value is the resolved setting value or a default fallback
	 */
	private Map<String, String> generateSettingsProperty(boolean isConnected, Settings settings, SettingProperty property) {
		String propertyName = String.format(Constant.PROPERTY_FORMAT, Constant.AGGREGATED_SETTINGS_GROUP, property.getName());
		String propertyValue = isConnected
				? Constant.NOT_AVAILABLE
				: Optional.ofNullable(Util.mapToSettingsProperty(property, settings)).orElse(Constant.NOT_AVAILABLE);

		return Collections.singletonMap(propertyName, propertyValue);
	}

	/**
	 * Generates a settings controllable property map for a given device ID.
	 * <p>
	 * The method constructs a property name using a predefined format and determines the corresponding value
	 * based on the device's connection status and whether an updated settings cache exists for the device.
	 * If the device is connected and has a pending settings cache, {@link Constant#NOT_AVAILABLE} is returned.
	 * Otherwise, it attempts to extract the value using a utility method with a new {@link Settings} instance.
	 * </p>
	 *
	 * @param isConnected whether the device is currently connected
	 * @param deviceId the unique identifier of the device
	 * @param property the specific {@link SettingProperty} to extract the value for
	 * @return a singleton map where the key is the generated property name and the value is the resolved setting value or a default fallback
	 */
	private Map<String, String> generateSettingsProperty(boolean isConnected, String deviceId, SettingProperty property) {
		boolean hasUpdatedSettingsCache = this.updatedSettingsCaches.stream().anyMatch(cache -> cache.getDeviceId().equals(deviceId));
		String propertyName = String.format(Constant.PROPERTY_FORMAT, Constant.AGGREGATED_SETTINGS_GROUP, property.getName());
		String propertyValue = isConnected && hasUpdatedSettingsCache
				? Constant.NOT_AVAILABLE
				: Optional.ofNullable(Util.mapToSettingsProperty(property, new Settings())).orElse(Constant.NOT_AVAILABLE);

		return Collections.singletonMap(propertyName, propertyValue);
	}

	/**
	 * Generates an {@link AdvancedControllableProperty} of type Dropdown with the specified name, labels, options, and value.
	 *
	 * @param dropdownName the name of the dropdown control property
	 * @param labels the display labels for each dropdown option
	 * @param options the actual option values associated with each label
	 * @param value the initial selected value of the dropdown
	 * @return an {@link AdvancedControllableProperty} configured as a dropdown control
	 */
	private AdvancedControllableProperty generateControllableDropdown(String dropdownName, String[] labels, String[] options, Object value) {
		AdvancedControllableProperty.DropDown dropdown = new AdvancedControllableProperty.DropDown();
		dropdown.setLabels(labels);
		dropdown.setOptions(options);

		return new AdvancedControllableProperty(dropdownName, new Date(), dropdown, value);
	}

	/**
	 * Generates an {@link AdvancedControllableProperty} of type Button with the specified name, labels, and grace period.
	 *
	 * @param buttonName the name of the button control property
	 * @param label the label to display on the button
	 * @param labelPressed the label to display when the button is pressed
	 * @param gracePeriod the time in milliseconds before the button can be pressed again
	 * @return an {@link AdvancedControllableProperty} configured as a button control
	 */
	private AdvancedControllableProperty generateControllableButton(String buttonName, String label, String labelPressed, long gracePeriod) {
		AdvancedControllableProperty.Button button = new Button();
		button.setLabel(label);
		button.setLabelPressed(labelPressed);
		button.setGracePeriod(gracePeriod);

		return new AdvancedControllableProperty(buttonName, new Date(), button, Constant.NOT_AVAILABLE);
	}

	/**
	 * Retrieves the updated settings cache for a specific device.
	 *
	 * @param deviceId the unique identifier of the device
	 * @return a map of setting API field names to their {@link OptionDetail} values,
	 * or an empty map if the device has no cached settings
	 */
	private Map<String, OptionDetail> getUpdatedSettingsCacheByDeviceId(String deviceId) {
		return this.updatedSettingsCaches.stream()
				.filter(settingsCache -> settingsCache.getDeviceId().equals(deviceId)).findFirst()
				.map(SettingsRequest::getSettings).orElse(Collections.emptyMap());
	}

	/**
	 * Fetches data from a given endpoint and maps the response to the specified class type.
	 *
	 * @param endpoint the API endpoint to fetch data from
	 * @param responseClass the target class to map the response to
	 * @param <T> the type of the expected response
	 * @return the mapped response object, or null if response is empty
	 * @throws FailedLoginException if authentication fails
	 */
	private <T> T fetchData(String endpoint, Class<T> responseClass) throws FailedLoginException {
		String responseClassName = responseClass.getSimpleName();
		try {
			this.requestStateHandler.pushRequest(endpoint);
			T response = super.doGet(endpoint, responseClass);
			if (Objects.isNull(response) && this.logger.isWarnEnabled()) {
				this.logger.warn(String.format(Constant.FETCHED_DATA_NULL_WARNING, endpoint, responseClassName));
			}
			this.requestStateHandler.resolveError(endpoint);

			return response;
		} catch (FailedLoginException e) {
			throw e;
		} catch (ResourceNotReachableException e) {
			throw new ResourceNotReachableException(e.getMessage(), e);
		} catch (Exception e) {
			this.requestStateHandler.pushError(endpoint, e);
			this.logger.error(String.format(Constant.FETCH_DATA_FAILED, endpoint, responseClassName), e);
			return null;
		}
	}

	/**
	 * Fetches data from a given endpoint and extracts a specific field to map using a {@link TypeReference}.
	 *
	 * @param endpoint the API endpoint to fetch data from
	 * @param indicatedField the field name in the JSON response to extract and map
	 * @param typeReference the {@link TypeReference} defining the target type
	 * @param <T> the type of the expected response
	 * @return the mapped response object, or null if response is empty
	 * @throws FailedLoginException if authentication fails
	 */
	public <T> T fetchData(String endpoint, String indicatedField, TypeReference<T> typeReference) throws FailedLoginException {
		String typeReferenceName = typeReference.getType().getTypeName();
		try {
			this.requestStateHandler.pushRequest(endpoint);
			String response = super.doGet(endpoint);
			T mappedResponse = this.objectMapper.readValue(this.objectMapper.readTree(response).get(indicatedField).toString(), typeReference);
			if (Objects.isNull(response) && this.logger.isWarnEnabled()) {
				this.logger.warn(String.format(Constant.FETCHED_DATA_NULL_WARNING, endpoint, typeReferenceName));
			}
			this.requestStateHandler.resolveError(endpoint);

			return mappedResponse;
		} catch (FailedLoginException e) {
			throw e;
		} catch (ResourceNotReachableException e) {
			throw new ResourceNotReachableException(e.getMessage(), e);
		} catch (Exception e) {
			this.requestStateHandler.pushError(endpoint, e);
			this.logger.error(String.format(Constant.FETCH_DATA_FAILED, endpoint, typeReferenceName), e);
			return null;
		}
	}

	/**
	 * Performs a control operation on the given URI with the provided request body.
	 * <p>Logs and throws meaningful exceptions for failure scenarios.</p>
	 *
	 * @param httpMethod the HTTP method to use
	 * @param endpoint the target URI to send the request to
	 * @param requestBody the request payload to be sent
	 */
	private void performControlOperation(ControlMethod httpMethod, String endpoint, Object requestBody) throws Exception {
		switch (httpMethod) {
			case POST:
				this.doPost(endpoint, requestBody, Object.class);
				break;
			case PATCH:
				this.doPatch(endpoint, requestBody, Object.class);
				break;
			default:
				break;
		}
	}
}
