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
    private List<MergeKline> mergeKlineList;
    private List<PeakKline> peakKlineList;
    private List<PeakKline> breakPeakKlineList;
    private List<PeakKline> jumpPeakKlineList;
    private List<PeakKline> turnPeakKlineList;
    private List<PeakKline> tendencyPeakKlineList;
    private KlineApplicationContext klineApplicationContext;

    public PeakKlineProcessor(KlineApplicationContext klineApplicationContext) throws IOException {
        this.klineApplicationContext = klineApplicationContext;
        this.klineList = klineApplicationContext.getKlineList();
        this.mergeKlineList = klineApplicationContext.getMergeKlineList();
        setPeakKlineList(mergeKlineList);
        setJumpPeakKlineList(mergeKlineList, peakKlineList);
        setBreakPeakKlineList(peakKlineList);
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

    private void setBreakPeakKlineList(
        List<PeakKline> peakKlineList) {
        List<PeakKline> breakPeakKlineList = new ArrayList<>();
        for (int i = 0; i < peakKlineList.size() - 3; i++) {
            if (peakKlineList.get(i).getPeakShape() != PeakShapeEnum.NONE) {
                if (peakKlineList.get(i).isJumpPeak()) {
                    peakKlineList.get(i).setIsBreakPeak(true);
                    breakPeakKlineList.add(peakKlineList.get(i));
                } else {
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
        }
        this.breakPeakKlineList = breakPeakKlineList;
    }

    /*private boolean setJumpPeakKline(PeakKline peakKline,  List<MergeKline> mergeKlineList) {

        Integer index = peakKline.getMergeKline().getIndex();

        Kline left = mergeKlineList.get(index - 1).getMergeKline();
        Kline middle = mergeKlineList.get(index).getMergeKline();
        Kline right = mergeKlineList.get(index + 1).getMergeKline();
        Kline second = mergeKlineList.get(index + 2).getMergeKline();

        Kline orig1 = mergeKlineList.get(index + 2).getFirstOriginKline();

        if (middle.getLow() < right.getLow()) {
            if (right.getHigh() < second.getLow()
                && second.getLow() > left.getHigh()
                && peakKline.getHighest() < orig1.getLow()) {
                peakKline.setTurnKline(orig1);
                peakKline.setIsJumpPeak(true);
                return true;
            }
            Kline third = mergeKlineList.get(index + 3).getMergeKline();
            Kline orig2 = mergeKlineList.get(index + 3).getFirstOriginKline();
            if (left.getHigh() < third.getLow()
                && right.getHigh() < third.getLow()
                && second.getHigh() < third.getLow()
                && second.getHigh() < orig2.getLow()
                && peakKline.getHighest() < orig2.getLow()) {
                peakKline.setTurnKline(orig2);
                peakKline.setIsJumpPeak(true);
                return true;
            }
        } else if (middle.getHigh() > right.getHigh()) {
            if (left.getLow() > second.getHigh()
                && right.getLow() > second.getHigh()
                && peakKline.getLowest() > orig1.getHigh()) {
                peakKline.setTurnKline(orig1);
                peakKline.setIsJumpPeak(true);
                return true;
            }
            Kline third = mergeKlineList.get(index + 3).getMergeKline();
            Kline orig2 = mergeKlineList.get(index + 3).getFirstOriginKline();
            if (left.getLow() > third.getHigh()
                && right.getLow() > third.getHigh()
                && second.getLow() > third.getHigh()
                && second.getLow() > orig2.getHigh()
                && peakKline.getLowest() > orig2.getHigh()) {
                peakKline.setTurnKline(orig2);
                peakKline.setIsJumpPeak(true);
                return true;
            }
        }
        return false;
    }*/

    private void setJumpPeakKlineList(
        List<MergeKline> mergeKlineList,
        List<PeakKline> peakKlineList) {
        List<PeakKline> jumpPeakKlineList = new ArrayList<>();
        for (int i = 0; i < peakKlineList.size() - 3; i++) {

            PeakKline breakPeakKline = peakKlineList.get(i);
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
                    jumpPeakKlineList.add(breakPeakKline);
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
                    jumpPeakKlineList.add(breakPeakKline);
                    continue;
                }
            } else if (middle.getHigh() > right.getHigh()) {
                if (left.getLow() > second.getHigh()
                    && right.getLow() > second.getHigh()
                    && breakPeakKline.getLowest() > orig1.getHigh()) {
                    breakPeakKline.setTurnKline(orig1);
                    breakPeakKline.setIsJumpPeak(true);
                    jumpPeakKlineList.add(breakPeakKline);
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
                                int max = prev.getPeakShape() == PeakShapeEnum.TOP ? prev.getHighest() : curr.getHighest();
                                int min = prev.getPeakShape() == PeakShapeEnum.FLOOR ? prev.getLowest() : curr.getLowest();
                                for (int z = prev.getMergeKlineIndex() + 1; z < curr.getMergeKlineIndex(); z++) {
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
                                curr.setRangePeak(true);
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
        
    }

    public void exportExcel() throws IOException {
        if (CollectionUtils.isEmpty(tendencyPeakKlineList)) return;
        try (OutputStream output = new FileOutputStream(klineApplicationContext.getOutputPath());
            SXSSFWorkbook workBook = new SXSSFWorkbook(tendencyPeakKlineList.size())) {
            Sheet sheet = workBook.createSheet();
            
            MergeKline prev = mergeKlineList.get(0);
            for (int i = 1; i < tendencyPeakKlineList.get(0).getMergeKlineIndex(); i++) {
                MergeKline curr = mergeKlineList.get(i);
                if (tendencyPeakKlineList.get(0).getPeakShape() == PeakShapeEnum.TOP) {
                    prev = prev.getLow() <= curr.getLow() ? prev : curr;
                } else {
                    prev = prev.getHigh() >= curr.getHigh() ? prev : curr;
                }
            }
            if (prev != null && prev.getIndex() < tendencyPeakKlineList.get(0).getMergeKlineIndex()) {
                Row firstRow = sheet.createRow(0);
                firstRow.createCell(0, CellType.STRING).setCellValue(String.valueOf(klineList.get(0).getDate()));
                firstRow.createCell(1, CellType.NUMERIC).setCellValue(klineList.get(0).getLow());
            }
    
            for (int i = 0; i < tendencyPeakKlineList.size(); i++) {
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
                Row row = sheet.createRow(i + 1);
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
                    row = sheet.createRow(i + 2);
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
