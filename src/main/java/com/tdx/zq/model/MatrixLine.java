package com.tdx.zq.model;

import com.tdx.zq.enums.LineShapeEnum;
import com.tdx.zq.enums.MatrixType;
import lombok.Data;

import java.util.List;

@Data
public class MatrixLine {

    private int high;

    private int low;

    private PeakKline begin;

    private PeakKline end;

    private Kline kline1;

    private Kline kline2;

    private Kline kline3;

    private Kline kline4;

    private Kline kline5;

    private PeakKline peak1;

    private PeakKline peak2;

    private PeakKline peak3;

    private PeakKline peak4;

    private PeakKline peak5;

    private List<PeakKline> peaks;

    private MatrixType matrixType;

    public MatrixLine(int index, List<PeakKline> peaks) {
        this.begin = peaks.get(index);
        this.peaks = peaks;
        this.peak1 = peaks.get(index);
        this.peak2 = peaks.get(index + 1);
        this.peak3 = peaks.get(index + 2);
        this.peak4 = peaks.get(index + 3);
        this.peak5 = peaks.get(index + 4);
        this.kline1 = peak1.getCombineKline().getKline();
        this.kline2 = peak2.getCombineKline().getKline();
        this.kline3 = peak3.getCombineKline().getKline();
        this.kline4 = peak4.getCombineKline().getKline();
        this.kline5 = peak5.getCombineKline().getKline();
        this.matrixType = peak1.getShapeType() == LineShapeEnum.FLOOR ? MatrixType.UP : MatrixType.DOWN;
    }

    public boolean isValidMatrix() {
        return hasPrevFigure() && hasEndPoint();
    }

    private boolean hasPrevFigure() {
        if (matrixType == MatrixType.UP) {
            if (kline2.getHigh() >= kline5.getLow()
                    && kline3.getLow() > kline1.getLow()
                    && kline5.getLow() > kline1.getLow()) {
                return true;
            }
        } else {
            if (kline5.getHigh() >= kline2.getLow()
                    && kline3.getHigh() < kline1.getHigh()
                    && kline5.getHigh() < kline1.getHigh()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasEndPoint() {
        if (matrixType == MatrixType.UP) {
            int min = kline1.getLow();
            int max = Math.max(kline2.getHigh(), kline4.getHigh());
            for (int i = begin.getPeakIndex() + 5; i < peaks.size(); i++) {
                PeakKline currPeak = peaks.get(i);
                Kline currKline = currPeak.getCombineKline().getKline();
                if (currKline.getHigh() > max) {
                    this.end = peaks.get(i);
                    this.low = min;
                    this.high = max;
                    return true;
                }
                if (currKline.getLow() < min) {
                    return false;
                }
            }
        } else {
            int min = Math.min(kline2.getLow(), kline4.getLow());
            int max = kline1.getHigh();
            for (int i = begin.getPeakIndex() + 5; i < peaks.size(); i++) {
                PeakKline currPeak = peaks.get(i);
                Kline currKline = currPeak.getCombineKline().getKline();
                if (currKline.getLow() < min) {
                    this.end = peaks.get(i);
                    this.low = min;
                    this.high = max;
                    return true;
                }
                if (currKline.getHigh() > max) {
                    return false;
                }
            }
        }
        return false;
    }




}
