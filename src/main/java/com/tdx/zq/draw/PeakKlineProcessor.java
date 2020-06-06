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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.util.CollectionUtils;

public class PeakKlineProcessor {

    private List<Kline> klineList;
    private Map<Long, Kline> klineMap;
    private List<MergeKline> mergeKlineList;
    private List<PeakKline> peakKlineList;
    private List<PeakKline> breakPeakKlineList;
    private List<PeakKline> jumpPeakKlineList;
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
        setTendencyPeakKlineList(mergeKlineList, breakPeakKlineList);
        setOppositeTendencyPeakKline(tendencyPeakKlineList);
        setOppositeTendencyPeakKline(oppositeTendencyPeakKlineList);
        setInDependentTendencyPeakKlineList();
        setTendencyAngle();
        exportExcel();
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

    public List<PeakKline> getPeakKlineList() {
        return peakKlineList;
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

            Kline origin2 = mergeKlineList.get(index + 2).getFirstOriginKline();
            Kline origin3 = mergeKlineList.get(index + 3).getFirstOriginKline();

            if (middle.getLow() < right.getLow()) {
                if (right.getHigh() < second.getLow()
                    && second.getLow() > left.getHigh()
                    && origin2.getLow() > right.getHigh()
                    && origin2.getLow() > left.getHigh()) {
                    breakPeakKline.setIsJumpPeak(true);
                    jumpPeakKlineList.add(breakPeakKline);
                    continue;
                }
                if (left.getHigh() < third.getLow()
                    && right.getHigh() < third.getLow()
                    && second.getHigh() < third.getLow()
                    && origin3.getLow() > left.getHigh()
                    && origin3.getLow() > right.getHigh()
                    && origin3.getLow() > second.getHigh()) {
                    breakPeakKline.setIsJumpPeak(true);
                    jumpPeakKlineList.add(breakPeakKline);
                    continue;
                }
            } else if (middle.getHigh() > right.getHigh()) {
                if (left.getLow() > second.getHigh()
                    && right.getLow() > second.getHigh()
                    && right.getLow() > origin2.getHigh()
                    && left.getLow() > origin2.getHigh()) {
                    breakPeakKline.setIsJumpPeak(true);
                    jumpPeakKlineList.add(breakPeakKline);
                    continue;
                }
                if (left.getLow() > third.getHigh()
                    && right.getLow() > third.getHigh()
                    && second.getLow() > third.getHigh()
                    && left.getLow() > origin3.getHigh()
                    && right.getLow() > origin3.getHigh()
                    && second.getLow() > origin3.getHigh()) {
                    breakPeakKline.setIsJumpPeak(true);
                    jumpPeakKlineList.add(breakPeakKline);
                    continue;
                }
            }
        }
        this.jumpPeakKlineList = jumpPeakKlineList;
    }

    private void setTendencyPeakKlineList(
        List<MergeKline> mergeKlineList,
        List<PeakKline> breakPeakKlineList) {

        for (int i = 0; i < breakPeakKlineList.size(); i++) {

            PeakKline peakKline = breakPeakKlineList.get(i);

            if (peakKline.getPeakShape() != PeakShapeEnum.NONE) {

                if (peakKline.isJumpPeak()) {
                    peakKline.setIsTendencyPeak(true);
                    continue;
                }

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
                            peakKline.setIsTendencyPeak(true);
                            while (i < breakPeakKlineList.size() - 1) {
                                if (breakPeakKlineList.get(i + 1).getMergeKline().getMergeKline().getDate() < curr.getDate()) {
                                    i++;
                                } else {
                                    break;
                                }
                            }
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
                            peakKline.setIsTendencyPeak(true);
                            while (i < breakPeakKlineList.size() - 1) {
                                if (breakPeakKlineList.get(i + 1).getMergeKline().getMergeKline().getDate() < curr.getDate()) {
                                    i++;
                                } else {
                                    break;
                                }
                            }
                            break;
                        } else if (middle.getHigh() < curr.getHigh()) {
                            break;
                        }
                    }
                }
            }
        }

        this.tendencyPeakKlineList =
            breakPeakKlineList.stream().filter(PeakKline::isTendencyPeak).collect(Collectors.toList());

    }

    private void setInDependentTendencyPeakKlineList() {

        for (int i = oppositeTendencyPeakKlineList.size() - 1; i >= 0; i--) {
            PeakKline curr = oppositeTendencyPeakKlineList.get(i);

            List<PeakKline> reversePeakList = new ArrayList<>();
            for (int j = curr.getIndex() - 1; j >= 0; j--) {
                if (peakKlineList.get(j).getPeakShape() != PeakShapeEnum.NONE) {
                    if (peakKlineList.get(j).getPeakShape() != curr.getPeakShape()) {
                        if (curr.getMergeKline().getIndex() - peakKlineList.get(j).getMergeKline().getIndex() < 4) {
                            reversePeakList.add(peakKlineList.get(j));
                        } else {
                            break;
                        }
                    }
                }
            }

            if (reversePeakList.size() == 2) {
                Kline left = reversePeakList.get(1).getMergeKline().getMergeKline();
                Kline right = reversePeakList.get(0).getMergeKline().getMergeKline();
                if (curr.getPeakShape() == PeakShapeEnum.TOP) {
                    if (left.getLow() < right.getLow()) {
                        reversePeakList.remove(0);
                    } else {
                        reversePeakList.remove(1);
                    }
                } else {
                    if (left.getHigh() > right.getHigh()) {
                        reversePeakList.remove(0);
                    } else {
                        reversePeakList.remove(1);
                    }
                }
            }

            if (reversePeakList.size() == 1 && !curr.isJumpPeak()) {
                int min = Math.min(curr.getMergeKline().getMergeKline().getLow(), reversePeakList.get(0).getMergeKline().getMergeKline().getLow());
                int max = Math.max(curr.getMergeKline().getMergeKline().getHigh(), reversePeakList.get(0).getMergeKline().getMergeKline().getHigh());
                while(i - 2 >= 0) {
                    int min1 = Math.min(oppositeTendencyPeakKlineList.get(i - 1).getMergeKline().getMergeKline().getLow(),
                        oppositeTendencyPeakKlineList.get(i - 2).getMergeKline().getMergeKline().getLow());
                    int max1 = Math.max(oppositeTendencyPeakKlineList.get(i - 1).getMergeKline().getMergeKline().getHigh(),
                        oppositeTendencyPeakKlineList.get(i - 2).getMergeKline().getMergeKline().getHigh());
                    if (min < min1 && max > max1) {
                        oppositeTendencyPeakKlineList.get(i - 1).setDependentTendencyPeak(true);
                        oppositeTendencyPeakKlineList.get(i - 2).setDependentTendencyPeak(true);
                        i -= 2;
                    } else {
                        break;
                    }
                }
            }
        }

        this.independentTendencyPeakKlineList = oppositeTendencyPeakKlineList.stream()
            .filter(peakKline -> !peakKline.isDependentTendencyPeak()).collect(Collectors.toList());

        deleteContainTendencyPeak();

    }

    private void setOppositeTendencyPeakKline(List<PeakKline> tendencyPeakKlineList) {

        List<PeakKline> klineList = new ArrayList<>();
        for (int i = tendencyPeakKlineList.size() - 1; i >= 0; i--) {
            if (!tendencyPeakKlineList.get(i).isEqualDirectPeak()) {
                if (klineList.size() == 0) {
                    klineList.add(tendencyPeakKlineList.get(i));
                } else if (tendencyPeakKlineList.get(i).getPeakShape() != klineList.get(0).getPeakShape()) {
                    klineList.add(tendencyPeakKlineList.get(i));
                } else if (klineList.size() == 1) {
                    Kline left = tendencyPeakKlineList.get(i).getMergeKline().getMergeKline();
                    Kline right = klineList.get(0).getMergeKline().getMergeKline();
                    if (klineList.get(0).getPeakShape() == PeakShapeEnum.TOP) {
                        if (left.getHigh() <= right.getHigh()) {
                            tendencyPeakKlineList.get(i).setIsEqualDirectPeak(true);
                        } else {
                            klineList.get(0).setIsEqualDirectPeak(true);
                        }
                    } else {
                        if (left.getLow() >= right.getLow()) {
                            tendencyPeakKlineList.get(i).setIsEqualDirectPeak(true);
                        } else {
                            klineList.get(0).setIsEqualDirectPeak(true);
                        }
                    }
                    i += 2;
                    klineList.clear();
                } else if (klineList.size() == 2) {
                    klineList.clear();
                    i += 1;
                } else {
                    List<PeakKline> sortKlineList = new ArrayList<>();
                    for (int j = 1; j < klineList.size(); j++) {
                        sortKlineList.add(klineList.get(j));
                    }
                    sortKlineList.sort(new Comparator<PeakKline>() {
                        @Override
                        public int compare(PeakKline o1, PeakKline o2) {
                            Kline k1 = o1.getMergeKline().getMergeKline();
                            Kline k2 = o2.getMergeKline().getMergeKline();

                            if (o1.getPeakShape() == PeakShapeEnum.TOP) {
                                if (k1.getHigh() > k2.getHigh()) {
                                    return -1;
                                } else if (k1.getLow() == k2.getLow()){
                                    if (k1.getDate() < k2.getDate()) {
                                        return 1;
                                    } else {
                                        return -1;
                                    }
                                } else {
                                    return 1;
                                }
                            } else {
                                if (k1.getLow() < k2.getLow()) {
                                    return -1;
                                } else if (k1.getLow() == k2.getLow()){
                                    if (k1.getDate() < k2.getDate()) {
                                        return 1;
                                    } else {
                                        return -1;
                                    }
                                } else {
                                    return 1;
                                }
                            }
                        }
                    });
                    for (int j = 1; j < sortKlineList.size(); j++) {
                        sortKlineList.get(j).setIsEqualDirectPeak(true);
                    }
                    i++;
                    klineList.clear();
                }
            }
        }

        this.oppositeTendencyPeakKlineList =
            tendencyPeakKlineList.stream().filter(peakKline -> !peakKline.isEqualDirectPeak()).collect(Collectors.toList());

    }

    private void deleteContainTendencyPeak() {
        if (independentTendencyPeakKlineList.size() < 2) return;
        PeakKline prev = independentTendencyPeakKlineList.get(0);
        for (int i = 1; i < independentTendencyPeakKlineList.size(); i++) {
            PeakKline curr = independentTendencyPeakKlineList.get(i);
            if (prev.getHighest() > curr.getHighest() && prev.getLowest() < curr.getLowest()) {
                curr.setDependent(true);
            } else {
                prev = curr;
            }
        }
        this.independentTendencyPeakKlineList = independentTendencyPeakKlineList.stream()
                .filter(peakKline -> !peakKline.isDependent()).collect(Collectors.toList());
    }

    public List<PeakKline> getOppositeTendencyPeakKlineList() {
        return oppositeTendencyPeakKlineList;
    }

    public List<PeakKline> getIndependentTendencyPeakKlineList() {
        return independentTendencyPeakKlineList;
    }

    public void setTendencyAngle() {

        List<PeakKline> anglePeakKlineList = new ArrayList<>();

        for (int i = 1; i < independentTendencyPeakKlineList.size(); i++) {

            PeakKline prev = independentTendencyPeakKlineList.get(i - 1);
            PeakKline curr = independentTendencyPeakKlineList.get(i);

            long high;
            int start;
            int end;

            if (prev.getPeakShape() == curr.getPeakShape()) {
                continue;
            }

            if (prev.getPeakShape() == PeakShapeEnum.FLOOR) {
                high = curr.getHighest() - prev.getLowest();
                start = getLowestIndex(prev.getLowest(), prev);
                end = getHighestIndex(curr.getHighest(), curr);
            } else {
                high = prev.getHighest() - curr.getLowest();
                start = getHighestIndex(prev.getHighest(), prev);
                end = getLowestIndex(curr.getLowest(), curr);
            }

            double width = (end - start + 1) * 6.5;

            double angle = Math.toDegrees(Math.atan((double)high / width));

            prev.setAngle(angle);

            anglePeakKlineList.add(prev);

        }

        this.anglePeakKlineList = anglePeakKlineList;

    }

    public int getHighestIndex(int highest, PeakKline peakKline) {
        List<Kline> klineList = peakKline.getMergeKline().getContainKlineList();
        for (Kline kline : klineList) {
            if (kline.getHigh() == highest) {
                return kline.getIndex();
            }
        }
        return -1;
    }

    public int getLowestIndex(int lowest, PeakKline peakKline) {
        List<Kline> klineList = peakKline.getMergeKline().getContainKlineList();
        for (Kline kline : klineList) {
            if (kline.getLow() == lowest) {
                return kline.getIndex();
            }
        }
        return -1;
    }

    public List<PeakKline> getBreakPeakKlineList() {
        return breakPeakKlineList;
    }

    public List<PeakKline> getAnglePeakKlineList() {
        return anglePeakKlineList;
    }

    public List<PeakKline> getJumpPeakKlineList() {
        return jumpPeakKlineList;
    }

    public List<PeakKline> getTendencyPeakKlineList() {
        return tendencyPeakKlineList;
    }

    public void exportExcel() throws IOException {
        if (CollectionUtils.isEmpty(independentTendencyPeakKlineList)) return;
        try (OutputStream output = new FileOutputStream(klineApplicationContext.getOutputPath());
            SXSSFWorkbook workBook = new SXSSFWorkbook(independentTendencyPeakKlineList.size())) {
            Sheet sheet = workBook.createSheet();
            for (int i = 0; i < independentTendencyPeakKlineList.size(); i++) {
                PeakKline peakKline = independentTendencyPeakKlineList.get(i);
                int value = peakKline.getPeakShape() == PeakShapeEnum.TOP ? peakKline.getHighest() : peakKline.getLowest();
                long date = peakKline.getPeakDate();
                Row row = sheet.createRow(i);
                row.createCell(0, CellType.STRING).setCellValue(String.valueOf(date));
                row.createCell(1, CellType.NUMERIC).setCellValue(value);
                if (i == independentTendencyPeakKlineList.size() - 1) {
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
        }
    }

}
