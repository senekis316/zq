package com.tdx.zq.draw;

import com.tdx.zq.enums.LineReserveTypeEnum;
import com.tdx.zq.enums.LineShapeEnum;
import com.tdx.zq.model.CombineKline;
import com.tdx.zq.model.Kline;
import com.tdx.zq.model.PeakKline;
import com.tdx.zq.utils.JacksonUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Component
public class PeakKlineService {

    public List<PeakKline> computerPeakKlines(List<CombineKline> combineKlineList) {

        //1.获取所有的峰值点
        List<PeakKline> allPeakKlineList = this.computerAllPeakKline(combineKlineList);
        System.out.println("allPeakKlineList: " + JacksonUtils.toJson(allPeakKlineList));

        //2.过滤不符合条件的峰值点
        //(1)过滤了2,3两个K线Break峰值点的情况
        List<PeakKline> noEnsureReservePeakKlineList = filterTwoThreeBreak(combineKlineList, allPeakKlineList);
        System.out.println("twoThreeBreakPeakKlineList: " +
                JacksonUtils.toJson(noEnsureReservePeakKlineList.stream().map(peakKline -> peakKline.getCombineKline().getKline()).collect(Collectors.toList())));

        //3.跳空保留峰值点
        jumpReservePeak(combineKlineList, noEnsureReservePeakKlineList);
        System.out.println("jumpBreakPeakKlineList: " +
                JacksonUtils.toJson(noEnsureReservePeakKlineList.stream().map(peakKline -> peakKline.getCombineKline().getKline()).collect(Collectors.toList())));

        //4.趋势保留峰值点
        List<PeakKline> tendencyReservePeakKlineList = tendencyReservePeak(combineKlineList, noEnsureReservePeakKlineList);
        System.out.println("tendencyReservePeakKlineList: " +
                JacksonUtils.toJson(tendencyReservePeakKlineList.stream().map(peakKline -> peakKline.getCombineKline().getKline()).collect(Collectors.toList())));

        //5.确定保留点
        List<PeakKline> ensureReservePeakKlineList = ensureReservePeak(combineKlineList, tendencyReservePeakKlineList);
        System.out.println("ensureReservePeakKlineList: " +
                JacksonUtils.toJson(ensureReservePeakKlineList.stream().map(peakKline -> peakKline.getCombineKline().getKline()).collect(Collectors.toList())));

        //6.修正点处理
        List<PeakKline> correctedPeakKlineList = correctedPeak(ensureReservePeakKlineList);
        System.out.println("correctedPeakKlineList: " +
                JacksonUtils.toJson(correctedPeakKlineList.stream().map(peakKline -> peakKline.getCombineKline().getKline()).collect(Collectors.toList())));

        //7.加入一个未成形的波峰波谷点
        PeakKline specialPeakKline = specialPeak(combineKlineList, correctedPeakKlineList);
        List<PeakKline> addSpecialPeakKlineList = correctedPeakKlineList;
        addSpecialPeakKlineList.add(specialPeakKline);
        System.out.println("addSpecialPeakKlineList: " +
                JacksonUtils.toJson(addSpecialPeakKlineList.stream().map(peakKline -> peakKline.getCombineKline().getKline()).collect(Collectors.toList())));

        return addSpecialPeakKlineList;
    }

