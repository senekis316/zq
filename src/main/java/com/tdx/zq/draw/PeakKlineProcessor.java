package com.tdx.zq.draw;

import com.tdx.zq.context.KlineApplicationContext;
import com.tdx.zq.enums.PeakShapeEnum;
import com.tdx.zq.model.Kline;
import com.tdx.zq.model.MergeKline;
import com.tdx.zq.model.PeakKline;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.util.CollectionUtils;

public class PeakKlineProcessor {

    private List<Kline> klineList;
    private Map<Long, Kline> klineMap;
    private List<MergeKline> mergeKlineList;
    private List<PeakKline> peakKlineList;
    private List<PeakKline> breakPeakKlineList;
    private List<PeakKline> jumpPeakKlineList;
    private List<PeakKline> turnPeakKlineList;
    private List<PeakKline> tendencyPeakKlineList;
    private List<PeakKline> oppositeTendencyPeakKlineList;
    private List<PeakKline> independentTendencyPeakKlineList;
    private List<PeakKline> anglePeakKlineList;
    private KlineApplicationContext klineApplicationContext;

    public PeakKlineProcessor(KlineApplicationContext klineApplicationContext) throws IOException {
        this.klineApplicationContext = klineApplicationContext;
        this.klineMap = klineApplicationContext.getKlineMap();
        this.klineList = klineApplicationContext.getKlineList();
        this.mergeKlineList = klineApplicationContext.getMergeKlineList();
        setPeakKlineList(mergeKlineList);
        setBreakPeakKlineList(peakKlineList);
        setJumpPeakKlineList(mergeKlineList, breakPeakKlineList);
        setTurningPeakKlineList(mergeKlineList, breakPeakKlineList);
        setBreakRangePeak();
        setTendencyPeakKlineList();
        exportExcel();
    }

    public List<PeakKline> getPeakKlineList() {
        return peakKlineList;
    }

    public List<PeakKline> getBreakPeakKlineList() {
        return breakPeakKlineList;
    }

    public List<PeakKline> getJumpPeakKlineList() {
        return jumpPeakKlineList;
    }

    public List<PeakKline> getTurnPeakKlineList() {
        return turnPeakKlineList;
    }

    private void setPeakKlineList(List<MergeKline> mergeKlineList) {
        List<PeakKline> peakKlineList = new ArrayList<>();
        for (int i = 1; i < mergeKlineList.size() - 1; i++) {
            MergeKline prev = mergeKlineList.get(i - 1);
            MergeKline curr = mergeKlineList.get(i);
            MergeKline next = mergeKlineList.get(i + 1);
            peakKlineList.add(new PeakKline(peakKlineList.size(), prev, curr, next));
        }
        this.peakKlineList = peakKlineList;
    }

    private void setBreakPeakKlineList(
        List<PeakKline> peakKlineList) {
        List<PeakKline> breakPeakKlineList = new ArrayList<>();
        for (int i = 0; i < peakKlineList.size() - 3; i++) {
            if (peakKlineList.get(i).getPeakShape() != PeakShapeEnum.NONE) {
                Kline middle = peakKlineList.get(i).getMergeKline().getMergeKline();
                Kline right = peakKlineList.get(i + 1).getMergeKline().getMergeKline();
                Kline second = peakKlineList.get(i + 2).getMergeKline().getMergeKline();
                Kline third = peakKlineList.get(i + 3).getMergeKline().getMergeKline();

                //波谷
                if (middle.getLow() < right.getLow()) {
                    if (middle.getLow() > second.getLow()) {
                        continue;
                    }
                    if (middle.getLow() > third.getLow()) {
                        continue;
                    }
                    peakKlineList.get(i).setIsBreakPeak(true);
                    breakPeakKlineList.add(peakKlineList.get(i));
                } else if (middle.getHigh() > right.getHigh()) {
                    if (middle.getHigh() < second.getHigh()) {
                        continue;
                    }
                    if (middle.getHigh() < third.getHigh()) {
                        continue;
                    }
                    peakKlineList.get(i).setIsBreakPeak(true);
                    breakPeakKlineList.add(peakKlineList.get(i));
                }
            }
        }
        this.breakPeakKlineList = breakPeakKlineList;
    }

