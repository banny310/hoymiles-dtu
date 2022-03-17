package com.hoymiles.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public final class AppInfo {
    private String dtuSn;
    private DtuInfo dtuInfo;
    private List<SgsInfo> sgsInfo;

    @Getter
    @Setter
    @Builder
    public static final class DtuInfo {
        private int dtuSw;
        private int dtuHw;
    }

    @Getter
    @Setter
    @Builder
    public static final class SgsInfo {
        private String sn;
        private int link;

        public boolean isConnected() {
            return link != 0;
        }
    }
}



