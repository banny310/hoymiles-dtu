package com.hoymiles.domain.model.ha;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class Sensor {
    @SerializedName("availability")
    private List<Availability> availability;
    @SerializedName("device")
    private Device device;
    @SerializedName("enabled_by_default")
    private boolean enabledByDefault;
    @SerializedName("icon")
    private String icon;
    @SerializedName("json_attributes_topic")
    private String jsonAttributesTopic;
    @SerializedName("name")
    private String name;
    @SerializedName("state_class")
    private String stateClass;
    @SerializedName("device_class")
    private String deviceClass;
    @SerializedName("state_topic")
    private String stateTopic;
    @SerializedName("unique_id")
    private String uniqueId;
    @SerializedName("unit_of_measurement")
    private String unitOfMeasurement;
    @SerializedName("value_template")
    private String valueTemplate;

    @Getter
    @Builder
    public static class Availability {
        private String topic;
    }

    @Getter
    @Builder
    public static class Device {
        @SerializedName("identifiers")
        private List<String> identifiers;
        @SerializedName("manufacturer")
        private String manufacturer = "Hoymiles";
        @SerializedName("model")
        private String model;
        @SerializedName("name")
        private String name;
        @SerializedName("sw_version")
        private String swVersion;
    }
}
