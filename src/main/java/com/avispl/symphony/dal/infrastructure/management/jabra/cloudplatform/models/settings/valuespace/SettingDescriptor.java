package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.valuespace;

import java.util.List;

public record SettingDescriptor(Object valuespace) {
    public record DropdownValuespace(
            List<String> options
    ) {}

    public record NumberValuespace(
            int min,
            int max
    ) {}

    public record TextValuespace(
            int minLength,
            int maxLength
    ) {}
}
