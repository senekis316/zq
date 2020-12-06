package com.tdx.zq.model;

import com.tdx.zq.enums.PeakShapeEnum;
import lombok.Data;

public class PeakKlinePrint {

  private Kline mergeKline;

  private boolean isTurnPeak;

  private boolean isJumpPeak;

  private boolean isBreakPeak;

  private boolean isRangePeak;

  private boolean isTendencyPeak;

  private PeakShapeEnum peakShape;

  public PeakKlinePrint(PeakKline peakKline) {
    this.isTurnPeak = peakKline.isTurnPeak();
    this.isJumpPeak = peakKline.isJumpPeak();
    this.isBreakPeak = peakKline.isBreakPeak();
    this.isRangePeak = peakKline.isRangePeak();
    this.isTendencyPeak = peakKline.isTendencyPeak();
    this.peakShape = peakKline.getPeakShape();
    this.mergeKline = peakKline.getMergeKline().getMergeKline();
  }

  public Kline getMergeKline() {
    return mergeKline;
  }

  public void setMergeKline(Kline mergeKline) {
    this.mergeKline = mergeKline;
  }

  public boolean isTurnPeak() {
    return isTurnPeak;
  }

  public void setTurnPeak(boolean turnPeak) {
    isTurnPeak = turnPeak;
  }

  public boolean isJumpPeak() {
    return isJumpPeak;
  }

  public void setJumpPeak(boolean jumpPeak) {
    isJumpPeak = jumpPeak;
  }

  public boolean isBreakPeak() {
    return isBreakPeak;
  }

  public void setBreakPeak(boolean breakPeak) {
    isBreakPeak = breakPeak;
  }

  public boolean isRangePeak() {
    return isRangePeak;
  }

  public void setRangePeak(boolean rangePeak) {
    isRangePeak = rangePeak;
  }

  public boolean isTendencyPeak() {
    return isTendencyPeak;
  }

  public void setTendencyPeak(boolean tendencyPeak) {
    isTendencyPeak = tendencyPeak;
  }

  public PeakShapeEnum getPeakShape() {
    return peakShape;
  }

  public void setPeakShape(PeakShapeEnum peakShape) {
    this.peakShape = peakShape;
  }
}