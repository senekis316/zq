package com.tdx.zq.model;

import java.util.List;

public class MergeKline {

    private Kline mergeKline;
    private List<Kline> containKlines;

    public MergeKline(Kline mergeKline, List<Kline> containKlines) {
        this.mergeKline = mergeKline;
        this.containKlines = containKlines;
    }

    public Kline getMergeKline() {
        return mergeKline;
    }

    public List<Kline> getContainKlines() {
        return containKlines;
    }

}
