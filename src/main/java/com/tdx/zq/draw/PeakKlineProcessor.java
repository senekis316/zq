package com.tdx.zq.draw;

import com.tdx.zq.context.KlineApplicationContext;
import com.tdx.zq.enums.PeakShapeEnum;
import com.tdx.zq.enums.TendencyTypeEnum;
import com.tdx.zq.model.Kline;
import com.tdx.zq.model.MergeKline;
import com.tdx.zq.model.PeakKline;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.tdx.zq.tuple.TwoTuple;
import com.tdx.zq.utils.JacksonUtils;
import lombok.Data;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.util.CollectionUtils;

public class PeakKlineProcessor {

    private List<Kline> klineList;
    private List<MergeKline> mergeKlineList;
    private List<PeakKline> peakKlineList;
    private List<PeakKline> breakPeakKlineList;
    private List<PeakKline> jumpPeakKlineList;
    private List<PeakKline> turnPeakKlineList;
    private List<PeakKline> tendencyPeakKlineList;
    private List<MatrixKlineRow> matrixKlineRowList;
    private KlineApplicationContext klineApplicationContext;

    public PeakKlineProcessor(KlineApplicationContext klineApplicationContext) throws IOException {
        this.klineApplicationContext = klineApplicationContext;
        this.klineList = klineApplicationContext.getKlineList();
        this.mergeKlineList = klineApplicationContext.getMergeKlineList();
        setPeakKlineList(mergeKlineList);
        setBreakJumpPeakKlineList(mergeKlineList, peakKlineList);
        setBreakPeakKlineList(peakKlineList);
        setJumpPeakKlineList(mergeKlineList, breakPeakKlineList);
        setTurningPeakKlineList(mergeKlineList, breakPeakKlineList);
        setBreakRangePeak();
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

    private void setBreakJumpPeakKlineList(
        List<MergeKline> mergeKlineList,
        List<PeakKline> peakKlineList) {
        for (int i = 0; i < peakKlineList.size() - 3; i++) {

            if (peakKlineList.get(i).getPeakShape() != PeakShapeEnum.NONE) {

                PeakKline peakKline = peakKlineList.get(i);
                Integer index = peakKline.getMergeKline().getIndex();

                Kline left = mergeKlineList.get(index - 1).getMergeKline();
                Kline middle = mergeKlineList.get(index).getMergeKline();
                Kline right = mergeKlineList.get(index + 1).getMergeKline();
                Kline second = mergeKlineList.get(index + 2).getMergeKline();
                Kline third = mergeKlineList.get(index + 3).getMergeKline();

                Kline orig1 = mergeKlineList.get(index + 2).getFirstOriginKline();

                if (middle.getLow() < right.getLow()) {
                    if (right.getHigh() < second.getLow()
                        && second.getLow() > left.getHigh()
                        && peakKline.getHighest() < orig1.getLow()
                        && peakKline.getLowest() <= third.getLow()) {
                        peakKline.setTurnKline(orig1);
                        peakKline.setIsJumpPeak(true);
                        peakKline.setIsBreakPeak(true);
                    }
                } else if (middle.getHigh() > right.getHigh()) {
                    if (left.getLow() > second.getHigh()
                        && right.getLow() > second.getHigh()
                        && peakKline.getLowest() > orig1.getHigh()
                        && peakKline.getHighest() <= third.getHigh()) {
                        peakKline.setTurnKline(orig1);
                        peakKline.setIsJumpPeak(true);
                        peakKline.setIsBreakPeak(true);
                    }
                }
            }
        }
    }

    private void setBreakPeakKlineList(
            List<PeakKline> peakKlineList) {
        for (int i = 0; i < peakKlineList.size() - 4; i++) {
            if (!peakKlineList.get(i).isBreakPeak()) {
                if (peakKlineList.get(i).getPeakShape() != PeakShapeEnum.NONE) {
                    if (peakKlineList.get(i).isJumpPeak()) {
                        peakKlineList.get(i).setIsBreakPeak(true);
                    } else {
                        Kline middle = peakKlineList.get(i).getMergeKline().getMergeKline();
                        Kline right = peakKlineList.get(i + 1).getMergeKline().getMergeKline();
                        Kline second = peakKlineList.get(i + 2).getMergeKline().getMergeKline();
                        Kline third = peakKlineList.get(i + 3).getMergeKline().getMergeKline();
                        Kline fourth = peakKlineList.get(i + 4).getMergeKline().getMergeKline();

                        //波谷
                        if (middle.getLow() < right.getLow()) {
                            if (middle.getLow() > second.getLow()) {
                                continue;
                            }
                            if (middle.getLow() > third.getLow()) {
                                continue;
                            }
                            if (middle.getLow() > fourth.getLow()) {
                                continue;
                            }
                            peakKlineList.get(i).setIsBreakPeak(true);
                        } else if (middle.getHigh() > right.getHigh()) {
                            if (middle.getHigh() < second.getHigh()) {
                                continue;
                            }
                            if (middle.getHigh() < third.getHigh()) {
                                continue;
                            }
                            if (middle.getHigh() < fourth.getHigh()) {
                                continue;
                            }
                            peakKlineList.get(i).setIsBreakPeak(true);
                        }
                    }
                }
            }
        }
        this.breakPeakKlineList =
                peakKlineList.stream().filter(PeakKline::isBreakPeak).collect(Collectors.toList());
    }

    private void setJumpPeakKlineList(
        List<MergeKline> mergeKlineList,
        List<PeakKline> breakPeakKlineList) {
        for (int i = 0; i <= breakPeakKlineList.size() - 3; i++) {

            PeakKline breakPeakKline = breakPeakKlineList.get(i);
            Integer index = breakPeakKline.getMergeKline().getIndex();

            Kline left = mergeKlineList.get(index - 1).getMergeKline();
            Kline middle = mergeKlineList.get(index).getMergeKline();
            Kline right = mergeKlineList.get(index + 1).getMergeKline();
            Kline second = mergeKlineList.get(index + 2).getMergeKline();

            Kline orig1 = mergeKlineList.get(index + 2).getFirstOriginKline();

            if (middle.getLow() < right.getLow()) {
                if (right.getHigh() < second.getLow()
                    && second.getLow() > left.getHigh()
                    && breakPeakKline.getHighest() < orig1.getLow()) {
                    breakPeakKline.setTurnKline(orig1);
                    breakPeakKline.setIsJumpPeak(true);
                    continue;
                }
                Kline third = mergeKlineList.get(index + 3).getMergeKline();
                Kline orig2 = mergeKlineList.get(index + 3).getFirstOriginKline();
                if (left.getHigh() < third.getLow()
                    && right.getHigh() < third.getLow()
                    && second.getHigh() < third.getLow()
                    && second.getHigh() < orig2.getLow()
                    && breakPeakKline.getHighest() < orig2.getLow()) {
                    breakPeakKline.setTurnKline(orig2);
                    breakPeakKline.setIsJumpPeak(true);
                    continue;
                }
            } else if (middle.getHigh() > right.getHigh()) {
                if (left.getLow() > second.getHigh()
                    && right.getLow() > second.getHigh()
                    && breakPeakKline.getLowest() > orig1.getHigh()) {
                    breakPeakKline.setTurnKline(orig1);
                    breakPeakKline.setIsJumpPeak(true);
                    continue;
                }
                Kline third = mergeKlineList.get(index + 3).getMergeKline();
                Kline orig2 = mergeKlineList.get(index + 3).getFirstOriginKline();
                if (left.getLow() > third.getHigh()
                    && right.getLow() > third.getHigh()
                    && second.getLow() > third.getHigh()
                    && second.getLow() > orig2.getHigh()
                    && breakPeakKline.getLowest() > orig2.getHigh()) {
                    breakPeakKline.setTurnKline(orig2);
                    breakPeakKline.setIsJumpPeak(true);
                    continue;
                }
            }
        }
        this.jumpPeakKlineList =
            peakKlineList.stream().filter(PeakKline::isJumpPeak).collect(Collectors.toList());
    }

    private void setTurningPeakKlineList(
        List<MergeKline> mergeKlineList,
        List<PeakKline> breakPeakKlineList) {

        for (int i = 0; i < breakPeakKlineList.size(); i++) {

            PeakKline peakKline = breakPeakKlineList.get(i);

            if (peakKline.getPeakShape() != PeakShapeEnum.NONE) {
                Integer index = peakKline.getMergeKline().getIndex();
                Kline left = mergeKlineList.get(index - 1).getMergeKline();
                Kline middle = mergeKlineList.get(index).getMergeKline();
                Kline right = mergeKlineList.get(index + 1).getMergeKline();
                Kline second = mergeKlineList.get(index + 2).getMergeKline();
                Kline third = mergeKlineList.get(index + 3).getMergeKline();

                if (middle.getLow() < right.getLow()) {
                    if (peakKline.isJumpPeak()) {
                        peakKline.setTurnPeak(true);
                    }
                    int max = Arrays.stream(
                        new int[]{left.getHigh(), middle.getHigh(), right.getHigh(), second.getHigh(),
                            third.getHigh()}).max().getAsInt();
                    for (int j = index + 4; j < mergeKlineList.size(); j++) {
                        Kline curr = mergeKlineList.get(j).getMergeKline();
                        if (max <= curr.getHigh()) {
                            peakKline.setTurnPeak(true);
                            if (peakKline.isJumpPeak()) {
                                if (peakKline.getTurnKline().getDate() >= curr.getDate()) {
                                    peakKline.setTurnKline(curr);
                                }
                            } else {
                                peakKline.setTurnKline(curr);
                            }
                            break;
                        } else if (middle.getLow() > curr.getLow()) {
                            break;
                        }
                    }
                } else if (middle.getHigh() > right.getHigh()) {
                    if (peakKline.isJumpPeak()) {
                        peakKline.setTurnPeak(true);
                    }
                    int min = Arrays.stream(
                        new int[]{left.getLow(), middle.getLow(), right.getLow(), second.getLow(),
                            third.getLow()}).min().getAsInt();
                    for (int j = index + 4; j < mergeKlineList.size(); j++) {
                        Kline curr = mergeKlineList.get(j).getMergeKline();
                        if (min >= curr.getLow()) {
                            peakKline.setTurnPeak(true);
                            if (peakKline.isJumpPeak()) {
                                if (peakKline.getTurnKline().getDate() >= curr.getDate()) {
                                    peakKline.setTurnKline(curr);
                                }
                            } else {
                                peakKline.setTurnKline(curr);
                            }
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
        for (int i = 1; i < breakPeakKlineList.size(); i++) {
            PeakKline prev = breakPeakKlineList.get(i - 1);
            if (!prev.isRangePeak()) {
                if (prev.isJumpPeak() || prev.isTurnPeak()) {
                    for (int j = i; j < breakPeakKlineList.size(); j++) {
                        PeakKline curr = breakPeakKlineList.get(j);
                        if (!curr.isRangePeak()) {
                            if ((curr.isJumpPeak() || curr.isTurnPeak())
                                && prev.getPeakShape() != curr.getPeakShape()
                                && curr.getMergeKline().getLastOriginKline().getIndex() >= prev.getTurnKline().getIndex()) {
                                int max =
                                    prev.getPeakShape() == PeakShapeEnum.TOP ? prev.getHighest()
                                        : curr.getHighest();
                                int min =
                                    prev.getPeakShape() == PeakShapeEnum.FLOOR ? prev.getLowest()
                                        : curr.getLowest();
                                for (int z = prev.getMergeKlineIndex() + 1;
                                    z < curr.getMergeKlineIndex(); z++) {
                                    MergeKline merge = mergeKlineList.get(z);
                                    if (curr.getPeakShape() == PeakShapeEnum.FLOOR) {
                                        if (merge.getLow() < min) {
                                            curr.setRangePeak(true);
                                            break;
                                        }
                                    } else {
                                        if (merge.getHigh() > max) {
                                            curr.setRangePeak(true);
                                            break;
                                        }
                                    }
                                }
                            } else {
                                if (curr.getPeakShape() == prev.getPeakShape()) {
                                    if (curr.getPeakShape() == PeakShapeEnum.FLOOR) {
                                        if (prev.getLowest() <= curr.getLowest()) {
                                            curr.setRangePeak(true);
                                        } else {
                                            prev.setRangePeak(true);
                                        }
                                    } else {
                                        if (prev.getHighest() >= curr.getHighest()) {
                                            curr.setRangePeak(true);
                                        } else {
                                            prev.setRangePeak(true);
                                        }
                                    }
                                } else {
                                    curr.setRangePeak(true);
                                }
                            }
                        }
                        if (!curr.isRangePeak()) {
                            i = j;
                            break;
                        }
                    }
                }
            }
        }
    }

    public void setTendencyPeakKlineList() {

        tendencyPeakKlineList = new ArrayList<>();
        for (int i = 0; i < breakPeakKlineList.size(); i++) {
            PeakKline peakKline = breakPeakKlineList.get(i);
            if (peakKline.isJumpPeak() || peakKline.isTurnPeak()) {
                if (!peakKline.isRangePeak()) {
                    peakKline.setIsTendencyPeak(true);
                    tendencyPeakKlineList.add(peakKline);
                }
            }
        }

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
        this.tendencyPeakKlineList = breakPeakKlineList.stream().filter(PeakKline::isTendencyPeak).collect(Collectors.toList());
    }

    public static class KlineRow {
        private long date;
        private long value;
        public KlineRow(long date, long value) {
            this.date = date;
            this.value = value;
        }

        public long getDate() {
            return date;
        }

        public long getValue() {
            return value;
        }
    }

    public static class MatrixKlineRow {
        private long low;
        private long high;
        private long date;
        private PeakShapeEnum shape;
        private TendencyTypeEnum tendency;
        public MatrixKlineRow(long low, long high, long date, PeakShapeEnum shape) {
            this.low = low;
            this.high = high;
            this.date = date;
            this.shape = shape;
        }

        public long getLow() {
            return low;
        }

        public long getHigh() {
            return high;
        }

        public long getDate() {
            return date;
        }

        public PeakShapeEnum getShape() {
            return shape;
        }

        public TendencyTypeEnum getTendency() {
            return tendency;
        }
    }

    public void exportExcel() throws IOException {
        if (CollectionUtils.isEmpty(tendencyPeakKlineList)) return;
        try (OutputStream output = new FileOutputStream(klineApplicationContext.getOutputPath());
            SXSSFWorkbook workBook = new SXSSFWorkbook(tendencyPeakKlineList.size())) {
            Sheet sheet = workBook.createSheet();

            List<KlineRow> klineRows = new ArrayList<>();
            List<MatrixKlineRow> matrixKlineRows = new ArrayList<>();

            int skip = 0;
            MergeKline one = mergeKlineList.get(0);
            MergeKline two = tendencyPeakKlineList.get(0).getMergeKline();
            if (one.getIndex() != two.getIndex()) {
                if (tendencyPeakKlineList.get(0).getPeakShape() == PeakShapeEnum.FLOOR) {
                    if (one.getLow() <= two.getLow()) {
                        klineRows.add(new KlineRow(one.getMergeKline().getDate(), one.getLow()));
                        matrixKlineRows.add(new MatrixKlineRow(one.getLow(), one.getHigh(), one.getMergeKline().getDate(), PeakShapeEnum.FLOOR));
                        skip++;
                    } else {
                        klineRows.add(new KlineRow(one.getMergeKline().getDate(), one.getHigh()));
                        matrixKlineRows.add(new MatrixKlineRow(one.getLow(), one.getHigh(), one.getMergeKline().getDate(), PeakShapeEnum.TOP));
                    }
                } else {
                    if (one.getHigh() >= two.getHigh()) {
                        klineRows.add(new KlineRow(one.getMergeKline().getDate(), one.getHigh()));
                        matrixKlineRows.add(new MatrixKlineRow(one.getLow(), one.getHigh(), one.getMergeKline().getDate(), PeakShapeEnum.TOP));
                        skip++;
                    } else {
                        klineRows.add(new KlineRow(one.getMergeKline().getDate(), one.getHigh()));
                        matrixKlineRows.add(new MatrixKlineRow(one.getLow(), one.getHigh(), one.getMergeKline().getDate(), PeakShapeEnum.FLOOR));
                    }
                }
            }

            for (int i = skip; i < tendencyPeakKlineList.size(); i++) {
                PeakKline peakKline = tendencyPeakKlineList.get(i);
                int value = peakKline.getPeakShape() == PeakShapeEnum.TOP ? peakKline.getHighest() : peakKline.getLowest();
                Long date = null;
                if (peakKline.getPeakShape() == PeakShapeEnum.TOP) {
                    for (Kline kline : peakKline.getMergeKline().getContainKlineList()) {
                        if (kline.getHigh() == value) {
                            date = kline.getDate();
                            break;
                        }
                    }
                } else {
                    for (Kline kline : peakKline.getMergeKline().getContainKlineList()) {
                        if (kline.getLow() == value) {
                            date = kline.getDate();
                            break;
                        }
                    }
                }

                klineRows.add(new KlineRow(date, value));
                matrixKlineRows.add(new MatrixKlineRow(peakKline.getLowest(), peakKline.getHighest(), date, peakKline.getPeakShape()));

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

                    PeakShapeEnum shape = peakKline.getPeakShape() == PeakShapeEnum.TOP ? PeakShapeEnum.FLOOR : PeakShapeEnum.TOP;
                    for (int j = peakKlineList.size() - 1; j >= 0; j--) {
                        if (peakKlineList.get(j).getPeakShape() == shape) {
                            if (peakKlineList.get(j).getPeakDate() == date) {

                                Integer pos = peakKlineList.get(j).getMergeKline().getIndex();

                                if (pos + 2 < mergeKlineList.size()) {

                                    Kline left = mergeKlineList.get(pos - 1).getMergeKline();
                                    Kline middle = mergeKlineList.get(pos).getMergeKline();
                                    Kline right = mergeKlineList.get(pos + 1).getMergeKline();
                                    Kline second = mergeKlineList.get(pos + 2).getMergeKline();

                                    Kline orig1 = mergeKlineList.get(pos + 2).getFirstOriginKline();

                                    if (middle.getLow() < right.getLow()) {
                                        if (right.getHigh() < second.getLow()
                                            && second.getLow() > left.getHigh()
                                            && peakKlineList.get(j).getHighest() < orig1.getLow()) {
                                            tendencyPeakKlineList.add(peakKlineList.get(j));
                                            break;
                                        }
                                        if (pos + 3 < mergeKlineList.size()) {
                                            Kline third = mergeKlineList.get(pos + 3).getMergeKline();
                                            Kline orig2 = mergeKlineList.get(pos + 3)
                                                .getFirstOriginKline();
                                            if (left.getHigh() < third.getLow()
                                                && right.getHigh() < third.getLow()
                                                && second.getHigh() < third.getLow()
                                                && second.getHigh() < orig2.getLow()
                                                && peakKlineList.get(j).getHighest() < orig2.getLow()) {
                                                tendencyPeakKlineList.add(peakKlineList.get(j));
                                                break;
                                            }
                                        }
                                    } else if (middle.getHigh() > right.getHigh()) {
                                        if (left.getLow() > second.getHigh()
                                            && right.getLow() > second.getHigh()
                                            && peakKlineList.get(j).getLowest() > orig1.getHigh()) {
                                            tendencyPeakKlineList.add(peakKlineList.get(j));
                                            break;
                                        }
                                        if (pos + 3 < mergeKlineList.size()) {
                                            Kline third = mergeKlineList.get(pos + 3)
                                                .getMergeKline();
                                            Kline orig2 = mergeKlineList.get(pos + 3)
                                                .getFirstOriginKline();
                                            if (left.getLow() > third.getHigh()
                                                && right.getLow() > third.getHigh()
                                                && second.getLow() > third.getHigh()
                                                && second.getLow() > orig2.getHigh()
                                                && peakKlineList.get(j).getLowest() > orig2
                                                .getHigh()) {
                                                tendencyPeakKlineList.add(peakKlineList.get(j));
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (i != tendencyPeakKlineList.size() - 1) {
                        continue;
                    }
                    value = peakKline.getPeakShape() == PeakShapeEnum.TOP ? lowest : highest;
                    klineRows.add(new KlineRow(date, value));
                    if (peakKline.getPeakShape() == PeakShapeEnum.TOP) {
                        matrixKlineRows.add(new MatrixKlineRow(lowest, peakKline.getHighest(), date, PeakShapeEnum.FLOOR));
                    } else {
                        matrixKlineRows.add(new MatrixKlineRow(peakKline.getLowest(), highest, date, PeakShapeEnum.TOP));
                    }
                }
            }

            this.matrixKlineRowList = matrixKlineRows;

            for (int i = 0; i < klineRows.size(); i++) {
                KlineRow klineRow = klineRows.get(i);
                Row row = sheet.createRow(i);
                row.createCell(0, CellType.STRING).setCellValue(String.valueOf(klineRow.getDate()));
                row.createCell(1, CellType.NUMERIC).setCellValue(klineRow.getValue());
            }

            workBook.write(output);
            workBook.dispose();
        } finally {
            System.out.println("输出文件: " + klineApplicationContext.getOutputPath());
        }

    }

    public List<MatrixKlineRow> getMatrixKlineRowList() {
        return matrixKlineRowList;
    }

}