    private void setJumpPeakKlineList(
        List<MergeKline> mergeKlineList,
        List<PeakKline> breakPeakKlineList) {
        List<PeakKline> jumpPeakKlineList = new ArrayList<>();
        for (int i = 0; i < breakPeakKlineList.size(); i++) {

            PeakKline breakPeakKline = breakPeakKlineList.get(i);
            Integer index = breakPeakKline.getMergeKline().getIndex();

            Kline left = mergeKlineList.get(index - 1).getMergeKline();
            Kline middle = mergeKlineList.get(index).getMergeKline();
            Kline right = mergeKlineList.get(index + 1).getMergeKline();
            Kline second = mergeKlineList.get(index + 2).getMergeKline();
            Kline third = mergeKlineList.get(index + 3).getMergeKline();

            Kline orig1 = mergeKlineList.get(index + 2).getFirstOriginKline();
            Kline orig2 = mergeKlineList.get(index + 3).getFirstOriginKline();

            if (middle.getLow() < right.getLow()) {
                if (right.getHigh() < second.getLow()
                    && second.getLow() > left.getHigh()
                    && breakPeakKline.getHighest() < orig1.getLow()) {
                    breakPeakKline.setIsJumpPeak(true);
                    jumpPeakKlineList.add(breakPeakKline);
                    continue;
                }
                if (left.getHigh() < third.getLow()
                    && right.getHigh() < third.getLow()
                    && second.getHigh() < third.getLow()
                    && second.getHigh() < orig2.getLow()
                    && breakPeakKline.getHighest() < orig2.getLow()) {
                    breakPeakKline.setIsJumpPeak(true);
                    jumpPeakKlineList.add(breakPeakKline);
                    continue;
                }
            } else if (middle.getHigh() > right.getHigh()) {
                if (left.getLow() > second.getHigh()
                    && right.getLow() > second.getHigh()
                    && breakPeakKline.getLowest() > orig1.getHigh()) {
                    breakPeakKline.setIsJumpPeak(true);
                    jumpPeakKlineList.add(breakPeakKline);
                    continue;
                }
                if (left.getLow() > third.getHigh()
                    && right.getLow() > third.getHigh()
                    && second.getLow() > third.getHigh()
                    && second.getLow() > orig2.getHigh()
                    && breakPeakKline.getLowest() > orig2.getHigh()) {
                    breakPeakKline.setIsJumpPeak(true);
                    jumpPeakKlineList.add(breakPeakKline);
                    continue;
                }
            }
        }
        this.jumpPeakKlineList = jumpPeakKlineList;
    }

    private void setTurningPeakKlineList(
        List<MergeKline> mergeKlineList,
        List<PeakKline> breakPeakKlineList) {

        for (int i = 0; i < breakPeakKlineList.size(); i++) {

            PeakKline peakKline = breakPeakKlineList.get(i);

            if (peakKline.getPeakShape() != PeakShapeEnum.NONE) {

                System.out.println(peakKline.getMergeKline().getMergeKline().getDate());

                Integer index = peakKline.getMergeKline().getIndex();
                Kline left = mergeKlineList.get(index - 1).getMergeKline();
                Kline middle = mergeKlineList.get(index).getMergeKline();
                Kline right = mergeKlineList.get(index + 1).getMergeKline();
                Kline second = mergeKlineList.get(index + 2).getMergeKline();
                Kline third = mergeKlineList.get(index + 3).getMergeKline();

                if (middle.getLow() < right.getLow()) {
                    int max = Arrays.stream(
                        new int[]{left.getHigh(), middle.getHigh(), right.getHigh(), second.getHigh(),
                            third.getHigh()}).max().getAsInt();
                    for (int j = index + 4; j < mergeKlineList.size(); j++) {
                        Kline curr = mergeKlineList.get(j).getMergeKline();
                        if (max <= curr.getHigh()) {
                            peakKline.setTurnPeak(true);
                            break;
                        } else if (middle.getLow() > curr.getLow()) {
                            break;
                        }
                    }
                } else if (middle.getHigh() > right.getHigh()) {
                    int min = Arrays.stream(
                        new int[]{left.getLow(), middle.getLow(), right.getLow(), second.getLow(),
                            third.getLow()}).min().getAsInt();
                    for (int j = index + 4; j < mergeKlineList.size(); j++) {
                        Kline curr = mergeKlineList.get(j).getMergeKline();
                        if (min >= curr.getLow()) {
                            peakKline.setTurnPeak(true);
                            break;
                        } else if (middle.getHigh() < curr.getHigh()) {
                            break;
                        }
                    }
                }
            }
        }

        this.turnPeakKlineList =
            breakPeakKlineList.stream().filter(PeakKline::isTurnPeak).collect(Collectors.toList());
    }

