package com.hoymiles.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class RealData implements Serializable {
    private String dtuSn;
    private List<SGSMO> inverters;
    private List<PvMO> panels;
    private List<MeterMO> meters;
    private float powerTotal;
    private int energyTotal;
    private int energyToday;
    private LocalDateTime time;               // timestamp

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

    @Getter
    @Setter
    @Builder
    public static class MeterMO implements Serializable {
        private String sn;           // meter serial number
        private String displaySn;
        private int type;            // meter type
        private String ctAmps;       // CT Amps

        private float powerA;        // Phase A Power (x1W)
        private float powerB;        // Phase B Power (x1W)
        private float powerC;        // Phase C Power (x1W)
        private float powerTotal;    // Total Power (x1W)

        private float voltageA;      // Phase A Voltage (x1V)
        private float voltageB;      // Phase B Voltage (x1V)
        private float voltageC;      // Phase V Voltage (x1V)

        private float currentA;      // Phase A Current (x1A)
        private float currentB;      // Phase B Current (x1A)
        private float currentC;      // Phase C Current (x1A)

        private float energyImportedA;  // Phase A energy Imported   (x1W)
        private float energyImportedB;  // Phase B energy Imported    (x1W)
        private float energyImportedC;  // Phase C energy Imported    (x1W)
        private float energyImportedTotal;  // Total energy Imported  (x1W)

        private float energyExportedA;  // Phase A energy Exported    (x1W)
        private float energyExportedB;  // Phase B energy Exported    (x1W)
        private float energyExportedC;  // Phase C energy Exported    (x1W)
        private float energyExportedTotal;  // Total energy Exported  (x1W)

        private float powerFactorA;  // Phase A Power Factor (0.0 - 1.0)
        private float powerFactorB;  // Phase B Power Factor (0.0 - 1.0)
        private float powerFactorC;  // Phase C Power Factor (0.0 - 1.0)
        private float powerFactorTotal;  // Total Power Factor (0.0 - 1.0)

        private int time;           // timestamp
    }
    
}
