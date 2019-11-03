package com.tdx.zq.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tdx.zq.enums.LineShapeEnum;
import com.tdx.zq.enums.MatrixLineType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatrixLine {

    private int low;

    private int high;

    private int begin;

    private int end;

    @JsonIgnore
    private MatrixLineType matrixLineType;

    @JsonIgnore
    private PeakKline prevPeakKline;

    @JsonIgnore
    private PeakKline lastPeakKline;

    @JsonIgnore
    private PeakKline suffPeakKline;

    public MatrixLine(int low, int high, int begin, int end, PeakKline prevPeakKline, PeakKline lastPeakKline, PeakKline suffPeakKline, MatrixLineType matrixLineType) {
        this.low = low;
        this.high = high;
        this.begin = begin;
        this.end = end;
        this.prevPeakKline = prevPeakKline;
        this.lastPeakKline = lastPeakKline;
        this.suffPeakKline = suffPeakKline;
        this.matrixLineType = matrixLineType;
    }

}
