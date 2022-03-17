package com.hoymiles.domain;

import com.hoymiles.domain.model.AppInfo;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

@Dependent
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class AutodiscoveryService {

    private final IMqttRepository mqttService;

    public void registerHomeAssistantAutodiscovery(@NotNull AppInfo info) {
        String dtuId = String.format("dtu_%s", info.getDtuSn());
        JtwigTemplate templateDevice = JtwigTemplate.classpathTemplate("template/device.twig");
        JtwigTemplate templateSensor = JtwigTemplate.classpathTemplate("template/sensor.twig");

        JtwigModel modelDtu = JtwigModel.newModel()
                .with("device_id", dtuId)
                .with("model", "DTU-Pro")
                .with("name", "Hoymiles Solar Gateway")
                .with("sn", info.getDtuSn())
                .with("sw_version", info.getDtuInfo().getDtuSw())
                .with("hw_version", info.getDtuInfo().getDtuHw());

        // power
        JtwigModel modelPowerTotalW = JtwigModel.newModel()
                .with("availability_topic", "hoymiles-solar/bridge/state")
                .with("device", templateDevice.render(modelDtu))
                .with("icon", "mdi:solar-power")
                .with("json_attributes_topic", "hoymiles-solar/" + dtuId)
                .with("name", dtuId + "_power_total_w")
                .with("state_class", "measurement")
                .with("device_class", "power")
                .with("state_topic", "hoymiles-solar/" + dtuId)
                .with("unique_id", dtuId + "_power_total_w")
                .with("unit_of_measurement", "W")
                .with("value_template", "{{ value_json.power_total_w }}");
        mqttService.sendHomeAssistantConfig("power_total_w", templateSensor.render(modelPowerTotalW).getBytes());

        JtwigModel modelPowerTotalKW = JtwigModel.newModel()
                .with("availability_topic", "hoymiles-solar/bridge/state")
                .with("device", templateDevice.render(modelDtu))
                .with("icon", "mdi:solar-power")
                .with("json_attributes_topic", "hoymiles-solar/" + dtuId)
                .with("name", dtuId + "_power_total_kw")
                .with("state_class", "measurement")
                .with("device_class", "power")
                .with("state_topic", "hoymiles-solar/" + dtuId)
                .with("unique_id", dtuId + "_power_total_kw")
                .with("unit_of_measurement", "kW")
                .with("value_template", "{{ value_json.power_total_kw }}");
        mqttService.sendHomeAssistantConfig("power_total_kw", templateSensor.render(modelPowerTotalKW).getBytes());

        // energy today
        JtwigModel modelEnergyTodayWh = JtwigModel.newModel()
                .with("availability_topic", "hoymiles-solar/bridge/state")
                .with("device", templateDevice.render(modelDtu))
                .with("json_attributes_topic", "hoymiles-solar/" + dtuId)
                .with("name", dtuId + "_energy_today_wh")
                .with("state_class", "total_increasing")
                .with("device_class", "energy")
                .with("state_topic", "hoymiles-solar/" + dtuId)
                .with("unique_id", dtuId + "_energy_today_wh")
                .with("unit_of_measurement", "Wh")
                .with("value_template", "{{ value_json.energy_today_wh }}");
        mqttService.sendHomeAssistantConfig("energy_today_wh", templateSensor.render(modelEnergyTodayWh).getBytes());

        JtwigModel modelEnergyTodayKWh = JtwigModel.newModel()
                .with("availability_topic", "hoymiles-solar/bridge/state")
                .with("device", templateDevice.render(modelDtu))
                .with("json_attributes_topic", "hoymiles-solar/" + dtuId)
                .with("name", dtuId + "_energy_today_kwh")
                .with("state_class", "total_increasing")
                .with("device_class", "energy")
                .with("state_topic", "hoymiles-solar/" + dtuId)
                .with("unique_id", dtuId + "_energy_today_kwh")
                .with("unit_of_measurement", "kWh")
                .with("value_template", "{{ value_json.energy_today_kwh }}");
        mqttService.sendHomeAssistantConfig("energy_today_kwh", templateSensor.render(modelEnergyTodayKWh).getBytes());

        // energy total
        JtwigModel modelEnergyTotalWh = JtwigModel.newModel()
                .with("availability_topic", "hoymiles-solar/bridge/state")
                .with("device", templateDevice.render(modelDtu))
                .with("json_attributes_topic", "hoymiles-solar/" + dtuId)
                .with("name", dtuId + "_energy_total_wh")
                .with("state_class", "total_increasing")
                .with("device_class", "energy")
                .with("state_topic", "hoymiles-solar/" + dtuId)
                .with("unique_id", dtuId + "_energy_total_wh")
                .with("unit_of_measurement", "Wh")
                .with("value_template", "{{ value_json.energy_total_wh }}");
        mqttService.sendHomeAssistantConfig("energy_total_wh", templateSensor.render(modelEnergyTotalWh).getBytes());

        JtwigModel modelEnergyTotalKWh = JtwigModel.newModel()
                .with("availability_topic", "hoymiles-solar/bridge/state")
                .with("device", templateDevice.render(modelDtu))
                .with("json_attributes_topic", "hoymiles-solar/" + dtuId)
                .with("name", dtuId + "_energy_total_kwh")
                .with("state_class", "total_increasing")
                .with("device_class", "energy")
                .with("state_topic", "hoymiles-solar/" + dtuId)
                .with("unique_id", dtuId + "_energy_total_kwh")
                .with("unit_of_measurement", "Wh")
                .with("value_template", "{{ value_json.energy_total_kwh }}");
        mqttService.sendHomeAssistantConfig("energy_total_kwh", templateSensor.render(modelEnergyTotalKWh).getBytes());
    }
}
