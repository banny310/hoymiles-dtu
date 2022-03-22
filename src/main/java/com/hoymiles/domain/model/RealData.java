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
        private float gridVoltage;          // grid voltage
        private float gridFreq;             // grid frequency
        private float gridPower;            // grid power
        private float gridReactivePower;    // grid reactive power (moc bierna VA (var))
        private float gridCurrent;          // grid current
        private float powerFactor;          // power factor (0.0 - 1.0)
        private float temp;                 // inverter temp
        private int link;                   // link status
        private int time;                   // timestamp
    }

    @Getter
    @Setter
    @Builder
    public static class PvMO implements Serializable {
        private String sn;          // inverter serial number
        private int port;           // inverter port number
        private float voltage;      // voltage (x10V)
        private float current;      // current (x100A)
        private float power;        // power (x10W)
        private int energyTotal;    // energy total (Wh)
        private int energyToday;    // energy daily (Wh)
        private int time;           // timestamp
    }
}
