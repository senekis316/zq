package com.tdx.zq.model;

import com.tdx.zq.enums.PeakShapeEnum;
import com.tdx.zq.enums.TendencyTypeEnum;

public class MatrixKlineRow {
    private long low;
    private long high;
    private long date;
    private PeakShapeEnum shape;
    private TendencyTypeEnum tendency;
    private int idx;
    public MatrixKlineRow(long low, long high, long date, PeakShapeEnum shape, int idx) {
        this.low = low;
        this.high = high;
        this.date = date;
        this.shape = shape;
        this.idx = idx;
    }

    public long getLow() {
        return low;
    }

    public long getHigh() {
        return high;
    }

    public long getDate() {
        return date;
    }

    public PeakShapeEnum getShape() {
        return shape;
    }

    public TendencyTypeEnum getTendency() {
        return tendency;
    }

    public int getIdx() {
        return this.idx;
    }
}