    private PeakKline specialPeak(List<CombineKline> combineKlineList, List<PeakKline> peakKlineList) {
        PeakKline lastPeakKline = peakKlineList.get(peakKlineList.size() - 1);
        Kline lastKline = lastPeakKline.getCombineKline().getKline();
        CombineKline specialCombineKline = null;
        Integer specialIndex = null;
        if (lastPeakKline.getShapeType() == LineShapeEnum.FLOOR) {
            int max = lastKline.getHigh();
            for (int i = lastPeakKline.getCombineIndex() + 1; i < combineKlineList.size(); i++) {
                Kline currentKline = combineKlineList.get(i).getKline();
                if (currentKline.getHigh() > max) {
                    max = currentKline.getHigh();
                    specialCombineKline = combineKlineList.get(i);
                    specialIndex = i;
                }
            }
        } else {
            int min = lastKline.getLow();
            for (int i = lastPeakKline.getCombineIndex() + 1; i < combineKlineList.size(); i++) {
                Kline currentKline = combineKlineList.get(i).getKline();
                if (currentKline.getLow() < min) {
                    min = currentKline.getLow();
                    specialCombineKline = combineKlineList.get(i);
                    specialIndex = i;
                }
            }
        }

        LineShapeEnum lineShapeEnum = lastPeakKline.getShapeType() == LineShapeEnum.FLOOR  ? LineShapeEnum.TOP : LineShapeEnum.FLOOR;

        if (specialCombineKline != null) {
            return new PeakKline(specialCombineKline, specialIndex, lineShapeEnum);
        }

        return null;
    }

    public List<PeakKline> computerAllPeakKline(List<CombineKline> combineKlineList) {
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
    }

    private List<PeakKline> filterTwoThreeBreak(
            List<CombineKline> combineKlineList,
            List<PeakKline> allPeakKlineList) {

        List<PeakKline> noEnsureReservePeakKlineList = new ArrayList<>();

        for (int i = 0; i < allPeakKlineList.size() - 4; i++) {
            Integer index = allPeakKlineList.get(i).getCombineIndex();
            Kline middle = combineKlineList.get(index).getKline();
            Kline right = combineKlineList.get(index + 1).getKline();
            Kline second = combineKlineList.get(index + 2).getKline();
            Kline third = combineKlineList.get(index + 3).getKline();

            //波谷
            if (middle.getLow() < right.getLow()) {
                if (middle.getLow() > second.getLow()) {
                    continue;
                }
                if (middle.getLow() > third.getLow()) {
                    continue;
                }
                noEnsureReservePeakKlineList.add(allPeakKlineList.get(i));
            } else if (middle.getHigh() > right.getHigh()) {
                if (middle.getHigh() < second.getHigh()) {
                    continue;
                }
                if (middle.getHigh() < third.getHigh()) {
                    continue;
                }
                noEnsureReservePeakKlineList.add(allPeakKlineList.get(i));
            }
        }
        return noEnsureReservePeakKlineList;
    }


    private void jumpReservePeak(List<CombineKline> combineKlineList,
                    List<PeakKline> noEnsureReservePeakKlineList) {

        for (int i = 0; i < noEnsureReservePeakKlineList.size(); i++) {
            Integer index = noEnsureReservePeakKlineList.get(i).getCombineIndex();
            Kline left = combineKlineList.get(index - 1).getKline();
            Kline middle = combineKlineList.get(index).getKline();
            Kline right = combineKlineList.get(index + 1).getKline();
            Kline second = combineKlineList.get(index + 2).getKline();
            Kline third = combineKlineList.get(index + 3).getKline();

            if (middle.getLow() < right.getLow()) {
                if (right.getHigh() < second.getLow() && second.getLow() < left.getHigh()) {
                    noEnsureReservePeakKlineList.get(i).setReserveType(LineReserveTypeEnum.JUMP);
                    continue;
                }
                if (left.getHigh() < third.getLow() && right.getHigh() < third.getLow() && second.getHigh() < third.getLow()) {
                    noEnsureReservePeakKlineList.get(i).setReserveType(LineReserveTypeEnum.JUMP);
                    continue;
                }
            } else if (middle.getHigh() > right.getHigh()) {
                if (left.getLow() > second.getHigh() && right.getLow() > second.getHigh()) {
                    noEnsureReservePeakKlineList.get(i).setReserveType(LineReserveTypeEnum.JUMP);
                    continue;
                }
                if (left.getLow() > third.getHigh() && right.getLow() > third.getHigh() && second.getLow() > third.getHigh()) {
                    noEnsureReservePeakKlineList.get(i).setReserveType(LineReserveTypeEnum.JUMP);
                    continue;
                }
            }
        }
    }

