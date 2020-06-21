package com.tdx.zq.model;

import com.tdx.zq.enums.PeakShapeEnum;
import lombok.Data;

@Data
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

}