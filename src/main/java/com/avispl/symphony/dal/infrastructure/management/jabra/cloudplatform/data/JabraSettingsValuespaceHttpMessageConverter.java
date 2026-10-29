/*
 * Copyright (c) 2026 AVI-SPL, Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.data;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.valuespace.SettingDescriptor;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.valuespace.SettingsValuespace;
import com.avispl.symphony.dal.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Process json response of Jabra+ settings valuespaces to {@link SettingsValuespace}
 *
 * @since 1.1.1
 * @author Maksym.Rossiitsev/AVISPL Team
 * */
public class JabraSettingsValuespaceHttpMessageConverter implements GenericHttpMessageConverter<SettingsValuespace> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final List<MediaType> SUPPORTED_MEDIA_TYPES = List.of(
            MediaType.APPLICATION_JSON,
            new MediaType("application", "json", StandardCharsets.UTF_8)
    );

    // ── GenericHttpMessageConverter (parameterized type) ──────────────

    @Override
    public boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
        return isSettingList(type) && isJsonCompatible(mediaType);
    }

    @Override
    public SettingsValuespace read(Type type, @Nullable Class<?> contextClass,
                              HttpInputMessage inputMessage) throws IOException {
        return parseSettings(inputMessage);
    }

    @Override
    public boolean canWrite(@Nullable Type type, Class<?> clazz, @Nullable MediaType mediaType) {
        return type != null && isSettingList(type) && isJsonCompatible(mediaType);
    }

    @Override
    public void write(SettingsValuespace settings, @Nullable Type type, @Nullable MediaType contentType,
                      HttpOutputMessage outputMessage) throws IOException {
    }

    // ── HttpMessageConverter (raw class) ─────────────────────────────

    @Override
    public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
        return false; // defer to the Type-aware overload
    }

    @Override
    public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
        return false; // defer to the Type-aware overload
    }

    @Override
    public void write(SettingsValuespace settings, @Nullable MediaType contentType,
                      HttpOutputMessage outputMessage) throws IOException {
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {

        return SUPPORTED_MEDIA_TYPES;
    }

    @Override
    public SettingsValuespace read(Class<? extends SettingsValuespace> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return parseSettings(inputMessage);
    }

    // ── Parsing logic ────────────────────────────────────────────────
    private SettingsValuespace parseSettings(HttpInputMessage inputMessage) throws IOException {
        JsonNode root = objectMapper.readTree(inputMessage.getBody());

        JsonNode settingsNodeRaw = root.get("settings");
        JsonNode settingIdsRaw = root.get("settingIds");

        if (settingsNodeRaw == null || !settingsNodeRaw.isObject()) {
            throw new HttpMessageNotReadableException(
                    "Expected a 'settings' object at the root", inputMessage);
        }
        if (settingIdsRaw == null || !settingIdsRaw.isObject()) {
            throw new HttpMessageNotReadableException(
                    "Expected a 'settingIds' object at the root", inputMessage);
        }

        Map<String, String> settingIdsReverse = new HashMap<>();

        Iterator<String> jsonNodeIterator = settingIdsRaw.fieldNames();
        while(jsonNodeIterator.hasNext()) {
            String fieldName = jsonNodeIterator.next();
            settingIdsReverse.put(settingIdsRaw.get(fieldName).asText(), fieldName);
        }


        Map<String, SettingDescriptor> settingDescriptorMap = new HashMap<>();
        collectDescriptors(settingsNodeRaw, settingIdsReverse, settingDescriptorMap);

        JsonNode compositeSettings = settingsNodeRaw.path("compositeSettings");
        if (compositeSettings.isArray()) {
            for (JsonNode compositeSetting : compositeSettings) {
                collectDescriptors(compositeSetting, settingIdsReverse, settingDescriptorMap);
            }
        }
        return new SettingsValuespace(settingDescriptorMap);
    }

    /**
     * Adds descriptors for the listSettings, stringSettings and numberSettings arrays of a settings container.
     * Entries with an unknown settingId, or string and number entries without both bounds, are skipped.
     *
     * @param container node holding the listSettings, stringSettings and numberSettings arrays
     * @param settingIdsReverse map of Jabra setting IDs to setting names
     * @param target map receiving the descriptors, keyed by setting name
     */
    private void collectDescriptors(JsonNode container, Map<String, String> settingIdsReverse,
                                    Map<String, SettingDescriptor> target) {
        for (JsonNode props : container.path("listSettings")) {
            String settingName = settingIdsReverse.get(props.path("settingId").asText());
            if (StringUtils.isNullOrEmpty(settingName)) {
                continue;
            }
            List<String> supportedValues = new ArrayList<>();
            props.path("supportedValues").forEach(jsonNode -> supportedValues.add(jsonNode.asText()));
            boolean requiresRestart = props.path("requiresRestart").asBoolean();
            target.put(settingName, new SettingDescriptor(new SettingDescriptor.DropdownValuespace(requiresRestart, supportedValues)));
        }

        for (JsonNode props : container.path("stringSettings")) {
            String settingName = settingIdsReverse.get(props.path("settingId").asText());
            JsonNode minLength = props.get("minimumLength");
            JsonNode maxLength = props.get("maximumLength");
            if (StringUtils.isNullOrEmpty(settingName) || minLength == null || maxLength == null) {
                continue;
            }
            boolean requiresRestart = props.path("requiresRestart").asBoolean();
            target.put(settingName, new SettingDescriptor(new SettingDescriptor.TextValuespace(requiresRestart, minLength.asInt(), maxLength.asInt())));
        }

        for (JsonNode props : container.path("numberSettings")) {
            String settingName = settingIdsReverse.get(props.path("settingId").asText());
            JsonNode minValue = props.get("minimumValue");
            JsonNode maxValue = props.get("maximumValue");
            if (StringUtils.isNullOrEmpty(settingName) || minValue == null || maxValue == null) {
                continue;
            }
            boolean requiresRestart = props.path("requiresRestart").asBoolean();
            target.put(settingName, new SettingDescriptor(new SettingDescriptor.NumberValuespace(requiresRestart, minValue.asInt(), maxValue.asInt())));
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private boolean isSettingList(Type type) {
        return type.getTypeName().endsWith("SettingsValuespace");
    }

    private boolean isJsonCompatible(@Nullable MediaType mediaType) {
        return mediaType == null || mediaType.isCompatibleWith(MediaType.APPLICATION_JSON);
    }
}