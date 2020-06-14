package com.tdx.zq.model;

import com.tdx.zq.enums.PeakShapeEnum;
import java.util.List;


public class PeakKline {

    private double angle;

    private boolean isDependent;

    private boolean isTurnPeak;

    private boolean isJumpPeak;

    private boolean isBreakPeak;

    private boolean isTendencyPeak;

    private boolean isEqualDirectPeak;

    private boolean isDependentTendencyPeak;

    private boolean isRangePeak;

    private MergeKline mergeKline;

    private PeakShapeEnum peakShape;

    private int index;

    private int highest;

    private int lowest;

    private long peakDate;

    public PeakKline(int index, MergeKline prev, MergeKline curr, MergeKline next) {
        this.index = index;
        this.mergeKline = curr;
        this.lowest = Math.min(Math.min(prev.getMergeKline().getLow(), curr.getMergeKline().getLow()), next.getMergeKline().getLow());
        this.highest = Math.max(Math.max(prev.getMergeKline().getHigh(), curr.getMergeKline().getHigh()), next.getMergeKline().getHigh());
        setPeakShape(prev, curr, next);
        setPeakDate();
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

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public void setPeakDate() {
        List<Kline> klineList = this.getMergeKline().getContainKlineList();
        for (Kline kline : klineList) {
            if (peakShape == PeakShapeEnum.TOP) {
                if (kline.getHigh() == highest) {
                    this.peakDate = kline.getDate();
                }
            } else if (peakShape == PeakShapeEnum.FLOOR) {
                if (kline.getLow() == lowest) {
                    this.peakDate = kline.getDate();
                }
            }
        }

    }

    public long getPeakDate() {
        return peakDate;
    }

    public boolean isTurnPeak() {
        return isTurnPeak;
    }

    public void setTurnPeak(boolean turnPeak) {
        isTurnPeak = turnPeak;
    }

    public boolean isRangePeak() {
        return isRangePeak;
    }

    public void setRangePeak(boolean rangePeak) {
        isRangePeak = rangePeak;
    }
}
