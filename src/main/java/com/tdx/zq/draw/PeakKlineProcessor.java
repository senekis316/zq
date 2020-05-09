package com.tdx.zq.draw;

import com.tdx.zq.context.KlineApplicationContext;
import com.tdx.zq.enums.PeakShapeEnum;
import com.tdx.zq.model.Kline;
import com.tdx.zq.model.MergeKline;
import com.tdx.zq.model.PeakKline;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PeakKlineProcessor {

    private List<Kline> klineList;
    private Map<Integer, Kline> klineMap;
    private List<MergeKline> mergeKlineList;
    private List<PeakKline> peakKlineList;
    private List<PeakKline> breakPeakKlineList;
    private List<PeakKline> jumpPeakKlineList;
    private List<PeakKline> tendencyPeakKlineList;
    private List<PeakKline> oppositeTendencyPeakKlineList;
    private List<PeakKline> independentTendencyPeakKlineList;

    public PeakKlineProcessor(KlineApplicationContext klineApplicationContext) {
        this.klineMap = klineApplicationContext.getKlineMap();
        this.klineList = klineApplicationContext.getKlineList();
        this.mergeKlineList = klineApplicationContext.getMergeKlineList();
        setPeakKlineList(mergeKlineList);
        setBreakPeakKlineList(peakKlineList);
        setJumpPeakKlineList(mergeKlineList, breakPeakKlineList);
        setTendencyPeakKlineList(mergeKlineList, breakPeakKlineList);
        setOppositeTendencyPeakKline(tendencyPeakKlineList);
        setInDependentTendencyPeakKlineList();
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


//    private void setEqualDirectPeakKlineList(List<PeakKline> peakKlineList) {
//        PeakKline prev = peakKlineList.get(0);
//        for (int i = 1; i < peakKlineList.size(); i++) {
//            PeakKline curr = peakKlineList.get(i);
//            if (prev.getPeakShape() != curr.getPeakShape()) {
//                prev = curr;
//            } else {
//                Kline left = prev.getMergeKline().getMergeKline();
//                Kline right = curr.getMergeKline().getMergeKline();
//                if (prev.getPeakShape() == PeakShapeEnum.TOP && left.getHigh() < right.getHigh()) {
//                    prev.setIsEqualDirectPeak(true);
//                }
//                if (prev.getPeakShape() == PeakShapeEnum.FLOOR && left.getLow() > right.getLow()) {
//                    prev.setIsEqualDirectPeak(true);
//                }
//            }
//        }
//    }

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
            //PeakKline prev = oppositeTendencyPeakKlineList.get(i - 1);
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

        deleteContainTenencyPeak();

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

    private void deleteContainTenencyPeak() {

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



    /*private List<PeakKline> ensureReservePeak(List<MergeKline> combineKlineList, List<PeakKline> tendencyPeakKlineList) {
        List<PeakKline> ensureReservePeakKlineList = new LinkedList(tendencyPeakKlineList);
            //(tendencyPeakKlineList.stream()
                  //.filter(peak -> peak.getReserveType() != LineReserveTypeEnum.NONE).collect(Collectors.toList()));
        Iterator<PeakKline> peakKlineIterator = ensureReservePeakKlineList.iterator();
        PeakKline parent = null;
        PeakKline prev = peakKlineIterator.next();
        while(peakKlineIterator.hasNext()) {
            PeakKline curr = peakKlineIterator.next();

            if (!curr.isJumpPeak()
                && curr.getMergeKlineIndex() - prev.getMergeKlineIndex() < 4
                && (curr.getPeakShape() == PeakShapeEnum.FLOOR && parent != null && parent.getMergeKline().getMergeKline().getLow() <= curr.getMergeKline().getMergeKline().getLow() || curr.getPeakShape() == PeakShapeEnum.TOP && parent != null
                && parent.getMergeKline().getMergeKline().getHigh() >= curr.getMergeKline().getMergeKline().getHigh())
                && (!prev.isJumpPeak() || !curr.isTendencyPeak())
            ) {
                peakKlineIterator.remove();
            } else if (prev.getShapeType() == LineShapeEnum.FLOOR
                && prev.getShapeType() != curr.getShapeType()
                && curr.getCombineKline().getMergeKline().getHigh() < combineKlineList.get(prev.getCombineIndex() - 1).getMergeKline().getHigh()) {
                peakKlineIterator.remove();
            } else if (prev.getShapeType() == LineShapeEnum.TOP
                && prev.getShapeType() != curr.getShapeType()
                && curr.getCombineKline().getMergeKline().getLow() > combineKlineList.get(prev.getCombineIndex() - 1).getMergeKline().getLow()) {
                peakKlineIterator.remove();
            } else {
                parent = prev;
                prev = curr;
            }
        }

        return deleteEqualDirectPeak(ensureReservePeakKlineList);
    }*/
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


    public List<PeakKline> getOppositeTendencyPeakKlineList() {
        return oppositeTendencyPeakKlineList;
    }

    public List<PeakKline> getIndependentTendencyPeakKlineList() {
        return independentTendencyPeakKlineList;
    }

}
