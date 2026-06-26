package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings.valuespace;

import java.util.List;

public record SettingDescriptor(Object valuespace) {

    public sealed interface Valuespace permits DropdownValuespace, NumberValuespace, TextValuespace {
        boolean requiresRestart();
    }
    public record DropdownValuespace(
            boolean requiresRestart,
            List<String> options
    ) implements Valuespace {}

    public record NumberValuespace(
            boolean requiresRestart,
            int min,
            int max
    ) implements Valuespace {}

    public record TextValuespace(
            boolean requiresRestart,
            int minLength,
            int maxLength
    ) implements Valuespace {}
}
