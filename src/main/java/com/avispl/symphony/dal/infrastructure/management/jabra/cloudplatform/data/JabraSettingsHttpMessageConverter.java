package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.data;

import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.Setting;
import com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.SettingType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JabraSettingsHttpMessageConverter implements GenericHttpMessageConverter<List<Setting>> {
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
    public List<Setting> read(Type type, @Nullable Class<?> contextClass,
                              HttpInputMessage inputMessage) throws IOException {
        return parseSettings(inputMessage);
    }

    @Override
    public boolean canWrite(@Nullable Type type, Class<?> clazz, @Nullable MediaType mediaType) {
        return type != null && isSettingList(type) && isJsonCompatible(mediaType);
    }

    @Override
    public void write(List<Setting> settings, @Nullable Type type, @Nullable MediaType contentType,
                      HttpOutputMessage outputMessage) throws IOException {
        // TODO: implement if needed
    }

    // ── HttpMessageConverter (raw class) ─────────────────────────────

    @Override
    public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
        return false; // defer to the Type-aware overload
    }

    @Override
    public List<Setting> read(Class<? extends List<Setting>> clazz,
                              HttpInputMessage inputMessage) throws IOException {
        return parseSettings(inputMessage);
    }

    @Override
    public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
        return false; // defer to the Type-aware overload
    }

    @Override
    public void write(List<Setting> settings, @Nullable MediaType contentType,
                      HttpOutputMessage outputMessage) throws IOException {
        // TODO: implement if needed
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return SUPPORTED_MEDIA_TYPES;
    }

    // ── Parsing logic ────────────────────────────────────────────────

    private List<Setting> parseSettings(HttpInputMessage inputMessage) throws IOException {
        JsonNode root = objectMapper.readTree(inputMessage.getBody());
        JsonNode settingsNode = root.get("settings");

        if (settingsNode == null || !settingsNode.isObject()) {
            throw new HttpMessageNotReadableException(
                    "Expected a 'settings' object at the root", inputMessage);
        }

        List<Setting> settings = new ArrayList<>();

        Iterator<Map.Entry<String, JsonNode>> fields = settingsNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String name = entry.getKey();
            JsonNode props = entry.getValue();

            Setting setting = new Setting();
            setting.setName(name);

            if (props.has("isOn")) {
                setting.setType(SettingType.TOGGLE);
                setting.setValue(props.get("isOn").asBoolean());
            } else if (props.has("selected")) {
                setting.setType(SettingType.DROPDOWN);
                setting.setValue(props.get("selected").asText());
            } else if (props.has("value")) {
                JsonNode valueNode = props.get("value");
                if (valueNode.isNumber()) {
                    setting.setType(SettingType.NUMERIC);
                    setting.setValue(valueNode.numberValue());
                } else {
                    setting.setType(SettingType.TEXT);
                    setting.setValue(valueNode.asText());
                }
            }

            settings.add(setting);
        }

        return settings;
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private boolean isSettingList(Type type) {
        if (type instanceof ParameterizedType pt) {
            return pt.getRawType() == List.class
                    && pt.getActualTypeArguments().length == 1
                    && pt.getActualTypeArguments()[0] == Setting.class;
        }
        return false;
    }

    private boolean isJsonCompatible(@Nullable MediaType mediaType) {
        return mediaType == null || mediaType.isCompatibleWith(MediaType.APPLICATION_JSON);
    }
}