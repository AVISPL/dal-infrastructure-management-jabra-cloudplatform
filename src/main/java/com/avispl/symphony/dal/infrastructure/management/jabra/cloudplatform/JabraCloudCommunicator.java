/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.data.JabraSettingsHttpMessageConverter;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.data.JabraSettingsValuespaceHttpMessageConverter;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.*;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.valuespace.SettingDescriptor;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.valuespace.SettingsValuespace;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.security.auth.login.FailedLoginException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import com.avispl.symphony.api.common.error.InvalidArgumentException;
import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.dto.monitor.aggregator.AggregatedDevice;
import com.avispl.symphony.api.dal.error.ResourceNotReachableException;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.api.dal.monitor.aggregator.Aggregator;
import com.avispl.symphony.dal.communicator.RestCommunicator;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.bases.BaseProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.RequestStateHandler;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.Util;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.constants.ApiConstant;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.common.constants.Constant;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.IntervalSetting;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.device.Computer;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.device.Device;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.device.JabraClient;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.requests.SettingsRequest;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.requests.SettingsRequest.OptionDetail;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.rooms.DeviceOverview;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.rooms.Room;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.adapter.ClientTypeFilter;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.adapter.RetrievalType;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated.AggregatedGeneralProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated.ClientProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated.ComputerProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated.OptionalGeneralProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregated.SettingProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregator.GeneralProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.types.aggregator.RoomProperty;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.data.JabraCloudRequestInterceptor;
import com.avispl.symphony.dal.util.StringUtils;

import static com.avispl.symphony.dal.util.ControllablePropertyFactory.*;

