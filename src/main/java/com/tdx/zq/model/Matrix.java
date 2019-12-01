package com.tdx.zq.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tdx.zq.enums.MatrixType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Matrix {

    private int low;

    private int high;

    private int begin;

    private int end;

    @JsonIgnore
    private MatrixType matrixType;

    @JsonIgnore
    private PeakKline beginPeakKline;

    @JsonIgnore
    private PeakKline endPeakKline;

    public Matrix(MatrixLine matrixLine) {
        this.low = matrixLine.getLow();
        this.high = matrixLine.getHigh();
        this.end = matrixLine.getKline5().getDate();
        this.begin = matrixLine.getKline2().getDate();
    }

}
