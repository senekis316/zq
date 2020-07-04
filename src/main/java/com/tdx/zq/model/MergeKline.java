package com.tdx.zq.model;

import java.util.ArrayList;
import java.util.List;
import org.springframework.util.CollectionUtils;

public class MergeKline {

    private int index;
    private Kline mergeKline;
    private List<Kline> containKlineList;

    public MergeKline(int index, Kline mergeKline) {
        this.index = index;
        this.mergeKline = mergeKline;
        this.containKlineList = new ArrayList<>();
        containKlineList.add(mergeKline);
    }

    public int getIndex() {
        return index;
    }

    public void setMergeKline(Kline mergeKline) {
        this.mergeKline = mergeKline;
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

    public Kline getLastOriginKline() {
        return CollectionUtils.isEmpty(containKlineList) ? mergeKline : containKlineList.get(containKlineList.size() - 1);
    }

    public int getLow() {
        return this.mergeKline.getLow();
    }

    public int getHigh() {
        return this.mergeKline.getHigh();
    }

}
