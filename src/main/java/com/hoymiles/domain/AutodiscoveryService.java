package com.hoymiles.domain;

import com.hoymiles.domain.model.AppInfo;
import com.hoymiles.domain.model.RealData;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.util.List;

@Dependent
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class AutodiscoveryService {

    private final IMqttRepository mqttService;

    public void registerHomeAssistantAutodiscovery(@NotNull AppInfo info) {
        registerDtuAutodiscovery(info);
        registerInvAutodiscovery(info.getSgsInfo());
    }

    public void registerPvAutodiscovery(@NotNull List<RealData.PvMO> pvInfos) {
        JtwigTemplate templateSensor = JtwigTemplate.classpathTemplate("template/sensor.twig");
        pvInfos.forEach(pv -> {
            String pvId = String.format("pv_%s_%d", pv.getSn(), pv.getPosition());

            JtwigModel position = newPvModel(key(pvId, "position"), pv)
                    .with("state_class", "")
                    .with("device_class", "")
                    .with("unit_of_measurement", "")
                    .with("value_template", "{{ value_json.position }}");
            mqttService.sendHomeAssistantConfig(key(pvId, "position"), templateSensor.render(position).getBytes());

            JtwigModel voltage = newPvModel(key(pvId, "voltage"), pv)
                    .with("state_class", "measurement")
                    .with("device_class", "voltage")
                    .with("unit_of_measurement", "V")
                    .with("value_template", "{{ value_json.voltage }}");
            mqttService.sendHomeAssistantConfig(key(pvId, "voltage"), templateSensor.render(voltage).getBytes());

            JtwigModel current = newPvModel(key(pvId, "current"), pv)
                    .with("state_class", "measurement")
                    .with("device_class", "current")
                    .with("unit_of_measurement", "A")
                    .with("value_template", "{{ value_json.current }}");
            mqttService.sendHomeAssistantConfig(key(pvId, "frequency"), templateSensor.render(current).getBytes());

            JtwigModel power = newPvModel(key(pvId, "power"), pv)
                    .with("icon", "mdi:solar-power")
                    .with("state_class", "measurement")
                    .with("device_class", "power")
                    .with("unit_of_measurement", "W")
                    .with("value_template", "{{ value_json.power }}");
            mqttService.sendHomeAssistantConfig(key(pvId, "power"), templateSensor.render(power).getBytes());

            JtwigModel energyToday = newPvModel(key(pvId, "energy_today"), pv)
                    .with("state_class", "total_increasing")
                    .with("device_class", "energy")
                    .with("unit_of_measurement", "Wh")
                    .with("value_template", "{{ value_json.energy_today }}");
            mqttService.sendHomeAssistantConfig(key(pvId, "current"), templateSensor.render(energyToday).getBytes());

            JtwigModel energyTotal = newPvModel(key(pvId, "energy_total"), pv)
                    .with("state_class", "total_increasing")
                    .with("device_class", "energy")
                    .with("unit_of_measurement", "Wh")
                    .with("value_template", "{{ value_json.energy_total }}");
            mqttService.sendHomeAssistantConfig(key(pvId, "reactive_power"), templateSensor.render(energyTotal).getBytes());
        });
    }

    /**
     * DTU Autodiscovery
     *
     * @param info
     */
    private void registerDtuAutodiscovery(@NotNull AppInfo info) {
        String dtuId = String.format("dtu_%s", info.getDtuSn());
        JtwigTemplate templateSensor = JtwigTemplate.classpathTemplate("template/sensor.twig");

        // DTU
        // power
        JtwigModel modelPowerTotalW = newDtuSensor(key(dtuId, "power_total_w"), info)
                .with("icon", "mdi:solar-power")
                .with("state_class", "measurement")
                .with("device_class", "power")
                .with("unit_of_measurement", "W")
                .with("value_template", "{{ value_json.power_total_w }}");
        mqttService.sendHomeAssistantConfig("power_total_w", templateSensor.render(modelPowerTotalW).getBytes());

        JtwigModel modelPowerTotalKW = newDtuSensor(key(dtuId, "power_total_kw"), info)
                .with("icon", "mdi:solar-power")
                .with("state_class", "measurement")
                .with("device_class", "power")
                .with("unit_of_measurement", "kW")
                .with("value_template", "{{ value_json.power_total_kw }}");
        mqttService.sendHomeAssistantConfig("power_total_kw", templateSensor.render(modelPowerTotalKW).getBytes());

        // energy today
        JtwigModel modelEnergyTodayWh = newDtuSensor(dtuId + "energy_today_wh", info)
                .with("state_class", "total_increasing")
                .with("device_class", "energy")
                .with("unit_of_measurement", "Wh")
                .with("value_template", "{{ value_json.energy_today_wh }}");
        mqttService.sendHomeAssistantConfig("energy_today_wh", templateSensor.render(modelEnergyTodayWh).getBytes());

        JtwigModel modelEnergyTodayKWh = newDtuSensor(key(dtuId, "energy_today_kwh"), info)
                .with("state_class", "total_increasing")
                .with("device_class", "energy")
                .with("unit_of_measurement", "kWh")
                .with("value_template", "{{ value_json.energy_today_kwh }}");
        mqttService.sendHomeAssistantConfig("energy_today_kwh", templateSensor.render(modelEnergyTodayKWh).getBytes());

        // energy total
        JtwigModel modelEnergyTotalWh = newDtuSensor(key(dtuId, "energy_total_wh"), info)
                .with("state_class", "total_increasing")
                .with("device_class", "energy")
                .with("unit_of_measurement", "Wh")
                .with("value_template", "{{ value_json.energy_total_wh }}");
        mqttService.sendHomeAssistantConfig("energy_total_wh", templateSensor.render(modelEnergyTotalWh).getBytes());

        JtwigModel modelEnergyTotalKWh = newDtuSensor(key(dtuId, "energy_total_kwh"), info)
                .with("state_class", "total_increasing")
                .with("device_class", "energy")
                .with("unit_of_measurement", "Wh")
                .with("value_template", "{{ value_json.energy_total_kwh }}");
        mqttService.sendHomeAssistantConfig("energy_total_kwh", templateSensor.render(modelEnergyTotalKWh).getBytes());
    }

    /**
     * Inverters autodiscovery
     *
     * @param sgsInfos
     */
    private void registerInvAutodiscovery(@NotNull List<AppInfo.SgsInfo> sgsInfos) {
        JtwigTemplate templateSensor = JtwigTemplate.classpathTemplate("template/sensor.twig");

        // INVERTERS
        sgsInfos.forEach(sgsInfo -> {
            String invId = String.format("inv_%s", sgsInfo.getSn());
            JtwigModel gridVoltage = newInvModel(key(invId, "grid_voltage"), sgsInfo)
                    .with("state_class", "measurement")
                    .with("device_class", "voltage")
                    .with("unit_of_measurement", "V")
                    .with("value_template", "{{ value_json.grid_voltage }}");
            mqttService.sendHomeAssistantConfig(key(invId, "grid_voltage"), templateSensor.render(gridVoltage).getBytes());

            JtwigModel gridFrequency = newInvModel(key(invId, "grid_frequency"), sgsInfo)
                    .with("state_class", "measurement")
                    .with("device_class", "frequency")
                    .with("unit_of_measurement", "Hz")
                    .with("value_template", "{{ value_json.grid_frequency }}");
            mqttService.sendHomeAssistantConfig(key(invId, "grid_frequency"), templateSensor.render(gridFrequency).getBytes());

            JtwigModel gridPower = newInvModel(key(invId, "grid_power"), sgsInfo)
                    .with("icon", "mdi:solar-power")
                    .with("state_class", "measurement")
                    .with("device_class", "power")
                    .with("unit_of_measurement", "W")
                    .with("value_template", "{{ value_json.grid_power }}");
            mqttService.sendHomeAssistantConfig(key(invId, "grid_power"), templateSensor.render(gridPower).getBytes());

            JtwigModel gridReactivePower = newInvModel(key(invId, "grid_reactive_power"), sgsInfo)
                    .with("state_class", "measurement")
                    .with("device_class", "reactive_power")
                    .with("unit_of_measurement", "var")
                    .with("value_template", "{{ value_json.grid_reactive_power }}");
            mqttService.sendHomeAssistantConfig(key(invId, "grid_reactive_power"), templateSensor.render(gridReactivePower).getBytes());

            JtwigModel gridCurrent = newInvModel(key(invId, "grid_current"), sgsInfo)
                    .with("state_class", "measurement")
                    .with("device_class", "current")
                    .with("unit_of_measurement", "var")
                    .with("value_template", "{{ value_json.grid_current }}");
            mqttService.sendHomeAssistantConfig(key(invId, "grid_current"), templateSensor.render(gridCurrent).getBytes());

            JtwigModel powerFactor = newInvModel(key(invId, "power_factor"), sgsInfo)
                    .with("state_class", "measurement")
                    .with("device_class", "power_factor")
                    .with("unit_of_measurement", "%")
                    .with("value_template", "{{ value_json.power_factor }}");
            mqttService.sendHomeAssistantConfig(key(invId, "power_factor"), templateSensor.render(powerFactor).getBytes());

            JtwigModel temperature = newInvModel(key(invId, "temperature"), sgsInfo)
                    .with("state_class", "measurement")
                    .with("device_class", "temperature")
                    .with("unit_of_measurement", "°C")
                    .with("value_template", "{{ value_json.temperature }}");
            mqttService.sendHomeAssistantConfig(key(invId, "temperature"), templateSensor.render(temperature).getBytes());
        });
    }

    private JtwigModel newPvModel(String name, @NotNull RealData.PvMO pv) {
        String pvId = String.format("pv_%s_%d", pv.getSn(), pv.getPosition());
        JtwigTemplate templateDevice = JtwigTemplate.classpathTemplate("template/device.twig");
        JtwigModel modelInv = JtwigModel.newModel()
                .with("device_id", pvId)
                .with("model", "")
                .with("name", "Solar Panel");

        return newSensor()
                .with("json_attributes_topic", "hoymiles-dtu/" + pvId)
                .with("state_topic", "hoymiles-dtu/" + pvId)
                .with("device", templateDevice.render(modelInv));
    }

    private JtwigModel newInvModel(String name, @NotNull AppInfo.SgsInfo info) {
        String invId = String.format("inv_%s", info.getSn());
        JtwigTemplate templateDevice = JtwigTemplate.classpathTemplate("template/device.twig");
        JtwigModel modelInv = JtwigModel.newModel()
                .with("device_id", invId)
                .with("model", "HM-1500")
                .with("name", "Hoymiles Solar Inverter");

        return newSensor()
                .with("json_attributes_topic", "hoymiles-dtu/" + invId)
                .with("state_topic", "hoymiles-dtu/" + invId)
                .with("device", templateDevice.render(modelInv));
    }

    private JtwigModel newDtuSensor(String name, @NotNull AppInfo info) {
        String dtuId = String.format("dtu_%s", info.getDtuSn());
        JtwigTemplate templateDevice = JtwigTemplate.classpathTemplate("template/device.twig");
        JtwigModel modelDtu = JtwigModel.newModel()
                .with("device_id", dtuId)
                .with("model", "DTU-Pro")
                .with("name", "Hoymiles Solar Gateway")
                .with("sw_version", info.getDtuInfo().getDtuSw());

        return newSensor()
                .with("json_attributes_topic", "hoymiles-dtu/" + dtuId)
                .with("state_topic", "hoymiles-dtu/" + dtuId)
                .with("device", templateDevice.render(modelDtu));
    }

    private JtwigModel newSensor() {
        return JtwigModel.newModel()
                .with("availability_topic", "hoymiles-dtu/bridge/state");
    }

    private static String key(String device, String name) {
        return String.format("%s_%s", device, name);
    }
}