    private List<PeakKline> deleteEqualDirectPeak(List<PeakKline> peakKlineList) {

        List<PeakKline> deletedEqualDirectPeakList = new ArrayList<>();

        for (int i = 1; i < peakKlineList.size(); i++) {
            PeakKline prev;
            PeakKline curr = peakKlineList.get(i);

            if (deletedEqualDirectPeakList.size() == 0) {
                prev = peakKlineList.get(i - 1);
            } else {
                prev = deletedEqualDirectPeakList.get(deletedEqualDirectPeakList.size() - 1);
            }

            if (prev.getShapeType() != curr.getShapeType()) {
                deletedEqualDirectPeakList.add(curr);
            } else {
                if (curr.getShapeType() == LineShapeEnum.TOP) {
                    if (prev.getCombineKline().getKline().getHigh() < curr.getCombineKline().getKline().getHigh()) {
                        deletedEqualDirectPeakList.set(deletedEqualDirectPeakList.size() - 1, curr);
                    }
                }

                if (curr.getShapeType() == LineShapeEnum.FLOOR) {
                    if (prev.getCombineKline().getKline().getLow() > curr.getCombineKline().getKline().getLow()) {
                        deletedEqualDirectPeakList.set(deletedEqualDirectPeakList.size() - 1, curr);
                    }
                }
            }
        }
        return deletedEqualDirectPeakList;
    }

    private List<PeakKline> tendencyReservePeak(List<CombineKline> combineKlineList,
                                                List<PeakKline> noEnsureReservePeakKlineList) {

        noEnsureReservePeakKlineList = deleteEqualDirectPeak(noEnsureReservePeakKlineList);

        for (int i = 0; i < noEnsureReservePeakKlineList.size(); i++) {
            if (noEnsureReservePeakKlineList.get(i).getReserveType() != LineReserveTypeEnum.JUMP) {
                Integer index = noEnsureReservePeakKlineList.get(i).getCombineIndex();
                Kline left = combineKlineList.get(index - 1).getKline();
                Kline middle = combineKlineList.get(index).getKline();
                Kline right = combineKlineList.get(index + 1).getKline();
                Kline second = combineKlineList.get(index + 2).getKline();
                Kline third = combineKlineList.get(index + 3).getKline();

                if (middle.getLow() < right.getLow()) {
                    int max = Arrays.stream(new int[]{left.getHigh(), middle.getHigh(), right.getHigh(), second.getHigh(), third.getHigh()}).max().getAsInt();
                    for (int j = index + 4; j < combineKlineList.size(); j++) {
                        if (max <= combineKlineList.get(j).getKline().getHigh()) {
                            noEnsureReservePeakKlineList.get(i).setReserveType(LineReserveTypeEnum.TENDENCY);
                            break;
                        }
                        if (middle.getLow() > combineKlineList.get(j).getKline().getLow()) {
                            noEnsureReservePeakKlineList.get(i).setReserveType(LineReserveTypeEnum.DROP);
                            break;
                        }
                    }
                } else if (middle.getHigh() > right.getHigh()) {
                    int min = Arrays.stream(new int[]{left.getLow(), middle.getLow(), right.getLow(), second.getLow(), third.getLow()}).min().getAsInt();
                    for (int j = index + 4; j < combineKlineList.size(); j++) {
                        if (min >= combineKlineList.get(j).getKline().getLow()) {
                            noEnsureReservePeakKlineList.get(i).setReserveType(LineReserveTypeEnum.TENDENCY);
                            break;
                        }
                        if (middle.getHigh() < combineKlineList.get(j).getKline().getHigh()) {
                            noEnsureReservePeakKlineList.get(i).setReserveType(LineReserveTypeEnum.DROP);
                            break;
                        }
                    }
                }
            }
        }

        noEnsureReservePeakKlineList =  noEnsureReservePeakKlineList.stream()
                    .filter(peak -> peak.getReserveType() != LineReserveTypeEnum.DROP)
                        .collect(Collectors.toList());

        return deleteEqualDirectPeak(noEnsureReservePeakKlineList);
    }

