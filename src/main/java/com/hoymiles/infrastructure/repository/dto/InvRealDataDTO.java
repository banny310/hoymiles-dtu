package com.hoymiles.infrastructure.repository.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class InvRealDataDTO {
    @SerializedName("inverter_sn")
    private String sn;

    @SerializedName("grid_voltage")
    private float gridVoltage;

    @SerializedName("grid_frequency")
    private float gridFreq;

    @SerializedName("grid_power")
    private float gridPower;

    @SerializedName("grid_reactive_power")
    private float gridReactivePower;

    @SerializedName("grid_current")
    private float gridCurrent;

    @SerializedName("power_factor")
    private float powerFactor;

    @SerializedName("temperature")
    private float temp;

    @SerializedName("last_seen")
    private Date lastSeen;
}
