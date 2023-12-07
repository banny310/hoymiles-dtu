package com.hoymiles.infrastructure.repository.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class MetRealDataDTO {
    
    @SerializedName("meter_sn")
    private String sn;

    @SerializedName("meter_type")
    private int type;            // meter type

    @SerializedName("meter_ct")
    private String ctAmps;       // CT Amps

    @SerializedName("meter_power_a_w")
    private float powerA;        // Phase A Power (x1W)

    @SerializedName("meter_power_b_w")
    private float powerB;        // Phase B Power (x1W)

    @SerializedName("meter_power_c_w")
    private float powerC;        // Phase C Power (x1W)

    @SerializedName("meter_power_total_w")
    private float powerTotal;    // Total Power (x1W)

    @SerializedName("meter_power_a_kw")
    private float powerAKWh;        // Phase A Power (x1W)

    @SerializedName("meter_power_b_kw")
    private float powerBKWh;        // Phase B Power (x1W)

    @SerializedName("meter_power_c_kw")
    private float powerCKWh;        // Phase C Power (x1W)

    @SerializedName("meter_power_total_kw")
    private float powerTotalKWh;    // Total Power (x1W)

    @SerializedName("meter_u_a")
    private float voltageA;      // Phase A Voltage (x1V)

    @SerializedName("meter_u_b")
    private float voltageB;      // Phase B Voltage (x1V)

    @SerializedName("meter_u_c")
    private float voltageC;      // Phase V Voltage (x1V)

    @SerializedName("meter_i_a")
    private float currentA;      // Phase A Current (x1A)

    @SerializedName("meter_i_b")
    private float currentB;      // Phase B Current (x1A)

    @SerializedName("meter_i_c")
    private float currentC;      // Phase C Current (x1A)

    @SerializedName("meter_energy_import_a_wh")
    private float energyImportedA;  

    @SerializedName("meter_energy_import_b_wh")
    private float energyImportedB;  

    @SerializedName("meter_energy_import_c_wh")
    private float energyImportedC;  

    @SerializedName("meter_energy_import_total_wh")
    private float energyImportedTotal; 

    @SerializedName("meter_energy_import_a_kwh")
    private float energyImportedAKWh;  

    @SerializedName("meter_energy_import_b_kwh")
    private float energyImportedBKWh;  

    @SerializedName("meter_energy_import_c_kwh")
    private float energyImportedCKWh;  

    @SerializedName("meter_energy_import_total_kwh")
    private float energyImportedTotalKWh; 

    @SerializedName("meter_energy_export_a_wh")
    private float energyExportedA;  

    @SerializedName("meter_energy_export_b_wh")
    private float energyExportedB;  

    @SerializedName("meter_energy_export_c_wh")
    private float energyExportedC;  

    @SerializedName("meter_energy_export_total_wh")
    private float energyExportedTotal;  

    @SerializedName("meter_energy_export_a_kwh")
    private float energyExportedAKWh;  

    @SerializedName("meter_energy_export_b_kwh")
    private float energyExportedBKWh;  

    @SerializedName("meter_energy_export_c_kwh")
    private float energyExportedCKWh;  

    @SerializedName("meter_energy_export_total_kwh")
    private float energyExportedTotalKWh;  

    @SerializedName("meter_power_factor_a")
    private float powerFactorA;  // Phase A Power Factor (0.0 - 1.0)

    @SerializedName("meter_power_factor_b")
    private float powerFactorB;  // Phase B Power Factor (0.0 - 1.0)

    @SerializedName("meter_power_factor_c")
    private float powerFactorC;  // Phase C Power Factor (0.0 - 1.0)

    @SerializedName("meter_power_factor_total")
    private float powerFactorTotal;  // Total Power Factor (0.0 - 1.0)

    @SerializedName("last_seen")
    private Date lastSeen;
}
