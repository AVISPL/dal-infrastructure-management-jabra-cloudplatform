package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.valuespace;

import java.util.Map;

public record SettingsValuespace(Map<String, SettingDescriptor> descriptors) {}
