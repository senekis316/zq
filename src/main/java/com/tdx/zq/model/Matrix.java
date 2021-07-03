package com.tdx.zq.model;

import com.tdx.zq.draw.PeakKlineProcessor;
import com.tdx.zq.enums.TendencyTypeEnum;
import lombok.Data;

@Data
public class Matrix {
    private long high;
    private long low;
    private long startDate;
    private long endDate;
    private TendencyTypeEnum tendency;
    private int rangeIndex;
    private long rangeLow;
    private long rangeHigh;
    private MatrixKlineRow tailRow;
    public Matrix(long high, long low, long startDate, long endDate,
                  TendencyTypeEnum tendency, int rangeIndex, long rangeLow, long rangeHigh, MatrixKlineRow tailRow) {
        this.high = high;
        this.low = low;
        this.startDate = startDate;
        this.endDate = endDate;
        this.tendency = tendency;
        this.rangeIndex = rangeIndex;
        this.rangeLow = rangeLow;
        this.rangeHigh = rangeHigh;
        this.tailRow = tailRow;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"tendency\":")
                .append(tendency);
        sb.append(",\"startDate\":")
                .append(startDate);
        sb.append(",\"endDate\":")
                .append(endDate);
        sb.append(",\"high\":")
                .append(high);
        sb.append(",\"low\":")
                .append(low);
        sb.append('}');
        return sb.toString();
    }
}
