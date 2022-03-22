package com.hoymiles.infrastructure.repository.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class PvRealDataDTO {
    @SerializedName("inverter_sn")
    private String sn;

    @SerializedName("port")
    private int port;           // inverter port number

    @SerializedName("voltage")
    private float voltage;      // voltage (x10V)

    @SerializedName("current")
    private float current;      // current (x100A)

    @SerializedName("power")
    private float power;        // power (x10W)

    @SerializedName("energy_today")
    private int energyToday;    // energy daily (Wh)

    @SerializedName("energy_total")
    private int energyTotal;    // energy total (Wh)

    @SerializedName("last_seen")
    private Date lastSeen;
}
