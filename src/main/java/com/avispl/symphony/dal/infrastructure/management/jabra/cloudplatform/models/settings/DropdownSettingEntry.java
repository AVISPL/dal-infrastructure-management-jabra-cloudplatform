package com.avispl.symphony.dal.infrastructure.management.jabra.cloudplatform.models.settings;

public enum DropdownSettingEntry {
    FULL_SCREEN("None", "fullScreen"),
    INTELLIGENT_ZOOM("Intelligent Zoom", "intelligentZoom"),
    ACTIVE_SPEAKER("Virtual Director", "activeSpeaker"),

    SLOWEST("Slowest", "slowest"),
    SLOWER("Slower", "slower"),
    MEDIUM("Medium", "medium"),
    FASTER("Faster", "faster"),
    FASTEST("Fastest", "fastest"),

    OFF("Panoramic mode", "off"),
    MODE_1("Single-stream", "mode1"),
    MODE_2("Multi-stream for Microsoft Teams Rooms", "mode2"),

    D90("90", "_90deg"),
    D120("120", "_120deg"),
    D140("140", "_140deg"),
    D180("180", "_180deg"),

    ALWAYS("Always", "always"),
    WHEN_VIDEOS_IS_ENABLE("Only during a video conference", "whenVideoIsEnabled"),
    EN_CALL("When a video conference ends", "endCall"),
    PC_UNPLUG("When a new computer is connected", "pcUnplug"),
    BLEND("Blend", "blend"),
    HYBRID("Hybrid", "hybrid");

    private final String name;
    private final String value;

    DropdownSettingEntry(String name, String value) {
        this.name = name;
        this.value = value;
    }
    /**
     * Retrieves {@link #name}
     *
     * @return value of {@link #name}
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves {@link #value}
     *
     * @return value of {@link #value}
     */
    public String getValue() {
        return value;
    }
}
