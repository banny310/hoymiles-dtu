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
    }

    public void registerPvAutodiscovery(@NotNull List<RealData.PvMO> pvInfos) {
        pvInfos.forEach(pv -> {
            String pvId = String.format("pv_%s_%d", pv.getSn(), pv.getPosition());

            Sensor position = newPvModelBuilder(key(pvId, "position"), pv)
                    .stateClass("")
                    .stateTopic("")
                    .unitOfMeasurement("")
                    .valueTemplate("{{ value_json.position }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(pvId, "position"), gson.toJson(position).getBytes());

            Sensor voltage = newPvModelBuilder(key(pvId, "voltage"), pv)
                    .stateClass("measurement")
                    .deviceClass("voltage")
                    .unitOfMeasurement("V")
                    .valueTemplate("{{ value_json.voltage }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(pvId, "voltage"), gson.toJson(voltage).getBytes());

            Sensor current = newPvModelBuilder(key(pvId, "current"), pv)
                    .stateClass("measurement")
                    .deviceClass("current")
                    .unitOfMeasurement("A")
                    .valueTemplate("{{ value_json.current }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(pvId, "current"), gson.toJson(current).getBytes());

            Sensor power = newPvModelBuilder(key(pvId, "power"), pv)
                    .icon("mdi:solar-power")
                    .stateClass("measurement")
                    .deviceClass("power")
                    .unitOfMeasurement("W")
                    .valueTemplate("{{ value_json.frequency }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(pvId, "power"), gson.toJson(power).getBytes());

            Sensor energyToday = newPvModelBuilder(key(pvId, "energy_today"), pv)
                    .stateClass("total_increasing")
                    .deviceClass("energy")
                    .unitOfMeasurement("Wh")
                    .valueTemplate("{{ value_json.energy_today }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(pvId, "energy_today"), gson.toJson(energyToday).getBytes());

            Sensor energyTotal = newPvModelBuilder(key(pvId, "energy_total"), pv)
                    .stateClass("total_increasing")
                    .deviceClass("energy")
                    .unitOfMeasurement("Wh")
                    .valueTemplate("{{ value_json.energy_total }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(pvId, "energy_total"), gson.toJson(energyTotal).getBytes());
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
                .icon("mdi:solar-power")
                .stateClass("measurement")
                .deviceClass("power")
                .unitOfMeasurement("W")
                .valueTemplate("{{ value_json.power_total_w }}")
                .build();
        mqttService.sendHomeAssistantConfig("power_total_w", gson.toJson(modelPowerTotalW).getBytes());

        Sensor modelPowerTotalKW = newDtuModelBuilder(key(dtuId, "power_total_kw"), info)
                .icon("mdi:solar-power")
                .stateClass("measurement")
                .deviceClass("power")
                .unitOfMeasurement("kW")
                .valueTemplate("{{ value_json.power_total_kw }}")
                .build();
        mqttService.sendHomeAssistantConfig("power_total_kw", gson.toJson(modelPowerTotalKW).getBytes());

        // energy today
        Sensor modelEnergyTodayWh = newDtuModelBuilder(key(dtuId, "energy_today_wh"), info)
                .stateClass("total_increasing")
                .deviceClass("energy")
                .unitOfMeasurement("Wh")
                .valueTemplate("{{ value_json.energy_today_wh }}")
                .build();
        mqttService.sendHomeAssistantConfig("energy_today_wh", gson.toJson(modelEnergyTodayWh).getBytes());

        Sensor modelEnergyTodayKWh = newDtuModelBuilder(key(dtuId, "energy_today_kwh"), info)
                .stateClass("total_increasing")
                .deviceClass("energy")
                .unitOfMeasurement("kWh")
                .valueTemplate("{{ value_json.energy_today_kwh }}")
                .build();
        mqttService.sendHomeAssistantConfig("energy_today_kwh", gson.toJson(modelEnergyTodayKWh).getBytes());

        // energy total
        Sensor modelEnergyTotalWh = newDtuModelBuilder(key(dtuId, "energy_total_wh"), info)
                .stateClass("total_increasing")
                .deviceClass("energy")
                .unitOfMeasurement("Wh")
                .valueTemplate("{{ value_json.energy_total_wh }}")
                .build();
        mqttService.sendHomeAssistantConfig("energy_total_wh", gson.toJson(modelEnergyTotalWh).getBytes());

        Sensor modelEnergyTotalKWh = newDtuModelBuilder(key(dtuId, "energy_total_kwh"), info)
                .stateClass("total_increasing")
                .deviceClass("energy")
                .unitOfMeasurement("kWh")
                .valueTemplate("{{ value_json.energy_total_kwh }}")
                .build();
        mqttService.sendHomeAssistantConfig("energy_total_kwh", gson.toJson(modelEnergyTotalKWh).getBytes());
    }

    /**
     * Inverters autodiscovery
     *
     * @param sgsInfos
     */
    private void registerInvAutodiscovery(@NotNull List<AppInfo.SgsInfo> sgsInfos) {
        // INVERTERS
        sgsInfos.forEach(sgsInfo -> {
            String invId = String.format("inv_%s", sgsInfo.getSn());

            Sensor gridVoltage = newInvModelBuilder(key(invId, "grid_voltage"), sgsInfo)
                    .stateClass("measurement")
                    .deviceClass("voltage")
                    .unitOfMeasurement("V")
                    .valueTemplate("{{ value_json.grid_voltage }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(invId, "grid_voltage"), gson.toJson(gridVoltage).getBytes());

            Sensor gridFrequency = newInvModelBuilder(key(invId, "grid_frequency"), sgsInfo)
                    .stateClass("measurement")
                    .deviceClass("frequency")
                    .unitOfMeasurement("Hz")
                    .valueTemplate("{{ value_json.grid_frequency }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(invId, "grid_frequency"), gson.toJson(gridFrequency).getBytes());

            Sensor gridPower = newInvModelBuilder(key(invId, "grid_power"), sgsInfo)
                    .icon("mdi:solar-power")
                    .stateClass("measurement")
                    .deviceClass("power")
                    .unitOfMeasurement("W")
                    .valueTemplate("{{ value_json.grid_power }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(invId, "grid_power"), gson.toJson(gridPower).getBytes());

            Sensor gridReactivePower = newInvModelBuilder(key(invId, "grid_reactive_power"), sgsInfo)
                    .stateClass("measurement")
                    .deviceClass("reactive_power")
                    .unitOfMeasurement("var")
                    .valueTemplate("{{ value_json.grid_reactive_power }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(invId, "grid_reactive_power"), gson.toJson(gridReactivePower).getBytes());

            Sensor gridCurrent = newInvModelBuilder(key(invId, "grid_current"), sgsInfo)
                    .stateClass("measurement")
                    .deviceClass("current")
                    .unitOfMeasurement("A")
                    .valueTemplate("{{ value_json.grid_current }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(invId, "grid_current"), gson.toJson(gridCurrent).getBytes());

            Sensor powerFactor = newInvModelBuilder(key(invId, "power_factor"), sgsInfo)
                    .stateClass("measurement")
                    .deviceClass("power_factor")
                    .unitOfMeasurement("%")
                    .valueTemplate("{{ value_json.power_factor }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(invId, "power_factor"), gson.toJson(powerFactor).getBytes());

            Sensor temperature = newInvModelBuilder(key(invId, "temperature"), sgsInfo)
                    .stateClass("measurement")
                    .deviceClass("temperature")
                    .unitOfMeasurement("°C")
                    .valueTemplate("{{ value_json.temperature }}")
                    .build();
            mqttService.sendHomeAssistantConfig(key(invId, "temperature"), gson.toJson(temperature).getBytes());
        });
    }

    private Sensor.SensorBuilder newPvModelBuilder(String name, @NotNull RealData.PvMO pv) {
        String pvId = String.format("pv_%s_%d", pv.getSn(), pv.getPosition());
        return newSensorBuilder()
                .device(
                        Sensor.Device.builder()
                                .name("Solar Panel")
                                .model("")
                                .build())
                .jsonAttributesTopic("hoymiles-dtu/" + pvId)
                .stateTopic("hoymiles-dtu/" + pvId);
    }

    private Sensor.SensorBuilder newInvModelBuilder(String name, @NotNull AppInfo.SgsInfo info) {
        String invId = String.format("inv_%s", info.getSn());
        return newSensorBuilder()
                .device(
                        Sensor.Device.builder()
                                .name("Hoymiles Solar Inverter")
                                .model("HM-1500")
                                .build())
                .jsonAttributesTopic("hoymiles-dtu/" + invId)
                .stateTopic("hoymiles-dtu/" + invId);
    }

    private Sensor.SensorBuilder newDtuModelBuilder(String name, @NotNull AppInfo info) {
        String dtuId = String.format("dtu_%s", info.getDtuSn());
        return newSensorBuilder()
                .device(
                        Sensor.Device.builder()
                                .name("Hoymiles Solar Gateway")
                                .model("DTU-Pro")
                                .swVersion(String.valueOf(info.getDtuInfo().getDtuSw()))
                                .build())
                .jsonAttributesTopic("hoymiles-dtu/" + dtuId)
                .stateTopic("hoymiles-dtu/" + dtuId);
    }

    private Sensor.SensorBuilder newSensorBuilder() {
        return Sensor.builder()
                .availability(
                        List.of(
                                Sensor.Availability.builder()
                                        .topic("hoymiles-dtu/bridge/state")
                                        .build()));
    }

    private static String key(String device, String name) {
        return String.format("%s_%s", device, name);
    }
}
