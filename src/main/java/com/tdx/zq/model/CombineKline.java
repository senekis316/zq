package com.tdx.zq.model;

import lombok.Data;

@Data
public class CombineKline {

    private Kline kline;
    private int originalIndex;

    public CombineKline(Kline kLine, int originalIndex) {
        this.kline = kLine;
        this.originalIndex = originalIndex;
    }

}
