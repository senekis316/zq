package com.tdx.zq.model;

import com.tdx.zq.enums.PeakShapeEnum;


public class PeakKline {

    private boolean isJumpPeak;

    private boolean isBreakPeak;

    private MergeKline mergeKline;

    private PeakShapeEnum peakShape;

    public PeakKline(MergeKline mergeKline) {
        this.mergeKline = mergeKline;
        this.peakShape = PeakShapeEnum.NONE;
    }

    public PeakKline(MergeKline prev, MergeKline curr, MergeKline next) {
        this.mergeKline = curr;
        setPeakShape(prev, curr, next);
    }

    private void setPeakShape(MergeKline prev, MergeKline curr, MergeKline next) {
        Kline left = prev.getMergeKline();
        Kline middle = curr.getMergeKline();
        Kline right = next.getMergeKline();
        if (middle.getHigh() > left.getHigh() && middle.getHigh() > right.getHigh()) {
            this.peakShape = PeakShapeEnum.TOP;
        } else if (middle.getLow() < left.getLow() && middle.getLow() < right.getLow()) {
            this.peakShape = PeakShapeEnum.FLOOR;
        } else {
            this.peakShape = PeakShapeEnum.NONE;
        }
    }

    public MergeKline getMergeKline() {
        return mergeKline;
    }

    public PeakShapeEnum getPeakShape() {
        return peakShape;
    }

    public void setIsBreakPeak(boolean isBreakPeak) {
        this.isBreakPeak = isBreakPeak;
    }

    public boolean isBreakPeak() {
        return isBreakPeak;
    }

    public boolean isJumpPeak() {
        return isJumpPeak;
    }

    public void setIsJumpPeak(boolean isJumpPeak) {
        this.isJumpPeak = isJumpPeak;
    }

    //    private int peakIndex;
//
//    private int combineIndex;
//
//
//    private MergeKline combineKline;
//
//    private List<MergeKline> combineKlines;
//
//    private LineReserveTypeEnum reserveType = LineReserveTypeEnum.NONE;
//
//    public PeakKline(MergeKline combineKline, int combineIndex, LineShapeEnum shapeType) {
//        this.combineKline = combineKline;
//        this.combineIndex = combineIndex;
//        this.shapeType = shapeType;
//    }
//
//    public PeakKline(List<MergeKline> combineKlines, MergeKline combineKline, int combineIndex, LineShapeEnum shapeType) {
//        this.shapeType = shapeType;
//        this.combineKline = combineKline;
//        this.combineIndex = combineIndex;
//        this.combineKlines = combineKlines;
//    }


}
