package com.tdx.zq.draw;

import com.tdx.zq.enums.LineReserveTypeEnum;
import com.tdx.zq.enums.PeakShapeEnum;
import com.tdx.zq.utils.JacksonUtils;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import com.tdx.zq.model.Kline;
import com.tdx.zq.model.PeakKline;
import com.tdx.zq.model.MergeKline;
import com.tdx.zq.context.KlineApplicationContext;
import org.springframework.util.CollectionUtils;

public class PeakKlineProcessor {

    private List<Kline> klineList;
    private Map<Integer, Kline> klineMap;
    private List<MergeKline> mergeKlineList;
    private List<PeakKline> peakKlineList;
    private List<PeakKline> breakPeakKlineList;
    private List<PeakKline> jumpPeakKlineList;

    public PeakKlineProcessor(KlineApplicationContext klineApplicationContext) {
        this.klineMap = klineApplicationContext.getKlineMap();
        this.klineList = klineApplicationContext.getKlineList();
        this.mergeKlineList = klineApplicationContext.getMergeKlineList();
        setPeakKlineList(mergeKlineList);
        setBreakPeakKlineList(peakKlineList);
        setJumpPeakKlineList(mergeKlineList, breakPeakKlineList);
    }

    private void setPeakKlineList(List<MergeKline> mergeKlineList) {
        List<PeakKline> peakKlineList = new ArrayList<>();
        for (int i = 1; i < mergeKlineList.size() - 1; i++) {
            MergeKline prev = mergeKlineList.get(i - 1);
            MergeKline curr = mergeKlineList.get(i);
            MergeKline next = mergeKlineList.get(i + 1);
            peakKlineList.add(new PeakKline(prev, curr, next));
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

    public List<PeakKline> computerPeakKlines() {

        //1.获取所有的峰值点
        //List<PeakKline> allPeakKlineList = PeakKline.computerAllPeakKline(combineKlineList);
        //System.out.println("allPeakKlineList: " + JacksonUtils.toJson(allPeakKlineList));


        //2.过滤不符合条件的峰值点
        //(1)过滤了2,3两个K线Break峰值点的情况
//        List<PeakKline> noEnsureReservePeakKlineList = filterTwoThreeBreak(mergeKlineList, peakKlineList);
//        System.out.println("twoThreeBreakPeakKlineList: " +
//                JacksonUtils.toJson(noEnsureReservePeakKlineList.stream()
//                    .map(peakKline -> peakKline.getCombineKline().getMergeKline())
//                        .collect(Collectors.toList())));


//
//        //3.跳空保留峰值点
//        jumpReservePeak(combineKlineList, noEnsureReservePeakKlineList);
//        System.out.println("jumpBreakPeakKlineList: " +
//                JacksonUtils.toJson(noEnsureReservePeakKlineList.stream().map(peakKline -> peakKline.getCombineKline().getMergeKline()).collect(Collectors.toList())));
//
//        //4.趋势保留峰值点
//        List<PeakKline> tendencyReservePeakKlineList = tendencyReservePeak(combineKlineList, noEnsureReservePeakKlineList);
//        System.out.println("tendencyReservePeakKlineList: " +
//                JacksonUtils.toJson(tendencyReservePeakKlineList.stream().map(peakKline -> peakKline.getCombineKline().getMergeKline()).collect(Collectors.toList())));
//
//        //5.确定保留点
//        List<PeakKline> ensureReservePeakKlineList = ensureReservePeak(combineKlineList, tendencyReservePeakKlineList);
//        System.out.println("ensureReservePeakKlineList: " +
//                JacksonUtils.toJson(ensureReservePeakKlineList.stream().map(peakKline -> peakKline.getCombineKline().getMergeKline()).collect(Collectors.toList())));
//
//        //6.修正点处理
//        List<PeakKline> correctedPeakKlineList = correctedPeak(ensureReservePeakKlineList);
//        System.out.println("correctedPeakKlineList: " +
//                JacksonUtils.toJson(correctedPeakKlineList.stream().map(peakKline -> peakKline.getCombineKline().getMergeKline()).collect(Collectors.toList())));
//
//        //7.加入一个未成形的波峰波谷点
//        PeakKline specialPeakKline = specialPeak(combineKlineList, correctedPeakKlineList);
//        List<PeakKline> addSpecialPeakKlineList = correctedPeakKlineList;
//        addSpecialPeakKlineList.add(specialPeakKline);
//        System.out.println("addSpecialPeakKlineList: " +
//                JacksonUtils.toJson(addSpecialPeakKlineList.stream().map(peakKline -> peakKline.getCombineKline().getMergeKline()).collect(Collectors.toList())));
//
//        //8. PeakKline index编号
//        for (int i = 0; i < addSpecialPeakKlineList.size(); i++) {
//            addSpecialPeakKlineList.get(i).setPeakIndex(i);
//        }

        //return addSpecialPeakKlineList;

        return null;
    }


//    public static List<PeakKline> computerAllPeakKline(List<MergeKline> combineKlineList) {
//        List<PeakKline> allPeakKlineList = new ArrayList<>();
//        for (int i = 1; i < combineKlineList.size() - 1; i++) {
//            Kline left = combineKlineList.get(i - 1).getMergeKline();
//            Kline middle = combineKlineList.get(i).getMergeKline();
//            Kline right = combineKlineList.get(i + 1).getMergeKline();
//            if (middle.getHigh() > left.getHigh() && middle.getHigh() > right.getHigh()) {
//                allPeakKlineList.add(new PeakKline(combineKlineList.get(i), PeakShapeEnum.TOP));
//            } else if (middle.getLow() < left.getLow() && middle.getLow() < right.getLow()) {
//                allPeakKlineList.add(new PeakKline(combineKlineList.get(i), PeakShapeEnum.FLOOR));
//            }
//        }
//        return allPeakKlineList;
//    }


//    private PeakKline specialPeak(List<MergeKline> combineKlineList, List<PeakKline> peakKlineList) {
//        PeakKline lastPeakKline = peakKlineList.get(peakKlineList.size() - 1);
//        Kline lastKline = lastPeakKline.getCombineKline().getMergeKline();
//        MergeKline specialCombineKline = null;
//        Integer specialIndex = null;
//        if (lastPeakKline.getShapeType() == LineShapeEnum.FLOOR) {
//            int max = lastKline.getHigh();
//            for (int i = lastPeakKline.getCombineIndex() + 1; i < combineKlineList.size(); i++) {
//                Kline currentKline = combineKlineList.get(i).getMergeKline();
//                if (currentKline.getHigh() > max) {
//                    max = currentKline.getHigh();
//                    specialCombineKline = combineKlineList.get(i);
//                    specialIndex = i;
//                }
//            }
//        } else {
//            int min = lastKline.getLow();
//            for (int i = lastPeakKline.getCombineIndex() + 1; i < combineKlineList.size(); i++) {
//                Kline currentKline = combineKlineList.get(i).getMergeKline();
//                if (currentKline.getLow() < min) {
//                    min = currentKline.getLow();
//                    specialCombineKline = combineKlineList.get(i);
//                    specialIndex = i;
//                }
//            }
//        }
//
//        LineShapeEnum lineShapeEnum = lastPeakKline.getShapeType() == LineShapeEnum.FLOOR  ? LineShapeEnum.TOP : LineShapeEnum.FLOOR;
//
//        if (specialCombineKline != null) {
//            return new PeakKline(specialCombineKline, specialIndex, lineShapeEnum);
//        }
//
//        return null;
//    }

    /*public List<PeakKline> computerAllPeakKline(List<CombineKline> combineKlineList) {
        List<PeakKline> allPeakKlineList = new ArrayList<>();
        for (int i = 1; i < combineKlineList.size() - 1; i++) {
            Kline left = combineKlineList.get(i - 1).getKline();
            Kline middle = combineKlineList.get(i).getKline();
            Kline right = combineKlineList.get(i + 1).getKline();
            if (middle.getHigh() > left.getHigh() && middle.getHigh() > right.getHigh()) {
                allPeakKlineList.add(new PeakKline(combineKlineList.get(i), i, LineShapeEnum.TOP));
            } else if (middle.getLow() < left.getLow() && middle.getLow() < right.getLow()) {
                allPeakKlineList.add(new PeakKline(combineKlineList.get(i), i, LineShapeEnum.FLOOR));
            }
        }
        return allPeakKlineList;
    }*/

//
//
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
//
//    private List<PeakKline> deleteEqualDirectPeak(List<PeakKline> peakKlineList) {
//
//        List<PeakKline> deletedEqualDirectPeakList = new ArrayList<>();
//
//        for (int i = 1; i < peakKlineList.size(); i++) {
//            PeakKline prev;
//            PeakKline curr = peakKlineList.get(i);
//
//            if (deletedEqualDirectPeakList.size() == 0) {
//                prev = peakKlineList.get(i - 1);
//            } else {
//                prev = deletedEqualDirectPeakList.get(deletedEqualDirectPeakList.size() - 1);
//            }
//
//            if (prev.getShapeType() != curr.getShapeType()) {
//                deletedEqualDirectPeakList.add(curr);
//            } else {
//                if (curr.getShapeType() == LineShapeEnum.TOP) {
//                    if (prev.getCombineKline().getMergeKline().getHigh() < curr.getCombineKline().getMergeKline().getHigh()) {
//                        deletedEqualDirectPeakList.set(deletedEqualDirectPeakList.size() - 1, curr);
//                    }
//                }
//
//                if (curr.getShapeType() == LineShapeEnum.FLOOR) {
//                    if (prev.getCombineKline().getMergeKline().getLow() > curr.getCombineKline().getMergeKline().getLow()) {
//                        deletedEqualDirectPeakList.set(deletedEqualDirectPeakList.size() - 1, curr);
//                    }
//                }
//            }
//        }
//        return deletedEqualDirectPeakList;
//    }
//
//    private List<PeakKline> tendencyReservePeak(List<MergeKline> combineKlineList,
//                                                List<PeakKline> noEnsureReservePeakKlineList) {
//
//        noEnsureReservePeakKlineList = deleteEqualDirectPeak(noEnsureReservePeakKlineList);
//
//        for (int i = 0; i < noEnsureReservePeakKlineList.size(); i++) {
//            if (noEnsureReservePeakKlineList.get(i).getReserveType() != LineReserveTypeEnum.JUMP
//                    && noEnsureReservePeakKlineList.get(i).getReserveType() != LineReserveTypeEnum.DROP) {
//                Integer index = noEnsureReservePeakKlineList.get(i).getCombineIndex();
//                Kline left = combineKlineList.get(index - 1).getMergeKline();
//                Kline middle = combineKlineList.get(index).getMergeKline();
//                Kline right = combineKlineList.get(index + 1).getMergeKline();
//                Kline second = combineKlineList.get(index + 2).getMergeKline();
//                Kline third = combineKlineList.get(index + 3).getMergeKline();
//
//                if (middle.getLow() < right.getLow()) {
//                    int max = Arrays.stream(new int[]{left.getHigh(), middle.getHigh(), right.getHigh(), second.getHigh(), third.getHigh()}).max().getAsInt();
//                    for (int j = index + 4; j < combineKlineList.size(); j++) {
//                        if (max <= combineKlineList.get(j).getMergeKline().getHigh()) {
//                            noEnsureReservePeakKlineList.get(i).setReserveType(LineReserveTypeEnum.TENDENCY);
//                            break;
//                        }
//                        if (middle.getLow() > combineKlineList.get(j).getMergeKline().getLow()) {
//                            int rangeMin = middle.getLow();
//                            int rangeMax = Math.max(left.getHigh(), right.getHigh());
//                            int endDate = combineKlineList.get(j - 1).getMergeKline().getDate();
//                            int temp = i + 1;
//                            while(true) {
//                                Kline kline = noEnsureReservePeakKlineList.get(temp).getCombineKline().getMergeKline();
//                                if (kline.getDate() <= endDate) {
//                                    if (kline.getHigh() < rangeMax && kline.getLow() > rangeMin) {
//                                        noEnsureReservePeakKlineList.get(temp).setReserveType(LineReserveTypeEnum.DROP);
//                                    }
//                                } else {
//                                    break;
//                                }
//                                temp++;
//                            }
//                            noEnsureReservePeakKlineList.get(i).setReserveType(LineReserveTypeEnum.DROP);
//                            break;
//                        }
//                    }
//                } else if (middle.getHigh() > right.getHigh()) {
//                    int min = Arrays.stream(new int[]{left.getLow(), middle.getLow(), right.getLow(), second.getLow(), third.getLow()}).min().getAsInt();
//                    for (int j = index + 4; j < combineKlineList.size(); j++) {
//                        if (min >= combineKlineList.get(j).getMergeKline().getLow()) {
//                            noEnsureReservePeakKlineList.get(i).setReserveType(LineReserveTypeEnum.TENDENCY);
//                            break;
//                        }
//                        if (middle.getHigh() < combineKlineList.get(j).getMergeKline().getHigh()) {
//                            int rangeMax = middle.getHigh();
//                            int rangeMin = Math.min(left.getLow(), right.getLow());
//                            int endDate = combineKlineList.get(j-1).getMergeKline().getDate();
//                            int temp = i + 1;
//                            while(true) {
//                                Kline kline = noEnsureReservePeakKlineList.get(temp).getCombineKline().getMergeKline();
//                                if (kline.getDate() <= endDate) {
//                                    if (kline.getHigh() < rangeMax && kline.getLow() > rangeMin) {
//                                        noEnsureReservePeakKlineList.get(temp).setReserveType(LineReserveTypeEnum.DROP);
//                                    }
//                                } else {
//                                    break;
//                                }
//                                temp++;
//                            }
//                            noEnsureReservePeakKlineList.get(i).setReserveType(LineReserveTypeEnum.DROP);
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//
//        noEnsureReservePeakKlineList =  noEnsureReservePeakKlineList.stream()
//                    .filter(peak -> peak.getReserveType() != LineReserveTypeEnum.DROP)
//                        .collect(Collectors.toList());
//
//        return deleteEqualDirectPeak(noEnsureReservePeakKlineList);
//    }
//
//    private List<PeakKline> ensureReservePeak(List<MergeKline> combineKlineList, List<PeakKline> tendencyReservePeakKlineList) {
//        List<PeakKline> ensureReservePeakKlineList = new LinkedList(tendencyReservePeakKlineList.stream().filter(peak -> peak.getReserveType() != LineReserveTypeEnum.NONE).collect(Collectors.toList()));
//        Iterator<PeakKline> peakKlineIterator = ensureReservePeakKlineList.iterator();
//        PeakKline parent = null;
//        PeakKline prev = peakKlineIterator.next();
//        while(peakKlineIterator.hasNext()) {
//            PeakKline curr = peakKlineIterator.next();
//
//            if (curr.getCombineKline().getMergeKline().getDate() == 20190404) {
//                System.out.println(20190404);
//            }
//
//            if (curr.getCombineIndex() - prev.getCombineIndex() < 4
//                    && curr.getReserveType() != LineReserveTypeEnum.JUMP
//                        && (curr.getShapeType() == LineShapeEnum.FLOOR && parent != null && parent.getCombineKline().getMergeKline().getLow() <= curr.getCombineKline().getMergeKline().getLow()
//                            || curr.getShapeType() == LineShapeEnum.TOP && parent != null && parent.getCombineKline().getMergeKline().getHigh() >= curr.getCombineKline().getMergeKline().getHigh())
//                            && (prev.getReserveType() != LineReserveTypeEnum.JUMP || curr.getReserveType() != LineReserveTypeEnum.TENDENCY)
//            ) {
//                peakKlineIterator.remove();
//            } else if (prev.getShapeType() == LineShapeEnum.FLOOR && prev.getShapeType() != curr.getShapeType() && curr.getCombineKline().getMergeKline().getHigh() < combineKlineList.get(prev.getCombineIndex() - 1).getMergeKline().getHigh()) {
//                peakKlineIterator.remove();
//            } else if (prev.getShapeType() == LineShapeEnum.TOP && prev.getShapeType() != curr.getShapeType() && curr.getCombineKline().getMergeKline().getLow() > combineKlineList.get(prev.getCombineIndex() - 1).getMergeKline().getLow()) {
//                peakKlineIterator.remove();
//            } else {
//                parent = prev;
//                prev = curr;
//            }
//        }
//
//        return deleteEqualDirectPeak(ensureReservePeakKlineList);
//    }
//
//    private List<PeakKline> correctedPeak(List<PeakKline> ensureReservePeakKlineList) {
//        for (int i = 0; i < ensureReservePeakKlineList.size() - 3; i++) {
//            PeakKline peak1 = ensureReservePeakKlineList.get(i);
//            PeakKline peak2 = ensureReservePeakKlineList.get(i+1);
//            PeakKline peak3 = ensureReservePeakKlineList.get(i+2);
//            PeakKline peak4 = ensureReservePeakKlineList.get(i+3);
//
//            if (peak4.getCombineIndex() - peak3.getCombineIndex() < 4 && peak3.getReserveType() != LineReserveTypeEnum.JUMP) {
//                if (peak1.getShapeType() == LineShapeEnum.TOP) {
//                    if (peak1.getCombineKline().getMergeKline().getHigh() > peak3.getCombineKline().getMergeKline().getHigh()
//                            && peak2.getCombineKline().getMergeKline().getLow() > peak4.getCombineKline().getMergeKline().getLow()) {
//                        peak2.setReserveType(LineReserveTypeEnum.DROP);
//                        peak3.setReserveType(LineReserveTypeEnum.DROP);
//                    }
//                } else {
//                    if (peak1.getCombineKline().getMergeKline().getLow() < peak3.getCombineKline().getMergeKline().getLow()
//                            && peak2.getCombineKline().getMergeKline().getHigh() < peak4.getCombineKline().getMergeKline().getHigh()) {
//                        peak2.setReserveType(LineReserveTypeEnum.DROP);
//                        peak3.setReserveType(LineReserveTypeEnum.DROP);
//                    }
//                }
//            }
//        }
//        ensureReservePeakKlineList = ensureReservePeakKlineList.stream().filter(peak -> peak.getReserveType() != LineReserveTypeEnum.DROP).collect(Collectors.toList());
//
//        return deleteEqualDirectPeak(ensureReservePeakKlineList);
//    }

}