/**
 * Main adapter class for Jabra Cloud Platform.
 * Responsible for generating monitoring, controllable, and aggregated devices.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
public class JabraCloudCommunicator extends RestCommunicator implements Monitorable, Controller, Aggregator {
	private static final Set<String> DEFAULT_GRAPH_PROPERTIES = new HashSet<>(Arrays.asList(
			GeneralProperty.LAST_MONITORING_CYCLE_DURATION.getName(),
			GeneralProperty.MONITORED_DEVICES_TOTAL.getName()
	));
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
	 * Requested page for paginated API endpoints
	 * */
	private int apiPageSize = 100;
	/**
	 * API Version value for API header use
	 * */
	private String apiVersion = "1";
	/**
	 * Device adapter instantiation timestamp.
	 */
	private long adapterInitializationTimestamp;

	/**
	 * Last control activation timestamp for controls cooldown calculation
	 * */
	private Long lastControlActivationTimestamp = 0L;

	/**
	 * Duration (in milliseconds) of the last monitoring cycle.
	 */
	private long lastMonitoringCycleDuration;
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
	 * List of devices fetched from the {@link ApiConstant#DEVICES_ENDPOINT}.
	 */
	private volatile List<Device> devices;
	/**
	 * Mapping of device IDs to their corresponding settings from the {@link ApiConstant#DEVICE_SETTINGS_ENDPOINT}
	 */
	private Map<String, List<Setting>> devicesSettings;

	/**
	 * Feature model settings valuespace, containing all values and limits for supported models' settings
	 * */
	private Map<String, SettingsValuespace> featureModelSettingsValuespace;
	private Map<String, String> deviceIdFeatureModelSettingsValuespace;
	/**
	 * Room groupName:room map
	 * */
	private Map<String, Room> availableRooms = new HashMap<>();
	/**
	 * List of overview devices collected from all rooms.
	 */
	private List<DeviceOverview> devicesRooms = new ArrayList<>();
	/**
	 * List of rooms retrieved from {@link ApiConstant#ROOMS_ENDPOINT} based on current devices.
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

	/** Indicates whether all devices should be shown; defaults to false. */
	private ClientTypeFilter clientTypeFilter;
	/** Indicates whether control properties are visible; defaults to false. */
	private boolean configManagement;
	/**
	 * Default controls valuespace reference.
	 * */
	private String settingsValuespaceURLTemplate = "https://cdn.cloud.jabra.com/models/v/16/vendors/2830/products/%s/variants/%s/firmware-versions/%s/feature-model.json";
	/** Interval control for retrieving data from APIs. */
	private EnumMap<RetrievalType, IntervalSetting> retrievalIntervals;
	private Set<String> displayPropertyGroups;
    private final JabraCloudRequestInterceptor jabraCloudRequestInterceptor = new JabraCloudRequestInterceptor();
    private final JabraSettingsHttpMessageConverter jabraSettingsHttpMessageConterter = new JabraSettingsHttpMessageConverter();
    private final JabraSettingsValuespaceHttpMessageConverter jabraSettingsValuespaceHttpMessageConverter = new JabraSettingsValuespaceHttpMessageConverter();

	public JabraCloudCommunicator() {
		this.reentrantLock = new ReentrantLock();
		this.versionProperties = new Properties();
		this.adapterInitializationTimestamp = System.currentTimeMillis();
		this.objectMapper = new ObjectMapper();

		this.localExtendedStatistics = new ExtendedStatistics();
		this.localAggregatedDevices = new CopyOnWriteArrayList<>();
		this.requestStateHandler = new RequestStateHandler();
		this.devices = new CopyOnWriteArrayList<>();
		this.devicesRooms = new CopyOnWriteArrayList<>();
		this.devicesSettings = new ConcurrentHashMap<>();
		this.featureModelSettingsValuespace = new ConcurrentHashMap<>();
		this.deviceIdFeatureModelSettingsValuespace = new ConcurrentHashMap<>();
		this.rooms = new CopyOnWriteArrayList<>();
		this.updatedSettingsCaches = new HashSet<>();

		this.clientTypeFilter = ClientTypeFilter.MEETING_ROOM;
		this.configManagement = false;
		this.retrievalIntervals = new EnumMap<>(RetrievalType.class);
		this.displayPropertyGroups = new HashSet<>();
	}

	/**
	 * Retrieves {@link #apiVersion}
	 *
	 * @return value of {@link #apiVersion}
	 */
	public String getApiVersion() {
		return apiVersion;
	}

	/**
	 * Sets {@link #apiVersion} value
	 *
	 * @param apiVersion new value of {@link #apiVersion}
	 */
	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	/**
	 * Retrieves {@link #apiPageSize}
	 *
	 * @return value of {@link #apiPageSize}
	 */
	public int getApiPageSize() {
		return apiPageSize;
	}
	/**
	 * Sets {@link #apiPageSize} value
	 *
	 * @param apiPageSize new value of {@link #apiPageSize}
	 */
	public void setApiPageSize(int apiPageSize) {
		this.apiPageSize = apiPageSize;
	}

	/**
	 * Retrieves {@link #clientTypeFilter}
	 *
	 * @return value of {@link #clientTypeFilter}
	 */
	public String getClientTypeFilter() {
		return clientTypeFilter.getName();
	}

	/**
	 * Sets {@link #clientTypeFilter} value
	 *
	 * @param clientTypeFilter new value of {@link #clientTypeFilter}
	 */
	public void setClientTypeFilter(String clientTypeFilter) {
		if (StringUtils.isNullOrEmpty(clientTypeFilter, true)) {
			return;
		}
		ClientTypeFilter clientType = BaseProperty.getByNameIgnoreCase(ClientTypeFilter.class, clientTypeFilter.trim());
		this.clientTypeFilter = Optional.ofNullable(clientType).orElse(ClientTypeFilter.UNDEFINED);
	}

	/**
	 * Retrieves {@link #settingsValuespaceURLTemplate}
	 *
	 * @return value of {@link #settingsValuespaceURLTemplate}
	 */
	public String getSettingsValuespaceURLTemplate() {
		return settingsValuespaceURLTemplate;
	}

	/**
	 * Sets {@link #settingsValuespaceURLTemplate} value
	 *
	 * @param settingsValuespaceURLTemplate new value of {@link #settingsValuespaceURLTemplate}
	 */
	public void setSettingsValuespaceURLTemplate(String settingsValuespaceURLTemplate) {
		this.settingsValuespaceURLTemplate = settingsValuespaceURLTemplate;
	}

	/**
	 * Retrieves {@link #configManagement}
	 *
	 * @return value of {@link #configManagement}
	 */
	public boolean isConfigManagement() {
		return configManagement;
	}

	/**
	 * Sets {@link #configManagement} value
	 *
	 * @param configManagement new value of {@link #configManagement}
	 */
	public void setConfigManagement(boolean configManagement) {
		this.configManagement = configManagement;
	}

	/**
	 * Returns the configured retrieval interval (in milliseconds) for aggregated device information.
	 * <p>
	 * If the interval has not been explicitly set, a default {@link IntervalSetting}
	 * will be created and returned.
	 *
	 * @return the retrieval interval for aggregated devices, in milliseconds
	 */
	public long getDevicesInterval() {
		return this.getIntervalSettingByType(RetrievalType.DEVICES).getIntervalMs();
	}

	/**
	 * Sets the retrieval interval(ms) for aggregated device's general from adapter properties.
	 *
	 * @param devicesInterval the interval duration(ms)
	 */
	public void setDevicesInterval(long devicesInterval) {
		this.retrievalIntervals.put(RetrievalType.DEVICES, new IntervalSetting(devicesInterval));
	}

	/**
	 * Returns the configured retrieval interval(ms) for room group.
	 * <p>
	 * If the interval has not been explicitly set, a default {@link IntervalSetting}
	 * will be created and returned.
	 *
	 * @return the retrieval interval for rooms(ms)
	 */
	public long getRoomsInterval() {
		return this.getIntervalSettingByType(RetrievalType.ROOMS).getIntervalMs();
	}

	/**
	 * Sets the retrieval interval(ms) for room group from adapter properties.
	 *
	 * @param roomsInterval the interval duration(ms)
	 */
	public void setRoomsInterval(long roomsInterval) {
		this.retrievalIntervals.put(RetrievalType.ROOMS, new IntervalSetting(roomsInterval));
	}

	/**
	 * Returns the configured retrieval interval(ms) for device settings of aggregated device.
	 * <p>
	 * If the interval has not been explicitly set, a default {@link IntervalSetting}
	 * will be created and returned.
	 *
	 * @return the retrieval interval for device settings(ms)
	 */
	public long getDeviceSettingsInterval() {
		return this.getIntervalSettingByType(RetrievalType.DEVICE_SETTINGS).getIntervalMs();
	}

	/**
	 * Sets the retrieval interval(ms) for device settings from adapter properties.
	 *
	 * @param deviceSettingsInterval the interval duration(ms)
	 */
	public void setDeviceSettingsInterval(long deviceSettingsInterval) {
		this.retrievalIntervals.put(RetrievalType.DEVICE_SETTINGS, new IntervalSetting(deviceSettingsInterval));
	}

	/**
	 * Sets {@link #lastMonitoringCycleDuration} value
	 *
	 * @param lastMonitoringCycleDuration new value of {@link #lastMonitoringCycleDuration}
	 */
	public void setLastMonitoringCycleDuration(Long lastMonitoringCycleDuration) {
		this.lastMonitoringCycleDuration = lastMonitoringCycleDuration;
	}

	/**
	 * Returns a comma-separated list of property group names that are configured to be displayed.
	 *
	 * @return a comma-separated string of display property group names; may be empty if no groups are configured
	 */
	public String getDisplayPropertyGroups() {
		return String.join(Constant.COMMA, this.displayPropertyGroups);
	}

	/**
	 * Sets the list of property groups to be displayed, using a comma-separated string from adapter properties.
	 *
	 * @param displayPropertyGroups a comma-separated list of property group names to display; may be {@code null} or empty
	 */
	public void setDisplayPropertyGroups(String displayPropertyGroups) {
		this.displayPropertyGroups.clear();
		if (StringUtils.isNullOrEmpty(displayPropertyGroups)) {
			return;
		}
		Arrays.stream(displayPropertyGroups.split(Constant.COMMA)).map(String::trim)
				.filter(displayPropertyGroup -> !displayPropertyGroup.isEmpty())
				.forEach(displayPropertyGroup -> this.displayPropertyGroups.add(displayPropertyGroup));
	}

	@Override
	protected void internalInit() throws Exception {
		this.logger.info(Constant.INITIAL_INTERNAL_INFO + this);
		this.setTrustAllCertificates(true);
		this.setAuthenticationScheme(AuthenticationScheme.None);
		this.loadProperties();
		super.internalInit();
	}

	@Override
	public List<Statistics> getMultipleStatistics() throws Exception {
		if (System.currentTimeMillis() - lastControlActivationTimestamp <= 5000) {
			if (logger.isDebugEnabled()) {
				logger.debug("Device aggregator is in the cooldown state, emergency delivery update is skipped.");
			}
			return Collections.singletonList(this.localExtendedStatistics);
		}
		this.reentrantLock.lock();
		try {
			this.verifyAdapterProperties();
			this.setupData();
			Map<String, String> statistics = new HashMap<>(this.getGeneralProperties());
			List<AdvancedControllableProperty> controls = new ArrayList<>();
			if (this.shouldDisplayGroup(Constant.ROOM_GROUP)) {
				this.retrieveRoomProperties(statistics, controls);
			}

			ExtendedStatistics extendedStatistics = new ExtendedStatistics();
			extendedStatistics.setStatistics(statistics);
			extendedStatistics.setDynamicStatistics(this.getDynamicStatistics(statistics));
			extendedStatistics.setControllableProperties(controls);
			this.localExtendedStatistics = extendedStatistics;
		} finally {
			this.reentrantLock.unlock();
		}
		return Collections.singletonList(this.localExtendedStatistics);
	}

	@Override
	public List<AggregatedDevice> retrieveMultipleStatistics() {
		if (System.currentTimeMillis() - lastControlActivationTimestamp <= 5000) {
			if (logger.isDebugEnabled()) {
				logger.debug("Device aggregator is in the cooldown state, emergency delivery update is skipped.");
			}
			updateDeviceSettingsMode();
			return this.localAggregatedDevices;
		}
		this.setupDataLoader();
		if (CollectionUtils.isEmpty(this.devices)) {
			if (this.logger.isWarnEnabled()) {
				this.logger.warn(String.format(Constant.LIST_EMPTY_WARNING, "device"));
			}
			return Collections.emptyList();
		}
		List<AggregatedDevice> aggregatedDevices = new ArrayList<>();
		this.devices.forEach(device -> {
			AggregatedDevice aggregatedDevice = new AggregatedDevice();
			aggregatedDevice.setDeviceId(device.getId());
			String deviceName = device.getName();
			if (StringUtils.isNullOrEmpty(deviceName)) {
				deviceName = device.getProductName();
			}
			aggregatedDevice.setDeviceName(deviceName);

			aggregatedDevice.setDeviceModel(device.getProductName());
			aggregatedDevice.setDeviceMake("Jabra");
			aggregatedDevice.setType("AV Devices");
			aggregatedDevice.setCategory(defineDeviceCategory(device.getProductName()));

			String deviceConnectionStatus = device.getDeviceConnectionStatus();
			aggregatedDevice.setDeviceOnline(StringUtils.isNotNullOrEmpty(deviceConnectionStatus) && !"Offline".equals(deviceConnectionStatus));
			aggregatedDevice.setSerialNumber(device.getSerialNumber());
			aggregatedDevice.setTimestamp(System.currentTimeMillis());

			Map<String, String> properties = new HashMap<>(this.getAggregatedGeneralProperties(device));
			List<AdvancedControllableProperty> controllableProperties = new ArrayList<>();
			if (this.shouldDisplayGroup(Constant.AGGREGATED_COMPUTER_GROUP)) {
				properties.putAll(this.getComputerProperties(device.getComputer()));
			}
			if (this.shouldDisplayGroup(Constant.AGGREGATED_CLIENT_GROUP)) {
				properties.putAll(this.getClientProperties(device.getJabraClient()));
			}
			if (this.shouldDisplayGroup(Constant.AGGREGATED_SETTINGS_GROUP)) {
				this.processSettingsProperties(device, properties, controllableProperties);
			}

			Optional.of(controllableProperties).filter(List::isEmpty).ifPresent(l -> l.add(Constant.DUMMY_CONTROLLER));

			aggregatedDevice.setProperties(properties);
			aggregatedDevice.setControllableProperties(controllableProperties);
			aggregatedDevices.add(aggregatedDevice);
		});
		this.localAggregatedDevices = aggregatedDevices;
		this.versionProperties.setProperty(GeneralProperty.LAST_MONITORING_CYCLE_DURATION.getProperty(), String.valueOf(this.lastMonitoringCycleDuration));
		this.versionProperties.setProperty(GeneralProperty.MONITORED_DEVICES_TOTAL.getProperty(), String.valueOf(this.localAggregatedDevices.size()));

		updateDeviceSettingsMode();
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
			String deviceId = controllableProperty.getDeviceId();
			String propertyName = controllableProperty.getProperty();
			Object propertyValue = controllableProperty.getValue();

			String[] controllerParts = propertyName.split(Constant.HASH_SYMBOL);
			String[] groupParts = controllerParts[0].split(Constant.UNDERSCORE);
			String groupName = groupParts[0];
			String propertyNameUngrouped = controllerParts[1];

			if (propertyNameUngrouped.endsWith("Reboot") && availableRooms != null) {
				Room room = availableRooms.get(controllerParts[0]);
				this.rebootRoom(room.getId());
				return;
			}

			if (!groupName.equals(Constant.AGGREGATED_SETTINGS_GROUP)) {
				this.logger.warn("Cannot define the controllable property: " + controllableProperty.getProperty());
				return;
			}
			boolean applySettings = SettingProperty.APPLY.getName().equals(propertyNameUngrouped);
			boolean cancelSettings = SettingProperty.CANCEL.getName().equals(propertyNameUngrouped);

			if (!applySettings && !cancelSettings) {
				String propertyApiField = denormalizeSettingPropertyName(propertyNameUngrouped);
				String settingValue = controllableProperty.getValue().toString();

				Optional<SettingsRequest> settingsCache = this.updatedSettingsCaches.stream()
						.filter(c -> c.getDeviceId().equals(deviceId)).findFirst();

				boolean requiresRestart = checkControlPropertyRequiresRestart(deviceId, propertyApiField);
				if (settingsCache.isPresent()) {
					settingsCache.get().getSettings().put(propertyNameUngrouped, new OptionDetail(settingValue, requiresRestart));
				} else {
					SettingsRequest settingsRequest = new SettingsRequest(
							deviceId, System.currentTimeMillis() + UPDATED_SETTINGS_CACHE_EXPIRY_TIME,
							Collections.singletonMap(propertyApiField, new OptionDetail(settingValue, requiresRestart))
					);

					this.updatedSettingsCaches.add(settingsRequest);
				}
				updateLocalControllableProperty(deviceId, propertyName, propertyValue);
				return;
			}
			if (applySettings) {
				String url = String.format(ApiConstant.DEVICE_SETTINGS_ENDPOINT, deviceId);
				for (SettingsRequest settingsRequest : this.updatedSettingsCaches) {
					if (settingsRequest.getDeviceId().equals(controllableProperty.getDeviceId())) {
						this.applySettings(url, settingsRequest.getRequest());
						this.updatedSettingsCaches.remove(settingsRequest);
						break;
					}
				}
			} else {
				this.updatedSettingsCaches.removeIf(cache -> cache.getDeviceId().equals(controllableProperty.getDeviceId()));
			}
			updateDeviceSettingsMode();
		} finally {
			this.reentrantLock.unlock();
			this.lastControlActivationTimestamp = System.currentTimeMillis();
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
		headers.set(ApiConstant.API_VERSION_HEADER, this.apiVersion);
		headers.setContentType(MediaType.APPLICATION_JSON);

		return super.putExtraRequestHeaders(httpMethod, uri, headers);
	}

	@Override
	protected void internalDestroy() {
		this.logger.info(Constant.DESTROY_INTERNAL_INFO + this);

		this.displayPropertyGroups = null;
		this.retrievalIntervals = null;
		this.updatedSettingsCaches = null;
		this.rooms = null;
		this.devicesSettings = null;
		this.featureModelSettingsValuespace = null;
		this.deviceIdFeatureModelSettingsValuespace = null;
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


    @Override
    protected RestTemplate obtainRestTemplate() throws Exception {
        RestTemplate restTemplate = super.obtainRestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
		List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();

        if (!interceptors.contains(jabraCloudRequestInterceptor)) {
			interceptors.add(jabraCloudRequestInterceptor);
		}
		if (!converters.contains(jabraSettingsHttpMessageConterter)) {
			converters.add(0, jabraSettingsHttpMessageConterter);
		}
		if (!converters.contains(jabraSettingsValuespaceHttpMessageConverter)) {
			converters.add(1, jabraSettingsValuespaceHttpMessageConverter);
		}
        return restTemplate;
    }

	/**
	 * Check if the control requires restart of the device, using existing valuespace references
	 *
	 * @param deviceId id of the device
	 * @param propertyName denormalized and ungrouped property name to check in the valuespace map
	 * */
	private boolean checkControlPropertyRequiresRestart(String deviceId, String propertyName) {
		String valuespaceId = deviceIdFeatureModelSettingsValuespace.get(deviceId);
		if (StringUtils.isNullOrEmpty(valuespaceId)) {
			return false;
		}
		SettingsValuespace settingsValuespace = featureModelSettingsValuespace.get(valuespaceId);
		if (settingsValuespace == null) {
			return false;
		}
		SettingDescriptor descriptor = settingsValuespace.descriptors().get(propertyName);
		if (descriptor == null) {
			return false;
		}
		return ((SettingDescriptor.Valuespace)descriptor.valuespace()).requiresRestart();
	}
	/**
	 * Loads version properties and sets initial values used to create general properties for the aggregator device.
	 */
	private void loadProperties() {
		try {
			this.versionProperties.load(this.getClass().getResourceAsStream("/version.properties"));
		} catch (IOException e) {
			this.logger.error(Constant.READ_PROPERTIES_FILE_FAILED + e.getMessage());
			return;
		}
		this.versionProperties.setProperty(GeneralProperty.ADAPTER_UPTIME.getProperty(), String.valueOf(this.adapterInitializationTimestamp));
		this.versionProperties.setProperty(GeneralProperty.LAST_MONITORING_CYCLE_DURATION.getProperty(), String.valueOf(this.lastMonitoringCycleDuration));
		this.versionProperties.setProperty(GeneralProperty.MONITORED_DEVICES_TOTAL.getProperty(), String.valueOf(this.localAggregatedDevices.size()));
		try {
			this.versionProperties.setProperty(GeneralProperty.MONITORED_CYCLE_INTERVAL.getProperty(), String.valueOf(this.getMonitoringRate()));
		} catch (NoSuchMethodError error) {
			logger.warn("Unsupported feature: getMonitoringRate isn't available on current Cloud Connector version.", error);
		}
	}

	/**
	 * Verifies that the adapter properties has a valid value. <br/>
	 * If the value is valid, this method throws an {@link InvalidArgumentException}.
	 *
	 * @throws InvalidArgumentException if the params is invalid
	 */
	private void verifyAdapterProperties() {
		if (ClientTypeFilter.UNDEFINED.equals(this.clientTypeFilter)) {
			throw new InvalidArgumentException(String.format(Constant.SET_CLIENT_TYPE_FAILED, ClientTypeFilter.getValues()));
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

		if (CollectionUtils.isEmpty(this.devices) || CollectionUtils.isEmpty(this.displayPropertyGroups)) {
			return;
		}
		//	Collect data for this.rooms and this.devicesRooms
		IntervalSetting roomsInterval = this.getIntervalSettingByType(RetrievalType.ROOMS);
		if (roomsInterval.isValid() && this.shouldDisplayGroup(Constant.ROOM_GROUP)) {
			this.logger.info(String.format("Rooms retrieval is available now. %s", roomsInterval.getNextAvailabilityInfo()));
			this.devicesRooms.clear();
			this.rooms.clear();
			Set<String> groupIDs = this.devices.stream().map(Device::getGroupId).filter(Objects::nonNull).collect(Collectors.toSet());
			for (String groupId : groupIDs) {
				String url = String.format(ApiConstant.ROOMS_ENDPOINT, groupId);
				Room room = this.fetchData(url, Room.class);
				if (room == null) {
					continue;
				}

				this.rooms.add(room);
				this.devicesRooms.addAll(Optional.ofNullable(room.getDevices()).orElse(new ArrayList<>()));
			}
		}
		//	Update data for this.devices
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
			this.executorService = Executors.newFixedThreadPool(2);
			this.dataLoader = new JabraCloudDataLoader(
					this,
					this.devices, this.devicesSettings, this.featureModelSettingsValuespace, this.deviceIdFeatureModelSettingsValuespace,
					this.clientTypeFilter, this.apiPageSize, this.settingsValuespaceURLTemplate
			);
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
	 * Update cache stored controllable property value, so it's consistent throughout the multiple {@link #controlProperty) calls
	 * @param deviceId id of the device to change controllable property value for
	 * @param propertyName name of the property to change value for
	 * @param propertyValue new property value to set
	 * */
	private void updateLocalControllableProperty(String deviceId, String propertyName, Object propertyValue) {
		this.localAggregatedDevices.stream().filter(device -> device.getDeviceId().equals(deviceId)).findAny().ifPresent(device -> {
			List<AdvancedControllableProperty> controls = device.getControllableProperties();
			controls.stream().filter(controllableProperty -> propertyName.equals(controllableProperty.getName())).findAny().ifPresent(controllableProperty -> {
				controllableProperty.setValue(propertyValue);
				controllableProperty.setTimestamp(new Date());
			});
		});
	}
	/**
	 * Retrieves properties for each room and groups them accordingly.
	 * <p>Each room's properties are prefixed with a group name (e.g., JabraRoom_01, JabraRoom_02, ...).</p>
	 * <p>If no rooms are available, logs a warning and returns an empty map.</p>
	 */
	private void retrieveRoomProperties(Map<String, String> statistics, List<AdvancedControllableProperty> controls) {
		if (CollectionUtils.isEmpty(this.rooms)) {
			if (this.logger.isWarnEnabled()) {
				this.logger.warn(String.format(Constant.LIST_EMPTY_WARNING, Constant.ROOM_GROUP));
			}
			return;
		}
		for (int i = 0; i < this.rooms.size(); i++) {
			Room room = this.rooms.get(i);
			String groupName = "Room_" + normalizeJabraRoomName(room.getName());
			statistics.putAll(this.generateProperties(
					RoomProperty.values(), groupName, property -> Util.mapToRoomProperty(property, room)
			));
			availableRooms.put(groupName, room);

			if (!Constant.STATUS_DISCONNECTED.equalsIgnoreCase(room.getStatus())) {
				String rebootControlName = groupName + "#Reboot";
				statistics.put(rebootControlName, "N/A");
				controls.add(createButton(rebootControlName, "Reboot", "Rebooting", 0L));
			}
		}
	}

	/**
	 * Replace all special character in the string with _, also cutting down any consecutive entries to a single one.
	 * Leading and tailing _ are also removed.
	 *
	 * @param name original room name
	 * @return normalized room name
	 * */
	private String normalizeJabraRoomName(String name) {
		return name.replaceAll("[^a-zA-Z0-9.]+", "_").replaceAll("^_|_$", "");
	}
	/**
	 * Returns dynamic statistics for the given input.
	 * <p>
	 * If the input map is empty, logs a warning and returns an empty map.
	 * Otherwise, builds a map of {@code DEFAULT_GRAPHS} with values from
	 * {@code statistics}, or {@link Constant#NOT_AVAILABLE} if missing.
	 * </p>
	 *
	 * @param statistics the input statistics
	 * @return a map of default graphs and their values, or an empty map if none
	 */
	private Map<String, String> getDynamicStatistics(Map<String, String> statistics) {
		if (MapUtils.isEmpty(statistics)) {
			this.logger.warn(Constant.STATISTICS_EMPTY_WARNING);
			return Collections.emptyMap();
		}

		Map<String, String> dynamicStatistic = new HashMap<>();
		DEFAULT_GRAPH_PROPERTIES.forEach(defaultGraph -> {
			String statisticValue = Optional.ofNullable(statistics.get(defaultGraph)).orElse(Constant.NOT_AVAILABLE);

			dynamicStatistic.put(defaultGraph, statisticValue);
		});

		return dynamicStatistic;
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
	 * Returns a map of setting properties and their resolved values for a given device.
	 * <p>
	 * - If the device is {@code null}, a warning is logged and an empty map is returned. <br>
	 * - If the device is unsupported, properties are retrieved from {@code unsupportedDevicesSettings}. <br>
	 * - If no {@link Settings} exist or they are invalid, an empty map is returned. <br>
	 * - Otherwise, properties are built from {@link Settings} and the updated settings cache. <br>
	 * - {@code DYNAMIC_COMPOSITION} controls whether additional properties such as
	 *   {@code AUTOMATIC_ZOOM_MODE}, {@code AUTOMATIC_ZOOM_SPEED}, {@code SETTINGS_REVERT_TO_DEFAULT},
	 *   {@code FIELD_OF_VIEW}, and {@code VIDEO_STITCHING} are included. <br>
	 * - {@code SAFETY_CAPACITY_NOTIFICATION} is always included. <br>
	 * - If the updated settings cache is not empty, {@code APPLY} and {@code CANCEL}
	 *   are also added, marked as {@link Constant#NOT_AVAILABLE} when the device is connected.
	 * </p>
	 *
	 * @param device the target device
	 * @param properties device properties
	 * @param controllableProperties device controls
	 */
	private void processSettingsProperties(Device device, Map<String, String> properties, List<AdvancedControllableProperty> controllableProperties) {
		if (device == null) {
			if (this.logger.isWarnEnabled()) {
				this.logger.warn(String.format(Constant.OBJECT_EMPTY_WARNING, "device"));
			}
			return;
		}
		String deviceId = device.getId();
		List<Setting> settings = this.devicesSettings.get(deviceId);
		if (settings == null || settings.isEmpty()) {
			return;
		}

		String featureModelId = deviceIdFeatureModelSettingsValuespace.get(deviceId);
		if (featureModelId == null) {
			return;
		}
		SettingsValuespace settingsValuespace = featureModelSettingsValuespace.get(featureModelId);
		if (settingsValuespace == null) {
			return;
		}
		Map<String, SettingDescriptor> settingsDescriptors = settingsValuespace.descriptors();

		for(Setting setting: settings) {
			Map.Entry<String, String> entry = generateSettingsEntry(setting);
			properties.put(entry.getKey(), entry.getValue());
			if (!this.configManagement) {
				continue;
			}

			SettingDescriptor settingDescriptor;
			switch (setting.getType()) {
				case TOGGLE:
					addDeviceControl(controllableProperties, createSwitch(entry.getKey(), (boolean)setting.getValue() ? 1 : 0));
					break;
				case DROPDOWN:
                    String settingValue = setting.getValue().toString();
                    settingDescriptor = settingsDescriptors.get(setting.getName());
                    if (settingDescriptor == null) {
						if (logger.isWarnEnabled()) {
							logger.warn(String.format("Unable to process %s property values: missing settings descriptor.", setting.getName()));
						}
						break;
					}
					SettingDescriptor.DropdownValuespace dropdownValuespace = (SettingDescriptor.DropdownValuespace) settingDescriptor.valuespace();

                    Set<String> keys = new LinkedHashSet<>(dropdownValuespace.options());
                    Set<String> values = new LinkedHashSet<>(dropdownValuespace.options());
					addDeviceControl(controllableProperties, createDropdown(entry.getKey(), keys, values, settingValue));
					break;
				case TEXT:
					addDeviceControl(controllableProperties, createText(entry.getKey(), setting.getValue().toString()));
					break;
				case NUMERIC:
					settingDescriptor = settingsDescriptors.get(setting.getName());
					if (settingDescriptor == null) {
						addDeviceControl(controllableProperties, createNumeric(entry.getKey(), setting.getValue().toString()));
					} else {
						SettingDescriptor.NumberValuespace numberValuespace = (SettingDescriptor.NumberValuespace) settingDescriptor.valuespace();
						addDeviceControl(controllableProperties, createSlider(entry.getKey(), (float) numberValuespace.min(), (float) numberValuespace.max(), Float.valueOf(setting.getValue().toString())));
					}
					break;
				default:
					logger.warn(String.format("Unable to process device setting: no setting of type %s is supported.", setting.getType()));
					break;
			}
		}
	}

	/**
	 * Check if the device has any not applied settings and show Apply/Cancel buttons
	 * This method operates with cached version of aggregated devices - {@link #localAggregatedDevices}
	 *
	 * @since 1.1.1
	 * */
	private void updateDeviceSettingsMode(){
		for (AggregatedDevice device : this.localAggregatedDevices) {
			String deviceId = device.getDeviceId();
			Map<String, OptionDetail> pendingChanges = this.getUpdatedSettingsCacheByDeviceId(deviceId);
			Map<String, String> properties = device.getProperties();
			List<AdvancedControllableProperty> controls = device.getControllableProperties();
			try {
				String applyKey = String.format(Constant.PROPERTY_FORMAT, Constant.AGGREGATED_SETTINGS_GROUP, SettingProperty.APPLY.getName());
				String cancelKey = String.format(Constant.PROPERTY_FORMAT, Constant.AGGREGATED_SETTINGS_GROUP, SettingProperty.CANCEL.getName());
				if (pendingChanges.isEmpty()) {
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("No pending changes detected, skipping device settings mode update for device %s.", deviceId));
					}
					properties.remove(applyKey);
					properties.remove(cancelKey);
					controls.removeIf(controllableProperty -> applyKey.equalsIgnoreCase(controllableProperty.getName()) || cancelKey.equalsIgnoreCase(controllableProperty.getName()));
					continue;
				}
				boolean requiresRestart = pendingChanges.values().stream().anyMatch(OptionDetail::requiresRestart);

				properties.put(applyKey, "N/A");
				// 60s gracePeriod because new controls cant be applied unless 1 minute has passed.
				addDeviceControl(controls, createButton(applyKey, "Apply", "Applying", requiresRestart ? SETTING_UPDATE_TIME : 60000L));

				properties.put(cancelKey, "N/A");
				addDeviceControl(controls, createButton(cancelKey, "Cancel", "Canceling", 0L));
			} finally {
				device.setControllableProperties(controls);
				device.setTimestamp(System.currentTimeMillis());
			}
		}
	}

	/**
	 * Create dropdown controllable property
	 *
	 * @param name of the controllable property
	 * @param labels set of labels (for UI)
	 * @param options set of options (for API)
	 * @param initialValue value of the control
	 *
	 * @return built instance of {@link AdvancedControllableProperty}
	 * */
	private AdvancedControllableProperty createDropdown(String name, Set<String> labels, Set<String> options, String initialValue) {
		AdvancedControllableProperty.DropDown dropDown = new AdvancedControllableProperty.DropDown();
		dropDown.setOptions(options.toArray(new String[0]));
		dropDown.setLabels(labels.toArray(new String[0]));
		return new AdvancedControllableProperty(name, new Date(), dropDown, initialValue);
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
	 * Generate Settings properties entry based on {@link Setting}
	 *
	 * @param setting instance to create property name for
	 * @return {@link Map.Entry} of a given setting
	 *
	 * @since 1.1.1
	 * */
	private Map.Entry<String, String> generateSettingsEntry(Setting setting) {
		String propertyName = String.format(Constant.PROPERTY_FORMAT, Constant.AGGREGATED_SETTINGS_GROUP, normalizeSettingPropertyName(setting.getName()));
		return Map.entry(propertyName, setting.getValue().toString());
	}

	/**
	 * Normalize Setting property name, from camelCase to PascalCase (according to SY naming standards)
	 *
	 * @param name original string
	 * @return modified {@link String}
	 * @since 1.1.1
	 * */
	private String normalizeSettingPropertyName(String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	/**
	 * Denormalize Setting property name, from PascalCase to camelCase (according to Jabra+ naming standards)
	 *
	 * @param name Symphony-friendly string format
	 * @return modified {@link String}
	 * @since 1.1.1
	 * */
	private String denormalizeSettingPropertyName(String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}
		return Character.toLowerCase(name.charAt(0)) + name.substring(1);
	}

	/**
	 * Add controllable property to a device to preserve uniqueness in a centralized way. We need to update multiple fields (type as well, since it may change)
	 * so this approach is cleaner than just updating value/type selectively
	 *
	 * @param controls to add controllable property to
	 * @param control to add to controllable properties list
	 * @since 1.1.1
	 * */
	private void addDeviceControl(List<AdvancedControllableProperty> controls, AdvancedControllableProperty control) {
		controls.removeIf(c -> control.getName().equalsIgnoreCase(c.getName()));
		controls.add(control);
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
	 * Returns the IntervalSetting for the given type, creating one if absent.
	 * Guarantees a non-null entry so callers don't need null checks.
	 */
	IntervalSetting getIntervalSettingByType(RetrievalType type) {
		return retrievalIntervals.computeIfAbsent(type, t -> new IntervalSetting());
	}

	/**
	 * Reboot Jabra room. Processed asynchronously so that timeout issues are not escalated
	 * @param roomId id of the room to reboot
	 * @throws Exception when any error occurs
	 * */
	private void rebootRoom(String roomId) throws Exception {
		String requestUrl = String.format(ApiConstant.ROOMS_REBOOT_ENDPOINT, roomId);
		try {
			CompletableFuture.runAsync(() -> {
				try {
					doPost(requestUrl, JsonNode.class);
				} catch (Exception e) {
					this.logger.error("Unable to process room reboot request.", e);
				}
			}, executorService);
		} catch (RejectedExecutionException e) {
			throw new RuntimeException(String.format("Unable to request reboot operation for room with ID %s: another reboot operation is in progress. Please try again later.", roomId));
		}
	}
	/**
	 * Checks whether the specified property group is configured to be displayed.
	 *
	 * @param groupName the name of the property group to check
	 * @return {@code true} if the group is configured to be displayed; {@code false} otherwise
	 */
	public boolean shouldDisplayGroup(String groupName) {
		return (CollectionUtils.isNotEmpty(this.displayPropertyGroups) && this.displayPropertyGroups.contains(groupName))
                || this.displayPropertyGroups.contains("All");
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
		} catch (FailedLoginException | ResourceNotReachableException e) {
			throw e;
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
	<T> T fetchData(String endpoint, String indicatedField, TypeReference<T> typeReference) throws FailedLoginException {
		String typeReferenceName = typeReference.getType().getTypeName();
		try {
			this.requestStateHandler.pushRequest(endpoint);

			List<Object> aggregatedItems = null;
			String currentEndpoint = endpoint;
			boolean isList = typeReference.getType() instanceof ParameterizedType
					&& List.class.isAssignableFrom(
					(Class<?>) ((ParameterizedType) typeReference.getType()).getRawType());

			while (true) {
				JsonNode response = super.doGet(currentEndpoint, JsonNode.class);

				if (Objects.isNull(response)) {
					if (this.logger.isWarnEnabled()) {
						this.logger.warn(String.format(Constant.FETCHED_DATA_NULL_WARNING, currentEndpoint, typeReferenceName));
					}
					break;
				}

				JsonNode dataNode = this.objectMapper.readTree(response.toString()).get(indicatedField);
				T mappedResponse = this.objectMapper.readValue(dataNode.toString(), typeReference);

				JsonNode tokenNode = response.at("/continuationToken");
				String continuationToken = tokenNode.isMissingNode() || tokenNode.isNull()
						? null
						: tokenNode.asText();

				if (continuationToken == null || !isList) {
					if (isList && aggregatedItems != null) {
						aggregatedItems.addAll((List<?>) mappedResponse);
						this.requestStateHandler.resolveError(endpoint);
						return (T) aggregatedItems;
					}
					this.requestStateHandler.resolveError(endpoint);
					return mappedResponse;
				}

				if (aggregatedItems == null) {
					aggregatedItems = new ArrayList<>((List<?>) mappedResponse);
				} else {
					aggregatedItems.addAll((List<?>) mappedResponse);
				}

				currentEndpoint = appendContinuationToken(endpoint, continuationToken);
			}

			this.requestStateHandler.resolveError(endpoint);
			return isList && aggregatedItems != null ? (T) aggregatedItems : null;
		} catch (FailedLoginException | ResourceNotReachableException e) {
			throw e;
		} catch (Exception e) {
			this.requestStateHandler.pushError(endpoint, e);
			this.logger.error(String.format(Constant.FETCH_DATA_FAILED, endpoint), e);
			return null;
		}
	}

	/**
	 * Package-private fetchData method overload to use in DataLoader
	 *
	 * @param endpoint uri to request
	 * @param reference return type
	 * @return T return type based on reference argument
	 * */
	<T> T fetchData(String endpoint, ParameterizedTypeReference<T> reference) throws Exception {
		return doGet(endpoint, reference);
	}
	/**
	 * Append continuationToken query string parameter to the origin endpoint
	 * @param endpoint endpoint to attach continuationToken to
	 * @param continuationToken token value to attach to the endpoint
	 *
	 * @return String value of an endpoint, with continuation token included
	 * */
	private String appendContinuationToken(String endpoint, String continuationToken) {
		String separator = endpoint.contains("?") ? "&" : "?";
		return endpoint + separator + "continuationToken=" + continuationToken;
	}

	/**
	 * Performs a control operation on the given URI with the provided request body.
	 * <p>Logs and throws meaningful exceptions for failure scenarios.</p>
	 *
	 * @param endpoint the target URI to send the request to
	 * @param requestBody the request payload to be sent
	 */
	private void applySettings(String endpoint, Object requestBody) throws Exception {
		this.doPatch(endpoint, requestBody, Object.class);
	}

	/**
	 * Define device category by product name
	 * The category names defined by Jabra may be qute specific, for instance
	 * > Jabra PanaCast Control (Teams)
	 * > Jabra PanaCast Control
	 * So we're using a set of keywords here to match this to categories that Symphony has in its catalog.
	 *
	 * @param productName name of product provided by Jabra+ API
	 * @return String value of Symphony category
	 * */
	private String defineDeviceCategory(String productName) {
		if (StringUtils.isNullOrEmpty(productName)) {
			logger.warn("Unable to define device category: product name is null");
			return Constant.CATEGORY_GENERIC;
		}
		String name = productName.toLowerCase();
		for(Map.Entry<String, String> entry: Constant.PRODUCT_KEYWORD_TO_CATEGORY_MATCH.entrySet()) {
			if (name.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		return Constant.CATEGORY_GENERIC;
	}
}