    public void setBreakRangePeak() {
        for (int i = 0; i < breakPeakKlineList.size() - 1; i++) {
            PeakKline curr = breakPeakKlineList.get(i);
            PeakKline next = breakPeakKlineList.get(i + 1);
            if (!curr.isRangePeak()) {
                if (curr.isJumpPeak() || curr.isTurnPeak()) {
                    if (!next.isJumpPeak() && !next.isTurnPeak()) {
                        for (int j = next.getIndex() + 1; j < peakKlineList.size(); j++) {
                            PeakKline last = peakKlineList.get(j);
                            Kline kline = last.getMergeKline().getMergeKline();
                            if (kline.getLow() > next.getLowest()
                                && kline.getHigh() < next.getHighest()) {
                                if (last.isTurnPeak() || last.isJumpPeak()) {
                                    last.setRangePeak(true);
                                }
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < breakPeakKlineList.size(); i++) {
            PeakKline curr = breakPeakKlineList.get(i);
            for (int j = i + 1; j < breakPeakKlineList.size(); j++) {
                PeakKline last = breakPeakKlineList.get(j);
                if (!last.isRangePeak() && (last.isTurnPeak() || last.isJumpPeak())) {
                    if (last.getMergeKline().getMergeKline().getHigh() < curr.getHighest()
                        && last.getMergeKline().getMergeKline().getLow() > curr.getLowest()) {
                        last.setRangePeak(true);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public void setTendencyPeakKlineList() {
        tendencyPeakKlineList = new ArrayList<>();
        for (int i = 0; i < breakPeakKlineList.size() - 1; i++) {
            PeakKline peakKline = breakPeakKlineList.get(i);
            if (peakKline.isJumpPeak() || peakKline.isTurnPeak()) {
                if (!peakKline.isRangePeak()) {
                    peakKline.setIsTendencyPeak(true);
                    tendencyPeakKlineList.add(peakKline);
                }
            }
        }

        //处理同向点
        List<PeakKline> equalDirectionPeaks = new ArrayList<>();
        equalDirectionPeaks.add(tendencyPeakKlineList.get(0));

        for (int i = 1; i < tendencyPeakKlineList.size(); i++) {
            PeakKline prev = equalDirectionPeaks.get(equalDirectionPeaks.size() - 1);
            PeakKline curr = tendencyPeakKlineList.get(i);
            if (prev.getPeakShape() == curr.getPeakShape()) {
                equalDirectionPeaks.add(curr);
            } else {
                deleteEqualDirectionPeak(equalDirectionPeaks);
                equalDirectionPeaks.clear();
                equalDirectionPeaks.add(curr);
            }
        }
        deleteEqualDirectionPeak(equalDirectionPeaks);
        this.tendencyPeakKlineList = tendencyPeakKlineList.stream().filter(PeakKline::isTendencyPeak).collect(Collectors.toList());

        // 处理反向删除逻辑
        for (int i = tendencyPeakKlineList.size() - 1; i > 0; i--) {
            List<PeakKline> peaks = new ArrayList<>();
            PeakKline curr = tendencyPeakKlineList.get(i);
            for (int j = curr.getIndex() - 3; j >= 0 && j < curr.getIndex(); j++) {
                PeakKline prev = peakKlineList.get(j);
                if (curr.getMergeKline().getIndex() - prev.getMergeKline().getIndex() <= 3
                    && !prev.isJumpPeak()
                    && prev.getPeakShape() != PeakShapeEnum.NONE
                    && prev.getPeakShape() != curr.getPeakShape()) {
                    peaks.add(prev);
                }
            }
            if (peaks.size() > 0) {
                PeakKline peak = peaks.get(0);
                if (peaks.size() == 2) {
                    if (curr.getPeakShape() == PeakShapeEnum.TOP) {
                        peak = peak.getLowest() < peaks.get(1).getLowest() ? peak: peaks.get(1);
                    } else {
                        peak = peak.getHighest() > peaks.get(1).getHighest() ? peak: peaks.get(1);
                    }
                }
                int lowest;
                int highest;
                if (curr.getPeakShape() == PeakShapeEnum.TOP) {
                    lowest = peak.getLowest();
                    highest = curr.getHighest();
                } else {
                    lowest = curr.getLowest();
                    highest = peak.getHighest();
                }
                for (int j = i - 1; j >= 0 ; j--) {
                    PeakKline range = tendencyPeakKlineList.get(j);
                    if (range.getHighest() < highest
                        && range.getLowest() > lowest) {
                        range.setRangePeak(true);
                        range.setIsTendencyPeak(false);
                    } else {
                        if (range.getPeakShape() == curr.getPeakShape()) {
                            if (range.getPeakShape() == PeakShapeEnum.TOP) {
                                if (range.getHighest() >= curr.getHighest()) {
                                    curr.setIsTendencyPeak(false);
                                } else {
                                    range.setIsTendencyPeak(false);
                                }
                            } else {
                                if (range.getLowest() <= curr.getLowest()) {
                                    curr.setIsTendencyPeak(false);
                                } else {
                                    range.setIsTendencyPeak(false);
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        this.tendencyPeakKlineList = tendencyPeakKlineList.stream().filter(PeakKline::isTendencyPeak).collect(Collectors.toList());

    }

    private void deleteEqualDirectionPeak(List<PeakKline> equalDirectionPeaks) {
        if (equalDirectionPeaks.size() > 1) {
            PeakKline max = equalDirectionPeaks.get(0);
            PeakKline min = equalDirectionPeaks.get(0);
            for (int j = 1; j < equalDirectionPeaks.size(); j++) {
                PeakKline peak = equalDirectionPeaks.get(j);
                if (peak.getPeakShape() == PeakShapeEnum.TOP) {
                    if (max.getMergeKline().getMergeKline().getHigh() >=
                        peak.getMergeKline().getMergeKline().getHigh()) {
                        peak.setIsTendencyPeak(false);
                    } else {
                        max.setIsTendencyPeak(false);
                        max = peak;
                    }
                } else {
                    if (min.getMergeKline().getMergeKline().getLow() <=
                        peak.getMergeKline().getMergeKline().getLow()) {
                        peak.setIsTendencyPeak(false);
                    } else {
                        min.setIsTendencyPeak(false);
                        min = peak;
                    }
                }
            }
        }
    }


    public void exportExcel() throws IOException {
        if (CollectionUtils.isEmpty(tendencyPeakKlineList)) return;
        try (OutputStream output = new FileOutputStream(klineApplicationContext.getOutputPath());
            SXSSFWorkbook workBook = new SXSSFWorkbook(tendencyPeakKlineList.size())) {
            Sheet sheet = workBook.createSheet();
            for (int i = 0; i < tendencyPeakKlineList.size(); i++) {
                PeakKline peakKline = tendencyPeakKlineList.get(i);
                int value = peakKline.getPeakShape() == PeakShapeEnum.TOP ? peakKline.getHighest() : peakKline.getLowest();
                long date = peakKline.getPeakDate();
                Row row = sheet.createRow(i);
                row.createCell(0, CellType.STRING).setCellValue(String.valueOf(date));
                row.createCell(1, CellType.NUMERIC).setCellValue(value);
                if (i == tendencyPeakKlineList.size() - 1) {
                    int lowest = Integer.MAX_VALUE;
                    int highest = Integer.MIN_VALUE;
                    List<Kline> klines = peakKlineList.get(peakKline.getIndex() + 1).getMergeKline().getContainKlineList();
                    int index = klines.get(klines.size() - 1).getIndex();
                    for (int j = index + 1; j < klineList.size(); j++) {
                        if (peakKline.getPeakShape() == PeakShapeEnum.TOP) {
                            if (lowest > klineList.get(j).getLow()) {
                                lowest = klineList.get(j).getLow();
                                date = klineList.get(j).getDate();
                            }
                        } else {
                            if (highest < klineList.get(j).getHigh()) {
                                highest = klineList.get(j).getHigh();
                                date = klineList.get(j).getDate();
                            }
                        }
                    }
                    value = peakKline.getPeakShape() == PeakShapeEnum.TOP ? lowest : highest;
                    row = sheet.createRow(i + 1);
                    row.createCell(0, CellType.STRING).setCellValue(String.valueOf(date));
                    row.createCell(1, CellType.NUMERIC).setCellValue(value);
                }
            }
            workBook.write(output);
            workBook.dispose();
        } finally {
            System.out.println("输出文件: " + klineApplicationContext.getOutputPath());
        }

    }

}
