package com.tdx.zq.model;

import java.util.List;
import org.springframework.util.CollectionUtils;

public class MergeKline {

    private int index;
    private Kline mergeKline;
    private List<Kline> containKlineList;

    public MergeKline(int index, Kline mergeKline, List<Kline> containKlineList) {
        this.index = index;
        this.mergeKline = mergeKline;
        this.containKlineList = containKlineList;
    }

    public int getIndex() {
        return index;
    }

    public Kline getMergeKline() {
        return mergeKline;
    }

    public List<Kline> getContainKlineList() {
        return containKlineList;
    }

    public Kline getFirstOriginKline() {
        return CollectionUtils.isEmpty(containKlineList) ? mergeKline : containKlineList.get(0);
    }

}
