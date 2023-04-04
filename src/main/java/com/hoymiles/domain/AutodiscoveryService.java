package com.hoymiles.domain;

import com.google.gson.Gson;
import com.hoymiles.domain.model.AppInfo;
import com.hoymiles.domain.model.RealData;
import com.hoymiles.domain.model.ha.Sensor;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Dependent
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class AutodiscoveryService {

    private final IMqttRepository mqttService;
    private final Gson gson;

    public void registerHomeAssistantAutodiscovery(@NotNull AppInfo info) {
        registerDtuAutodiscovery(info);
        registerInvAutodiscovery(info.getSgsInfo());
        if(info.getMeterInfo() != null && !info.getMeterInfo().isEmpty())
        {
                registerMetAutodiscovery(info.getMeterInfo());
        }

    }

    public void registerPvAutodiscovery(@NotNull List<RealData.PvMO> pvInfos) {
        pvInfos.forEach(pv -> {
            String pvId = String.format("pv_%s_%d", pv.getSn(), pv.getPort());

            Sensor position = newPvModelBuilder(key(pvId, "port"), pv)
                    .valueTemplate("{{ value_json.port }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(pvId, "port"), gson.toJson(position).getBytes());

            Sensor voltage = newPvModelBuilder(key(pvId, "voltage"), pv)
                    .uniqueId(key(pvId, "voltage"))
                    .stateClass("measurement")
                    .deviceClass("voltage")
                    .unitOfMeasurement("V")
                    .valueTemplate("{{ value_json.voltage }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(pvId, "voltage"), gson.toJson(voltage).getBytes());

            Sensor current = newPvModelBuilder(key(pvId, "current"), pv)
                    .uniqueId(key(pvId, "current"))
                    .stateClass("measurement")
                    .deviceClass("current")
                    .unitOfMeasurement("A")
                    .valueTemplate("{{ value_json.current }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(pvId, "current"), gson.toJson(current).getBytes());

            Sensor power = newPvModelBuilder(key(pvId, "power"), pv)
                    .uniqueId(key(pvId, "power"))
                    .icon("mdi:solar-power")
                    .stateClass("measurement")
                    .deviceClass("power")
                    .unitOfMeasurement("W")
                    .valueTemplate("{{ value_json.power }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(pvId, "power"), gson.toJson(power).getBytes());

            Sensor energyToday = newPvModelBuilder(key(pvId, "energy_today"), pv)
                    .uniqueId(key(pvId, "energy_today"))
                    .stateClass("total_increasing")
                    .deviceClass("energy")
                    .unitOfMeasurement("Wh")
                    .valueTemplate("{{ value_json.energy_today }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(pvId, "energy_today"), gson.toJson(energyToday).getBytes());

            Sensor energyTotal = newPvModelBuilder(key(pvId, "energy_total"), pv)
                    .uniqueId(key(pvId, "energy_total"))
                    .stateClass("total_increasing")
                    .deviceClass("energy")
                    .unitOfMeasurement("Wh")
                    .valueTemplate("{{ value_json.energy_total }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(pvId, "energy_total"), gson.toJson(energyTotal).getBytes());
        });
    }

    public void registerMetAutodiscovery(@NotNull List<AppInfo.MeterInfo> metInfos) {
        metInfos.forEach(met -> {
            String metId = String.format("met_%s", met.getMeterSn());

                Sensor powerAW = newMetModelBuilder("Power A (W)", met)
                        .uniqueId(key(metId, "meter_power_a_w"))
                        .objectId(key(metId, "meter_power_a_w"))
                        .icon("mdi:lightning-bolt")
                        .stateClass("measurement")
                        .deviceClass("power")
                        .unitOfMeasurement("W")
                        .valueTemplate("{{ value_json.meter_power_a_w }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_power_a_w"), gson.toJson(powerAW).getBytes());

                Sensor powerAkW = newMetModelBuilder("Power A (kW)", met)
                        .uniqueId(key(metId, "meter_power_a_kw"))
                        .objectId(key(metId, "meter_power_a_kw"))
                        .icon("mdi:lightning-bolt")
                        .stateClass("measurement")
                        .deviceClass("power")
                        .unitOfMeasurement("kW")
                        .valueTemplate("{{ value_json.meter_power_a_kw }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_power_a_kw"), gson.toJson(powerAkW).getBytes());

                Sensor powerBW = newMetModelBuilder("Power B (W)", met)
                        .uniqueId(key(metId, "meter_power_b_w"))
                        .objectId(key(metId, "meter_power_b_w"))
                        .icon("mdi:lightning-bolt")
                        .stateClass("measurement")
                        .deviceClass("power")
                        .unitOfMeasurement("W")
                        .valueTemplate("{{ value_json.meter_power_b_w }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_power_b_w"), gson.toJson(powerBW).getBytes());

                Sensor powerBkW = newMetModelBuilder("Power B (kW)", met)
                        .uniqueId(key(metId, "meter_power_b_kw"))
                        .objectId(key(metId, "meter_power_b_kw"))
                        .icon("mdi:lightning-bolt")
                        .stateClass("measurement")
                        .deviceClass("power")
                        .unitOfMeasurement("kW")
                        .valueTemplate("{{ value_json.meter_power_b_kw }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_power_b_kw"), gson.toJson(powerBkW).getBytes());

                Sensor powerCW = newMetModelBuilder("Power C (W)", met)
                        .uniqueId(key(metId, "meter_power_c_w"))
                        .objectId(key(metId, "meter_power_c_w"))
                        .icon("mdi:lightning-bolt")
                        .stateClass("measurement")
                        .deviceClass("power")
                        .unitOfMeasurement("W")
                        .valueTemplate("{{ value_json.meter_power_c_w }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_power_c_w"), gson.toJson(powerCW).getBytes());

                Sensor powerCkW = newMetModelBuilder("Power C (kW)", met)
                        .uniqueId(key(metId, "meter_power_c_kw"))
                        .objectId(key(metId, "meter_power_c_kw"))
                        .icon("mdi:lightning-bolt")
                        .stateClass("measurement")
                        .deviceClass("power")
                        .unitOfMeasurement("kW")
                        .valueTemplate("{{ value_json.meter_power_c_kw }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_power_c_kw"), gson.toJson(powerCkW).getBytes());

                Sensor powerTotalW = newMetModelBuilder("Power Total (W)", met)
                        .uniqueId(key(metId, "meter_power_total_w"))
                        .objectId(key(metId, "meter_power_total_w"))
                        .icon("mdi:lightning-bolt")
                        .stateClass("measurement")
                        .deviceClass("power")
                        .unitOfMeasurement("W")
                        .valueTemplate("{{ value_json.meter_power_total_w }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_power_total_w"), gson.toJson(powerTotalW).getBytes());

                Sensor powerTotalKW = newMetModelBuilder("Power Total (kW)", met)
                        .uniqueId(key(metId, "meter_power_total_kw"))
                        .objectId(key(metId, "meter_power_total_kw"))
                        .icon("mdi:lightning-bolt")
                        .stateClass("measurement")
                        .deviceClass("power")
                        .unitOfMeasurement("kW")
                        .valueTemplate("{{ value_json.meter_power_total_kw }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_power_total_kw"), gson.toJson(powerTotalKW).getBytes());

                Sensor powerFactorA = newMetModelBuilder("Power Factor A", met)
                        .uniqueId(key(metId, "meter_power_factor_a"))
                        .objectId(key(metId, "meter_power_factor_a"))
                        .icon("mdi:cosine-wave")
                        .stateClass("measurement")
                        .deviceClass("power_factor")
                        .unitOfMeasurement("PF")
                        .valueTemplate("{{ value_json.meter_power_factor_a }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_power_factor_a"), gson.toJson(powerFactorA).getBytes());

                Sensor powerFactorB = newMetModelBuilder("Power Factor B", met)
                        .uniqueId(key(metId, "meter_power_factor_b"))
                        .objectId(key(metId, "meter_power_factor_b"))
                        .icon("mdi:cosine-wave")
                        .stateClass("measurement")
                        .deviceClass("power_factor")
                        .unitOfMeasurement("PF")
                        .valueTemplate("{{ value_json.meter_power_factor_b }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_power_factor_b"), gson.toJson(powerFactorB).getBytes());

                Sensor powerFactorC = newMetModelBuilder("Power Factor C", met)
                        .uniqueId(key(metId, "meter_power_factor_c"))
                        .objectId(key(metId, "meter_power_factor_c"))
                        .icon("mdi:cosine-wave")
                        .stateClass("measurement")
                        .deviceClass("power_factor")
                        .unitOfMeasurement("PF")
                        .valueTemplate("{{ value_json.meter_power_factor_c }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_power_factor_c"), gson.toJson(powerFactorC).getBytes());

                Sensor powerFactorTotal = newMetModelBuilder("Power Factor Total", met)
                        .uniqueId(key(metId, "meter_power_factor_total"))
                        .objectId(key(metId, "meter_power_factor_total"))
                        .icon("mdi:cosine-wave")
                        .stateClass("measurement")
                        .deviceClass("power_factor")
                        .unitOfMeasurement("PF")
                        .valueTemplate("{{ value_json.meter_power_factor_total }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_power_factor_total"), gson.toJson(powerFactorTotal).getBytes());

                Sensor voltageA = newMetModelBuilder("Voltage A", met)
                        .uniqueId(key(metId, "meter_u_a"))
                        .objectId(key(metId, "meter_u_a"))
                        .icon("mdi:sine-wave")
                        .stateClass("measurement")
                        .deviceClass("voltage")
                        .unitOfMeasurement("V")
                        .valueTemplate("{{ value_json.meter_u_a }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_u_a"), gson.toJson(voltageA).getBytes());

                Sensor voltageB = newMetModelBuilder("Voltage B", met)
                        .uniqueId(key(metId, "meter_u_b"))
                        .objectId(key(metId, "meter_u_b"))
                        .icon("mdi:sine-wave")
                        .stateClass("measurement")
                        .deviceClass("voltage")
                        .unitOfMeasurement("V")
                        .valueTemplate("{{ value_json.meter_u_b }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_u_b"), gson.toJson(voltageB).getBytes());

                Sensor voltageC = newMetModelBuilder("Voltage C", met)
                        .uniqueId(key(metId, "meter_u_c"))
                        .objectId(key(metId, "meter_u_c"))
                        .icon("mdi:sine-wave")
                        .stateClass("measurement")
                        .deviceClass("voltage")
                        .unitOfMeasurement("V")
                        .valueTemplate("{{ value_json.meter_u_c }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_u_c"), gson.toJson(voltageC).getBytes());

                Sensor currentA = newMetModelBuilder("Current A", met)
                        .uniqueId(key(metId, "meter_i_a"))
                        .objectId(key(metId, "meter_i_a"))
                        .icon("mdi:current-ac")
                        .stateClass("measurement")
                        .deviceClass("current")
                        .unitOfMeasurement("A")
                        .valueTemplate("{{ value_json.meter_i_a }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_i_a"), gson.toJson(currentA).getBytes());

                Sensor currentB = newMetModelBuilder("Current B", met)
                        .uniqueId(key(metId, "meter_i_b"))
                        .objectId(key(metId, "meter_i_b"))
                        .icon("mdi:current-ac")
                        .stateClass("measurement")
                        .deviceClass("current")
                        .unitOfMeasurement("A")
                        .valueTemplate("{{ value_json.meter_i_b }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_i_b"), gson.toJson(currentB).getBytes());

                Sensor currentC = newMetModelBuilder("Current C", met)
                        .uniqueId(key(metId, "meter_i_c"))
                        .objectId(key(metId, "meter_i_c"))
                        .icon("mdi:current-ac")
                        .stateClass("measurement")
                        .deviceClass("current")
                        .unitOfMeasurement("A")
                        .valueTemplate("{{ value_json.meter_i_c }}")
                        .build();
                mqttService.sendHomeAssistantConfig(key(metId, "meter_i_c"), gson.toJson(currentC).getBytes());



                Sensor energyImportAWh = newMetModelBuilder("Energy import A (Wh)", met)
                        .uniqueId(key(metId, "meter_energy_import_a_wh"))
                        .objectId(key(metId, "meter_energy_import_a_wh"))
                        .icon("mdi:transmission-tower-import")
                        .stateClass("total_increasing")
                        .deviceClass("energy")
                        .unitOfMeasurement("Wh")
                        .valueTemplate("{{ value_json.meter_energy_import_a_wh }}")
                        .build();
                mqttService.sendHomeAssistantConfig((key(metId, "meter_energy_import_a_wh")), gson.toJson(energyImportAWh).getBytes());


                Sensor energyImportAkWh = newMetModelBuilder("Energy import A (kWh)", met)
                        .uniqueId(key(metId, "meter_energy_import_a_wh"))
                        .objectId(key(metId, "meter_energy_import_a_wh"))
                        .icon("mdi:transmission-tower-import")
                        .stateClass("total_increasing")
                        .deviceClass("energy")
                        .unitOfMeasurement("Wh")
                        .valueTemplate("{{ value_json.meter_energy_import_a_wh }}")
                        .build();
                mqttService.sendHomeAssistantConfig((key(metId, "meter_energy_import_a_wh")), gson.toJson(energyImportAkWh).getBytes());

                Sensor energyImportBWh = newMetModelBuilder("Energy import B (Wh)", met)
                        .uniqueId(key(metId, "meter_energy_import_b_wh"))
                        .objectId(key(metId, "meter_energy_import_b_wh"))
                        .icon("mdi:transmission-tower-import")
                        .stateClass("total_increasing")
                        .deviceClass("energy")
                        .unitOfMeasurement("Wh")
                        .valueTemplate("{{ value_json.meter_energy_import_b_wh }}")
                        .build();
                mqttService.sendHomeAssistantConfig((key(metId, "meter_energy_import_b_wh")), gson.toJson(energyImportBWh).getBytes());

                Sensor energyImportBkWh = newMetModelBuilder("Energy import B (kWh)", met)
                        .uniqueId(key(metId, "meter_energy_import_b_kwh"))
                        .objectId(key(metId, "meter_energy_import_b_kwh"))
                        .icon("mdi:transmission-tower-import")
                        .stateClass("total_increasing")
                        .deviceClass("energy")
                        .unitOfMeasurement("kWh")
                        .valueTemplate("{{ value_json.meter_energy_import_b_kwh }}")
                        .build();
                mqttService.sendHomeAssistantConfig((key(metId, "meter_energy_import_b_kwh")), gson.toJson(energyImportBkWh).getBytes());

                Sensor energyImportCWh = newMetModelBuilder("Energy import C (Wh)", met)
                        .uniqueId(key(metId, "meter_energy_import_c_wh"))
                        .objectId(key(metId, "meter_energy_import_c_wh"))
                        .icon("mdi:transmission-tower-import")
                        .stateClass("total_increasing")
                        .deviceClass("energy")
                        .unitOfMeasurement("Wh")
                        .valueTemplate("{{ value_json.meter_energy_import_c_wh }}")
                        .build();
                mqttService.sendHomeAssistantConfig((key(metId, "meter_energy_import_c_wh")), gson.toJson(energyImportCWh).getBytes());

                Sensor energyImportCkWh = newMetModelBuilder("Energy import C (kWh)", met)
                        .uniqueId(key(metId, "meter_energy_import_c_kwh"))
                        .objectId(key(metId, "meter_energy_import_c_kwh"))
                        .icon("mdi:transmission-tower-import")
                        .stateClass("total_increasing")
                        .deviceClass("energy")
                        .unitOfMeasurement("kWh")
                        .valueTemplate("{{ value_json.meter_energy_import_c_kwh }}")
                        .build();
                mqttService.sendHomeAssistantConfig((key(metId, "meter_energy_import_c_kwh")), gson.toJson(energyImportCkWh).getBytes());

                Sensor energyExportAWh = newMetModelBuilder("Energy export A (Wh)", met)
                        .uniqueId(key(metId, "meter_energy_export_a_wh"))
                        .objectId(key(metId, "meter_energy_export_a_wh"))
                        .icon("mdi:transmission-tower-export")
                        .stateClass("total_increasing")
                        .deviceClass("energy")
                        .unitOfMeasurement("Wh")
                        .valueTemplate("{{ value_json.meter_energy_export_a_wh }}")
                        .build();
                mqttService.sendHomeAssistantConfig((key(metId, "meter_energy_export_a_wh")), gson.toJson(energyExportAWh).getBytes());

                Sensor energyExportAkWh = newMetModelBuilder("Energy export A (kWh)", met)
                        .uniqueId(key(metId, "meter_energy_export_a_kwh"))
                        .objectId(key(metId, "meter_energy_export_a_kwh"))
                        .icon("mdi:transmission-tower-export")
                        .stateClass("total_increasing")
                        .deviceClass("energy")
                        .unitOfMeasurement("kWh")
                        .valueTemplate("{{ value_json.meter_energy_export_a_kwh }}")
                        .build();
                mqttService.sendHomeAssistantConfig((key(metId, "meter_energy_export_a_kwh")), gson.toJson(energyExportAkWh).getBytes());

                Sensor energyExportBWh = newMetModelBuilder("Energy export B (Wh)", met)
                        .uniqueId(key(metId, "meter_energy_export_b_wh"))
                        .objectId(key(metId, "meter_energy_export_b_wh"))
                        .icon("mdi:transmission-tower-export")
                        .stateClass("total_increasing")
                        .deviceClass("energy")
                        .unitOfMeasurement("Wh")
                        .valueTemplate("{{ value_json.meter_energy_export_b_wh }}")
                        .build();
                mqttService.sendHomeAssistantConfig((key(metId, "meter_energy_export_b_wh")), gson.toJson(energyExportBWh).getBytes());

                Sensor energyExportBkWh = newMetModelBuilder("Energy export B (kWh)", met)
                        .uniqueId(key(metId, "meter_energy_export_b_kwh"))
                        .objectId(key(metId, "meter_energy_export_b_kwh"))
                        .icon("mdi:transmission-tower-export")
                        .stateClass("total_increasing")
                        .deviceClass("energy")
                        .unitOfMeasurement("kWh")
                        .valueTemplate("{{ value_json.meter_energy_export_b_kwh }}")
                        .build();
                mqttService.sendHomeAssistantConfig((key(metId, "meter_energy_export_b_kwh")), gson.toJson(energyExportBkWh).getBytes());

                Sensor energyExportCWh = newMetModelBuilder("Energy export C (Wh)", met)
                        .uniqueId(key(metId, "meter_energy_export_c_wh"))
                        .objectId(key(metId, "meter_energy_export_c_wh"))
                        .icon("mdi:transmission-tower-export")
                        .stateClass("total_increasing")
                        .deviceClass("energy")
                        .unitOfMeasurement("Wh")
                        .valueTemplate("{{ value_json.meter_energy_export_c_wh }}")
                        .build();
                mqttService.sendHomeAssistantConfig((key(metId, "meter_energy_export_c_wh")), gson.toJson(energyExportCWh).getBytes());

                Sensor energyExportCkWh = newMetModelBuilder("Energy export C (kWh)", met)
                        .uniqueId(key(metId, "meter_energy_export_c_kwh"))
                        .objectId(key(metId, "meter_energy_export_c_kwh"))
                        .icon("mdi:transmission-tower-export")
                        .stateClass("total_increasing")
                        .deviceClass("energy")
                        .unitOfMeasurement("kWh")
                        .valueTemplate("{{ value_json.meter_energy_export_c_kwh }}")
                        .build();
                mqttService.sendHomeAssistantConfig((key(metId, "meter_energy_export_c_kwh")), gson.toJson(energyExportCkWh).getBytes());

                Sensor energyImportTotalWh = newMetModelBuilder("Energy import Total (Wh)", met)
                        .uniqueId(key(metId, "meter_energy_import_total_wh"))
                        .objectId(key(metId, "meter_energy_import_total_wh"))
                        .icon("mdi:transmission-tower-import")
                        .stateClass("total_increasing")
                        .deviceClass("energy")
                        .unitOfMeasurement("Wh")
                        .valueTemplate("{{ value_json.meter_energy_import_total_wh }}")
                        .build();
                mqttService.sendHomeAssistantConfig((key(metId, "meter_energy_import_total_wh")), gson.toJson(energyImportTotalWh).getBytes());

                Sensor energyImportTotalkWh = newMetModelBuilder("Energy import Total (kWh)", met)
                        .uniqueId(key(metId, "meter_energy_import_total_kwh"))
                        .objectId(key(metId, "meter_energy_import_total_kwh"))
                        .icon("mdi:transmission-tower-import")
                        .stateClass("total_increasing")
                        .deviceClass("energy")
                        .unitOfMeasurement("kWh")
                        .valueTemplate("{{ value_json.meter_energy_import_total_kwh }}")
                        .build();
                mqttService.sendHomeAssistantConfig((key(metId, "meter_energy_import_total_kwh")), gson.toJson(energyImportTotalkWh).getBytes());

                Sensor energyExportTotalWh = newMetModelBuilder("Energy export Total (Wh)", met)
                        .uniqueId(key(metId, "meter_energy_export_total_wh"))
                        .objectId(key(metId, "meter_energy_export_total_wh"))
                        .icon("mdi:transmission-tower-export")
                        .stateClass("total_increasing")
                        .deviceClass("energy")
                        .unitOfMeasurement("Wh")
                        .valueTemplate("{{ value_json.meter_energy_export_total_wh }}")
                        .build();
                mqttService.sendHomeAssistantConfig((key(metId, "meter_energy_export_total_wh")), gson.toJson(energyExportTotalWh).getBytes());

                Sensor energyExportTotalkWh = newMetModelBuilder("Energy export Total (kWh)", met)
                        .uniqueId(key(metId, "meter_energy_export_total_kwh"))
                        .objectId(key(metId, "meter_energy_export_total_kwh"))
                        .icon("mdi:transmission-tower-export")
                        .stateClass("total_increasing")
                        .deviceClass("energy")
                        .unitOfMeasurement("kWh")
                        .valueTemplate("{{ value_json.meter_energy_export_total_kwh }}")
                        .build();
                mqttService.sendHomeAssistantConfig((key(metId, "meter_energy_export_total_kwh")), gson.toJson(energyExportTotalkWh).getBytes());


                
                
                
        });
    }

    /**
     * DTU Autodiscovery
     *
     * @param info
     */
    private void registerDtuAutodiscovery(@NotNull AppInfo info) {
        String dtuId = String.format("dtu_%s", info.getDtuSn());

        Sensor modelPowerTotalW = newDtuModelBuilder(key(dtuId, "power_total_w"), info)
                .uniqueId(key(dtuId, "power_total_w"))
                .objectId(key(dtuId, "power_total_w"))
                .icon("mdi:solar-power")
                .stateClass("measurement")
                .deviceClass("power")
                .unitOfMeasurement("W")
                .valueTemplate("{{ value_json.power_total_w }}")
                .build();
        mqttService.sendHomeAssistantConfig("power_total_w", gson.toJson(modelPowerTotalW).getBytes());

        Sensor modelPowerTotalKW = newDtuModelBuilder(key(dtuId, "power_total_kw"), info)
                .uniqueId(key(dtuId, "power_total_kw"))
                .objectId(key(dtuId, "power_total_kw"))
                .icon("mdi:solar-power")
                .stateClass("measurement")
                .deviceClass("power")
                .unitOfMeasurement("kW")
                .valueTemplate("{{ value_json.power_total_kw }}")
                .build();
        mqttService.sendHomeAssistantConfig("power_total_kw", gson.toJson(modelPowerTotalKW).getBytes());

        // energy today
        Sensor modelEnergyTodayWh = newDtuModelBuilder(key(dtuId, "energy_today_wh"), info)
                .uniqueId(key(dtuId, "energy_today_wh"))
                .objectId(key(dtuId, "energy_today_wh"))
                .stateClass("total_increasing")
                .deviceClass("energy")
                .unitOfMeasurement("Wh")
                .valueTemplate("{{ value_json.energy_today_wh }}")
                .build();
        mqttService.sendHomeAssistantConfig("energy_today_wh", gson.toJson(modelEnergyTodayWh).getBytes());

        Sensor modelEnergyTodayKWh = newDtuModelBuilder(key(dtuId, "energy_today_kwh"), info)
                .uniqueId(key(dtuId, "energy_today_kwh"))
                .objectId(key(dtuId, "energy_today_kwh"))
                .stateClass("total_increasing")
                .deviceClass("energy")
                .unitOfMeasurement("kWh")
                .valueTemplate("{{ value_json.energy_today_kwh }}")
                .build();
        mqttService.sendHomeAssistantConfig("energy_today_kwh", gson.toJson(modelEnergyTodayKWh).getBytes());

        // energy total
        Sensor modelEnergyTotalWh = newDtuModelBuilder(key(dtuId, "energy_total_wh"), info)
                .uniqueId(key(dtuId, "energy_total_wh"))
                .objectId(key(dtuId, "energy_total_wh"))
                .stateClass("total_increasing")
                .deviceClass("energy")
                .unitOfMeasurement("Wh")
                .valueTemplate("{{ value_json.energy_total_wh }}")
                .build();
        mqttService.sendHomeAssistantConfig("energy_total_wh", gson.toJson(modelEnergyTotalWh).getBytes());

        Sensor modelEnergyTotalKWh = newDtuModelBuilder(key(dtuId, "energy_total_kwh"), info)
                .uniqueId(key(dtuId, "energy_total_kwh"))
                .objectId(key(dtuId, "energy_total_kwh"))
                .stateClass("total_increasing")
                .deviceClass("energy")
                .unitOfMeasurement("kWh")
                .valueTemplate("{{ value_json.energy_total_kwh }}")
                .build();
        mqttService.sendHomeAssistantConfig("energy_total_kwh", gson.toJson(modelEnergyTotalKWh).getBytes());

        Sensor position = newDtuModelBuilder("last_seen", info)
                .uniqueId(key(dtuId, "last_seen"))
                .objectId(key(dtuId, "last_seen"))
                .icon("mdi:clock")
                .deviceClass("timestamp")
                .valueTemplate("{{ value_json.last_seen }}")
                .build();
        mqttService.sendHomeAssistantConfig("last_seen", gson.toJson(position).getBytes());
    }

    /**
     * Inverters autodiscovery
     *
     * @param sgsInfos
     */
    private void registerInvAutodiscovery(@NotNull List<AppInfo.SgsInfo> sgsInfos) {
        // INVERTERS
        sgsInfos.forEach(sgsInfo -> {
            String invId = String.format("inv_%s", sgsInfo.getInvSn());

            Sensor gridVoltage = newInvModelBuilder(key(invId, "grid_voltage"), sgsInfo)
                    .uniqueId(key(invId, "grid_voltage"))
                    .stateClass("measurement")
                    .deviceClass("voltage")
                    .unitOfMeasurement("V")
                    .valueTemplate("{{ value_json.grid_voltage }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(invId, "grid_voltage"), gson.toJson(gridVoltage).getBytes());

            Sensor gridFrequency = newInvModelBuilder(key(invId, "grid_frequency"), sgsInfo)
                    .uniqueId(key(invId, "grid_frequency"))
                    .stateClass("measurement")
                    .deviceClass("frequency")
                    .unitOfMeasurement("Hz")
                    .valueTemplate("{{ value_json.grid_frequency }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(invId, "grid_frequency"), gson.toJson(gridFrequency).getBytes());

            Sensor gridPower = newInvModelBuilder(key(invId, "grid_power"), sgsInfo)
                    .uniqueId(key(invId, "grid_power"))
                    .icon("mdi:solar-power")
                    .stateClass("measurement")
                    .deviceClass("power")
                    .unitOfMeasurement("W")
                    .valueTemplate("{{ value_json.grid_power }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(invId, "grid_power"), gson.toJson(gridPower).getBytes());

            Sensor gridReactivePower = newInvModelBuilder(key(invId, "grid_reactive_power"), sgsInfo)
                    .uniqueId(key(invId, "grid_reactive_power"))
                    .stateClass("measurement")
                    .deviceClass("reactive_power")
                    .unitOfMeasurement("var")
                    .valueTemplate("{{ value_json.grid_reactive_power }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(invId, "grid_reactive_power"), gson.toJson(gridReactivePower).getBytes());

            Sensor gridCurrent = newInvModelBuilder(key(invId, "grid_current"), sgsInfo)
                    .uniqueId(key(invId, "grid_current"))
                    .stateClass("measurement")
                    .deviceClass("current")
                    .unitOfMeasurement("A")
                    .valueTemplate("{{ value_json.grid_current }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(invId, "grid_current"), gson.toJson(gridCurrent).getBytes());

            Sensor powerFactor = newInvModelBuilder(key(invId, "power_factor"), sgsInfo)
                    .uniqueId(key(invId, "power_factor"))
                    .stateClass("measurement")
                    .deviceClass("power_factor")
                    .unitOfMeasurement("%")
                    .valueTemplate("{{ value_json.power_factor }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(invId, "power_factor"), gson.toJson(powerFactor).getBytes());

            Sensor temperature = newInvModelBuilder(key(invId, "temperature"), sgsInfo)
                    .uniqueId(key(invId, "temperature"))
                    .stateClass("measurement")
                    .deviceClass("temperature")
                    .unitOfMeasurement("°C")
                    .valueTemplate("{{ value_json.temperature }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(invId, "temperature"), gson.toJson(temperature).getBytes());
        });
    }

    private Sensor.SensorBuilder newPvModelBuilder(String name, @NotNull RealData.PvMO pv) {
        String pvId = String.format("pv_%s_%d", pv.getSn(), pv.getPort());
        return newSensorBuilder()
                .device(
                        Sensor.Device.builder()
                                .identifiers(List.of(pvId))
                                .manufacturer("Unknown")
                                .name("Solar Panel")
                                .model("")
                                .viaDevice(String.format("Inverter (sn: %s)", pv.getSn()))
                                .build())
                .name(name)
                .jsonAttributesTopic("hoymiles-dtu/" + pvId)
                .stateTopic("hoymiles-dtu/" + pvId);
    }

    private Sensor.SensorBuilder newMetModelBuilder(String name, @NotNull AppInfo.MeterInfo met) {
        String metId = String.format("met_%s", met.getMeterSn());
        return newSensorBuilder()
                .device(
                        Sensor.Device.builder()
                                .identifiers(List.of(metId))
                                .manufacturer("Unknown")
                                .name(String.format("Energy Meter (sn: %s)", met.getMeterSn()))
                                .model(String.format("%d - %d",met.getMeterModel(), met.getMeterCt()))
                                .viaDevice(String.format("Meter (sn: %s)", met.getMeterSn()))
                                .build())
                .name(name)
                .jsonAttributesTopic("hoymiles-dtu/" + metId)
                .stateTopic("hoymiles-dtu/" + metId);
    }

    private Sensor.SensorBuilder newInvModelBuilder(String name, @NotNull AppInfo.SgsInfo info) {
        String invId = String.format("inv_%s", info.getInvSn());
        return newSensorBuilder()
                .device(
                        Sensor.Device.builder()
                                .identifiers(List.of(invId))
                                .manufacturer("Hoymiles")
                                .name(String.format("Hoymiles Solar Inverter (sn: %s)", info.getInvSn()))
                                .model(String.format("Inverter Hoymiles (hw: %d)", info.getInvHw()))
                                .swVersion(String.valueOf(info.getInvSw()))
                                .build())
                .name(name)
                .jsonAttributesTopic("hoymiles-dtu/" + invId)
                .stateTopic("hoymiles-dtu/" + invId);
    }

    private Sensor.SensorBuilder newDtuModelBuilder(String name, @NotNull AppInfo info) {
        String dtuId = String.format("dtu_%s", info.getDtuSn());
        return newSensorBuilder()
                .device(
                        Sensor.Device.builder()
                                .identifiers(List.of(dtuId))
                                .manufacturer("Hoymiles")
                                .name("Hoymiles Solar Gateway")
                                .model(String.format("Hoymiles DTU (hw: %d)", info.getDtuInfo().getDtuHw()))
                                .swVersion(String.valueOf(info.getDtuInfo().getDtuSw()))
                                .build())
                .name(name)
                .jsonAttributesTopic("hoymiles-dtu/" + dtuId)
                .stateTopic("hoymiles-dtu/" + dtuId);
    }

    private Sensor.SensorBuilder newSensorBuilder() {
        return Sensor.builder()
                .availability(
                        List.of(
                                Sensor.Availability.builder()
                                        .topic("hoymiles-dtu/bridge/state")
                                        .build()))
                .enabledByDefault(true);
    }

    private static String key(String device, String name) {
        return String.format("%s_%s", device, name);
    }
}
