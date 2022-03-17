package com.hoymiles.infrastructure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hoymiles.domain.model.RealData;
import com.hoymiles.infrastructure.dtu.DtuClient;
import com.hoymiles.infrastructure.dtu.utils.DateUtil;
import com.hoymiles.infrastructure.gson.DateAdapter;
import com.hoymiles.infrastructure.repository.dto.RealDataDTO;
import com.hoymiles.infrastructure.repository.mapper.NumberFormatter;
import com.typesafe.config.Config;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;

import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Log4j2
public class BeanFactory {
    @Produces
    @Singleton
    public Config getConfig() {
        return com.typesafe.config.ConfigFactory.load();
    }

    @Produces
    @Singleton
    public Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateAdapter<>(DateUtil.ISO8601TimeZone))
                .create();
    }

    @Produces
    @Singleton
    public IMqttClient getMqttClient(@NotNull Config config) throws MqttException {
        String publisherId = "hoymiles_solar";
        String mqttUri = String.format("tcp://%s:%d", config.getString("mqtt.host"), config.getInt("mqtt.port"));
        IMqttClient mqttClient = new MqttClient(mqttUri, publisherId);
        return mqttClient;
    }

    @Produces
    @Singleton
    public DtuClient getDtuClient(@NotNull Config config, @NotNull BeanManager beanManager) {
        DtuClient client = new DtuClient(beanManager);
        return client;
    }

    @Produces
    @Singleton
    @Named("main")
    public BlockingQueue<Runnable> getMainQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Produces
    @Singleton
    public ModelMapper getModelMapper(@NotNull NumberFormatter formatter) {
        ModelMapper modelMapper = new ModelMapper();
        Converter<Float, Float> format0fc = ctx -> formatter.format1fd(ctx.getSource());
        Converter<Float, Float> divide1000f = ctx -> formatter.format3fd(ctx.getSource() / 1000f);
        Converter<Integer, Float> divide1000fInt = ctx -> formatter.format3fd(Float.valueOf(ctx.getSource()) / 1000f);
        Converter<Integer, Date> timestamp2Date = ctx -> new Date(ctx.getSource() * 1000L);
        modelMapper.typeMap(RealData.class, RealDataDTO.class)
                .addMappings(mapper -> {
                    mapper.using(format0fc).map(RealData::getPowerTotal, RealDataDTO::setPowerTotalW);
                    mapper.using(divide1000f).map(RealData::getPowerTotal, RealDataDTO::setPowerTotalKW);
                    mapper.map(RealData::getEnergyToday, RealDataDTO::setEnergyTodayWh);
                    mapper.using(divide1000fInt).map(RealData::getEnergyToday, RealDataDTO::setEnergyTodayKWh);
                    mapper.using(divide1000fInt).map(RealData::getEnergyTotal, RealDataDTO::setEnergyTotalKWh);
                    mapper.map(RealData::getEnergyTotal, RealDataDTO::setEnergyTotalWh);
                    mapper.using(timestamp2Date).map(RealData::getTime, RealDataDTO::setLastSeen);
                });

        modelMapper.validate();
        return modelMapper;
    }
}
