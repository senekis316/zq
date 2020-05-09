package com.tdx.zq.model;

import com.tdx.zq.enums.PeakShapeEnum;

public class PeakKline {

    private boolean isDependent;

    private boolean isJumpPeak;

    private boolean isBreakPeak;

    private boolean isTendencyPeak;

    private boolean isEqualDirectPeak;

    private boolean isDependentTendencyPeak;

    private MergeKline mergeKline;

    private PeakShapeEnum peakShape;

    private int index;

    private int highest;

    private int lowest;

    public PeakKline(int index, MergeKline prev, MergeKline curr, MergeKline next) {
        this.index = index;
        this.mergeKline = curr;
        this.lowest = Math.min(Math.min(prev.getMergeKline().getLow(), curr.getMergeKline().getLow()), next.getMergeKline().getLow());
        this.highest = Math.max(Math.max(prev.getMergeKline().getHigh(), curr.getMergeKline().getHigh()), next.getMergeKline().getHigh());
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

    public int getMergeKlineIndex() {
        return mergeKline.getIndex();
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

    public boolean isEqualDirectPeak() {
        return isEqualDirectPeak;
    }

    public void setIsEqualDirectPeak(boolean isEqualDirectPeak) {
        this.isEqualDirectPeak = isEqualDirectPeak;
    }

    public boolean isTendencyPeak() {
        return isTendencyPeak;
    }

    public void setIsTendencyPeak(boolean tendencyPeak) {
        isTendencyPeak = tendencyPeak;
    }

    public boolean isDependentTendencyPeak() {
        return isDependentTendencyPeak;
    }

    public void setDependentTendencyPeak(boolean dependentTendencyPeak) {
        isDependentTendencyPeak = dependentTendencyPeak;
    }

    public int getIndex() {
        return index;
    }

    public int getHighest() {
        return highest;
    }

    public int getLowest() {
        return lowest;
    }

    public boolean isDependent() {
        return isDependent;
    }

    public void setDependent(boolean dependent) {
        isDependent = dependent;
    }
}
