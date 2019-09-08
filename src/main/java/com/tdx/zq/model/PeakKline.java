package com.tdx.zq.model;

import lombok.Data;

@Data
public class PeakKline{

    private CombineKline combineKline;

    private int combineIndex;

    public PeakKline(CombineKline combineKline, int combineIndex) {
        this.combineKline = combineKline;
        this.combineIndex = combineIndex;
    }
}
