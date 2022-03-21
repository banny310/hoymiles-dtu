package com.hoymiles.domain;


import com.hoymiles.domain.model.AppInfo;
import com.hoymiles.domain.model.RealData;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@Dependent
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MetricsService {
    private static final String REAL_DATA_STORAGE_FILE = "./data/real_data.dat";

    private final IDtuRepository dtuRepository;
    private final IMqttRepository mqttRepository;

    public @Nullable RealData getRealData(@NotNull AppInfo appInfo) {
        RealData realData = dtuRepository.getRealData(appInfo);

        int link = realData.getInverters().stream()
                .mapToInt(RealData.SGSMO::getLink)
                .reduce(0, (result, el) -> result |= el);
        if (link > 0) {
            // inverters are online
            storeRealData(realData);
            return realData;
        } else {
            // inverters are offline
            realData = readRealData();
            return (realData != null) ? zeroData(realData) : null;
        }
    }

    private void storeRealData(RealData realData) {
        try {
            FileOutputStream fos = new FileOutputStream(REAL_DATA_STORAGE_FILE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(realData);
            oos.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private @Nullable RealData readRealData() {
        try {
            FileInputStream fis = new FileInputStream(REAL_DATA_STORAGE_FILE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            RealData realData = (RealData) ois.readObject();
            ois.close();
            return realData;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private RealData zeroData(RealData realData) {
        realData.setPowerTotal(0);
        realData.getInverters().forEach(sgs -> {
            sgs.setGridPower(0);
            sgs.setGridVoltage(0);
            sgs.setGridFreq(0);
            sgs.setGridCurrent(0);
            sgs.setGridReactivePower(0);
            sgs.setPowerFactor(0);
            sgs.setLink(0);
            sgs.setTemp(0);
        });
        realData.getPanels().forEach(pv -> {
            pv.setPower(0);
            pv.setVoltage(0);
        });
        return realData;
    }
}
