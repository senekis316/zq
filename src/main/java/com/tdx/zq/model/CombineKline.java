package com.tdx.zq.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CombineKline {

    private Kline kline;
    private List<Kline> contains;
    private int originalIndex;

    public CombineKline(Kline kLine, int originalIndex) {
        this.kline = kLine;
        this.originalIndex = originalIndex;
        this.contains = new ArrayList<>();
        contains.add(new Kline(kline));
    }

}
