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
        // 520 -> V00.02.08
        // 522 -> V00.02.10
        private int dtuSw;

        // 37122 -> H09.01.02
        private int dtuHw;
    }

    @Getter
    @Setter
    @Builder
    public static final class SgsInfo {
        private String invSn;
        private int invSw;
        private int invHw;
        private int link;

        public boolean isConnected() {
            return link != 0;
        }
    }
}



