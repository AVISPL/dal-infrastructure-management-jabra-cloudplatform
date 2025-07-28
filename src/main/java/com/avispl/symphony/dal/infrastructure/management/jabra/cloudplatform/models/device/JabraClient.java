/*
 * Copyright (c) 2025 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.device;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents client information of the {@link Device}.
 *
 * @author Kevin / Symphony Dev Team
 * @since 1.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JabraClient {
	private String client;
	private String clientName;
	private String clientType;
	private String clientVersion;
	private Boolean autoUpdate;

	public JabraClient() {
		//	Default constructor required for JSON deserialization.
	}

	/**
	 * Retrieves {@link #client}
	 *
	 * @return value of {@link #client}
	 */
	public String getClient() {
		return client;
	}

	/**
	 * Sets {@link #client} value
	 *
	 * @param client new value of {@link #client}
	 */
	public void setClient(String client) {
		this.client = client;
	}

	/**
	 * Retrieves {@link #clientName}
	 *
	 * @return value of {@link #clientName}
	 */
	public String getClientName() {
		return clientName;
	}

	/**
	 * Sets {@link #clientName} value
	 *
	 * @param clientName new value of {@link #clientName}
	 */
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	/**
	 * Retrieves {@link #clientType}
	 *
	 * @return value of {@link #clientType}
	 */
	public String getClientType() {
		return clientType;
	}

	/**
	 * Sets {@link #clientType} value
	 *
	 * @param clientType new value of {@link #clientType}
	 */
	public void setClientType(String clientType) {
		this.clientType = clientType;
	}

	/**
	 * Retrieves {@link #clientVersion}
	 *
	 * @return value of {@link #clientVersion}
	 */
	public String getClientVersion() {
		return clientVersion;
	}

	/**
	 * Sets {@link #clientVersion} value
	 *
	 * @param clientVersion new value of {@link #clientVersion}
	 */
	public void setClientVersion(String clientVersion) {
		this.clientVersion = clientVersion;
	}

	/**
	 * Retrieves {@link #autoUpdate}
	 *
	 * @return value of {@link #autoUpdate}
	 */
	public Boolean getAutoUpdate() {
		return autoUpdate;
	}

	/**
	 * Sets {@link #autoUpdate} value
	 *
	 * @param autoUpdate new value of {@link #autoUpdate}
	 */
	public void setAutoUpdate(Boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}
}
