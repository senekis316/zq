package com.tdx.zq.enums;


import java.awt.*;

public enum PointType {

    WEAK_B1("WB1"), B1("B1"), B2("B2"), WEAK_S1("WS1"), S1("S1"), S2("S2"),
    SIMILAR_WEAK_B2("SWB2"), SIMILAR_B2("SB2"), SIMILAR_WEAK_S2("SWS2"), SIMILAR_S2("SS2"), B3("B3"), S3("S3");

    private String code;

    PointType(String code) {
        this.code = code;
    }

    public static PointType create(String code) {
        for (PointType pointType : PointType.values()) {
            if (pointType.code.equalsIgnoreCase(code)) {
                return pointType;
            }
        }
        return null;
    }

}



