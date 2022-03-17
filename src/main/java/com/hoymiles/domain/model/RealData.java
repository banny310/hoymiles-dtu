package com.hoymiles.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
public class RealData implements Serializable {
    private String dtuSn;
    private List<SGSMO> inverters;
    private List<PvMO> panels;
    private float powerTotal;
    private int energyTotal;
    private int energyToday;
    private int time;               // timestamp

    @Getter
    @Setter
    @Builder
    public static class SGSMO implements Serializable {
        private String sn;
        private int version;
        private float gridVoltage;          // grid voltage (x10V)
        private float gridFreq;             // grid frequency (x100Hz)
        private float gridPower;            // grid power (x10W)
        private float gridReactivePower;    // grid reactive power (moc bierna) (x10VA)
        private float gridCurrent;          // grid current (x100A)
        private float powerFactor;          // power factor (x100)
        private float temp;                 // inverter temp (x10C)
        private int link;                   // link status
    }

    @Getter
    @Setter
    @Builder
    public static class PvMO implements Serializable {
        private String sn;          // inverter serial number
        private int position;       // inverter port number
        private float voltage;      // voltage (x10V)
        private float current;      // current (x100A)
        private float power;        // power (x10W)
        private int energyTotal;    // energy total (Wh)
        private int energyToday;    // energy daily (Wh)
    }
}
