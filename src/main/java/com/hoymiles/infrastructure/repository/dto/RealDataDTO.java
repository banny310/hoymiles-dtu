package com.hoymiles.infrastructure.repository.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class RealDataDTO {
    @SerializedName("power_total_w")
    private float powerTotalW;

    @SerializedName("power_total_kw")
    private float powerTotalKW;

    @SerializedName("energy_today_wh")
    private int energyTodayWh;

    @SerializedName("energy_today_kwh")
    private float energyTodayKWh;

    @SerializedName("energy_total_wh")
    private int energyTotalWh;

    @SerializedName("energy_total_kwh")
    private float energyTotalKWh;

    @SerializedName("last_seen")
    private Date lastSeen;
}
