package com.hoymiles.infrastructure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hoymiles.domain.model.RealData;
import com.hoymiles.infrastructure.dtu.DtuClient;
import com.hoymiles.infrastructure.dtu.utils.DateUtil;
import com.hoymiles.infrastructure.gson.DateAdapter;
import com.hoymiles.infrastructure.mqtt.MqttConnectionConfigProvider;
import com.hoymiles.infrastructure.repository.dto.DtuRealDataDTO;
import com.hoymiles.infrastructure.repository.dto.InvRealDataDTO;
import com.hoymiles.infrastructure.repository.dto.PvRealDataDTO;
import com.hoymiles.infrastructure.repository.mapper.NumberFormatter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.*;

@Log4j2
public class BeanFactory {
    @Produces
    @Singleton
    @Named("mainQueue")
    public BlockingQueue<Runnable> getMainQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Produces
    @Singleton
    @Named("mainExecutor")
    public ScheduledExecutorService getExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
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
    public Config getConfig() {
        return ConfigFactory.load();
    }

    @Produces
    @Singleton
    public IMqttClient getMqttClient(@NotNull MqttConnectionConfigProvider connectionUriProvider) throws MqttException {
        String publisherId = "mqtt_hoymiles_dtu_" + UUID.randomUUID();
        IMqttClient mqttClient = new MqttClient(connectionUriProvider.getConnectionUri(), publisherId, new MemoryPersistence());
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
    public ModelMapper getModelMapper(@NotNull NumberFormatter formatter) {
        ModelMapper modelMapper = new ModelMapper();
        Converter<Float, Float> format0fc = ctx -> formatter.format1fd(ctx.getSource());
        Converter<Float, Float> divide1000f = ctx -> formatter.format3fd(ctx.getSource() / 1000f);
        Converter<Integer, Float> divide1000fInt = ctx -> formatter.format3fd(Float.valueOf(ctx.getSource()) / 1000f);
        Converter<Integer, Date> timestamp2Date = ctx -> new Date(ctx.getSource() * 1000L);
        modelMapper.typeMap(RealData.class, DtuRealDataDTO.class)
                .addMappings(mapper -> {
                    mapper.using(format0fc).map(RealData::getPowerTotal, DtuRealDataDTO::setPowerTotalW);
                    mapper.using(divide1000f).map(RealData::getPowerTotal, DtuRealDataDTO::setPowerTotalKW);
                    mapper.map(RealData::getEnergyToday, DtuRealDataDTO::setEnergyTodayWh);
                    mapper.using(divide1000fInt).map(RealData::getEnergyToday, DtuRealDataDTO::setEnergyTodayKWh);
                    mapper.using(divide1000fInt).map(RealData::getEnergyTotal, DtuRealDataDTO::setEnergyTotalKWh);
                    mapper.map(RealData::getEnergyTotal, DtuRealDataDTO::setEnergyTotalWh);
                    mapper.using(timestamp2Date).map(RealData::getTime, DtuRealDataDTO::setLastSeen);
                });


        Converter<Float, Float> multiply10 = ctx -> ctx.getSource() * 10f;
        modelMapper.typeMap(RealData.SGSMO.class, InvRealDataDTO.class)
                .addMappings(mapper -> {
                    mapper.using(multiply10).map(RealData.SGSMO::getPowerFactor, InvRealDataDTO::setPowerFactor);
                    mapper.using(timestamp2Date).map(RealData.SGSMO::getTime, InvRealDataDTO::setLastSeen);
                });

        modelMapper.typeMap(RealData.PvMO.class, PvRealDataDTO.class)
                .addMappings(mapper -> {
                    mapper.using(timestamp2Date).map(RealData.PvMO::getTime, PvRealDataDTO::setLastSeen);
                });

        modelMapper.validate();
        return modelMapper;
    }
}
