//package com.tdx.zq.model;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.tdx.zq.enums.MatrixType;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@NoArgsConstructor
//public class Matrix {
//
//    private int low;
//
//    private int high;
//
//    private int begin;
//
//    private int end;
//
//    @JsonIgnore
//    private MatrixType matrixType;
//
//    @JsonIgnore
//    private PeakKline matrixBeginPeakKline;
//
//    @JsonIgnore
//    private PeakKline matrixEndPeakKline;
//
//    @JsonIgnore
//    private PeakKline matrixTendencyEndKline;
//
//    public Matrix(MatrixLine matrixLine) {
//        this.low = matrixLine.getLow();
//        this.high = matrixLine.getHigh();
//        this.end = matrixLine.getKline5().getDate();
//        this.begin = matrixLine.getKline2().getDate();
//        matrixType = matrixLine.getMatrixType();
//        this.matrixBeginPeakKline = matrixLine.getPeak2();
//        this.matrixEndPeakKline = matrixLine.getPeak5();
//        this.matrixTendencyEndKline = matrixLine.getEnd();
//    }
//
//}