    private List<PeakKline> ensureReservePeak(List<CombineKline> combineKlineList, List<PeakKline> tendencyReservePeakKlineList) {
        List<PeakKline> ensureReservePeakKlineList = new LinkedList(tendencyReservePeakKlineList.stream().filter(peak -> peak.getReserveType() != LineReserveTypeEnum.NONE).collect(Collectors.toList()));
        Iterator<PeakKline> peakKlineIterator = ensureReservePeakKlineList.iterator();
        PeakKline parent = null;
        PeakKline prev = peakKlineIterator.next();
        while(peakKlineIterator.hasNext()) {
            PeakKline curr = peakKlineIterator.next();

            if (curr.getCombineIndex() - prev.getCombineIndex() < 4
                    && curr.getReserveType() != LineReserveTypeEnum.JUMP
                        && (curr.getShapeType() == LineShapeEnum.FLOOR && parent != null && parent.getCombineKline().getKline().getLow() <= curr.getCombineKline().getKline().getLow()
                            || curr.getShapeType() == LineShapeEnum.TOP && parent != null && parent.getCombineKline().getKline().getHigh() >= curr.getCombineKline().getKline().getHigh())) {
                peakKlineIterator.remove();
            } else if (prev.getShapeType() == LineShapeEnum.FLOOR && prev.getShapeType() != curr.getShapeType() && curr.getCombineKline().getKline().getHigh() < combineKlineList.get(prev.getCombineIndex() - 1).getKline().getHigh()) {
                peakKlineIterator.remove();
            } else if (prev.getShapeType() == LineShapeEnum.TOP && prev.getShapeType() != curr.getShapeType() && curr.getCombineKline().getKline().getLow() > combineKlineList.get(prev.getCombineIndex() - 1).getKline().getLow()) {
                peakKlineIterator.remove();
            } else {
                parent = prev;
                prev = curr;
            }
        }

        return deleteEqualDirectPeak(ensureReservePeakKlineList);
    }

    private List<PeakKline> correctedPeak(List<PeakKline> ensureReservePeakKlineList) {
        for (int i = 0; i < ensureReservePeakKlineList.size() - 3; i++) {
            PeakKline peak1 = ensureReservePeakKlineList.get(i);
            PeakKline peak2 = ensureReservePeakKlineList.get(i+1);
            PeakKline peak3 = ensureReservePeakKlineList.get(i+2);
            PeakKline peak4 = ensureReservePeakKlineList.get(i+3);

            if (peak4.getCombineIndex() - peak3.getCombineIndex() < 4 && peak4.getReserveType() != LineReserveTypeEnum.JUMP) {
                if (peak1.getShapeType() == LineShapeEnum.TOP) {
                    if (peak1.getCombineKline().getKline().getHigh() > peak3.getCombineKline().getKline().getHigh()
                            && peak2.getCombineKline().getKline().getLow() > peak4.getCombineKline().getKline().getLow()) {
                        peak2.setReserveType(LineReserveTypeEnum.DROP);
                        peak3.setReserveType(LineReserveTypeEnum.DROP);
                    }
                } else {
                    if (peak1.getCombineKline().getKline().getLow() < peak3.getCombineKline().getKline().getLow()
                            && peak2.getCombineKline().getKline().getHigh() < peak4.getCombineKline().getKline().getHigh()) {
                        peak2.setReserveType(LineReserveTypeEnum.DROP);
                        peak3.setReserveType(LineReserveTypeEnum.DROP);
                    }
                }
            }
        }
        ensureReservePeakKlineList = ensureReservePeakKlineList.stream().filter(peak -> peak.getReserveType() != LineReserveTypeEnum.DROP).collect(Collectors.toList());

        return deleteEqualDirectPeak(ensureReservePeakKlineList);
    }

}
