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

    public List<Kline> computerPeakKlines(Map<Integer, Kline> originalKLineMap, List<Kline> originalKlineList, List<CombineKline> combineKlineList) {

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

    private List<PeakKline> tendencyReservePeak(List<CombineKline> combineKlineList,
                                                List<PeakKline> noEnsureReservePeakKlineList) {
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

        List<PeakKline> dropTendencyPeakKlineList = noEnsureReservePeakKlineList.stream().filter(peak -> peak.getReserveType() == LineReserveTypeEnum.DROP).collect(Collectors.toList());
        System.out.println("dropTendencyPeakKlineList: " +
                JacksonUtils.toJson(dropTendencyPeakKlineList.stream().map(peakKline -> peakKline.getCombineKline().getKline()).collect(Collectors.toList())));

        return noEnsureReservePeakKlineList.stream()
                    .filter(peak -> peak.getReserveType() != LineReserveTypeEnum.DROP)
                        .collect(Collectors.toList());
    }

    private List<PeakKline> ensureReservePeak(List<CombineKline> combineKlineList, List<PeakKline> tendencyReservePeakKlineList) {
        List<PeakKline> ensureReservePeakKlineList = new LinkedList(tendencyReservePeakKlineList.stream().filter(peak -> peak.getReserveType() != LineReserveTypeEnum.NONE).collect(Collectors.toList()));
        Iterator<PeakKline> peakKlineIterator = ensureReservePeakKlineList.iterator();
        PeakKline prev = peakKlineIterator.next();

        while(peakKlineIterator.hasNext()) {
            PeakKline curr = peakKlineIterator.next();
            if (prev.getShapeType() == curr.getShapeType()) {
                peakKlineIterator.remove();
            } else if (curr.getCombineIndex() - prev.getCombineIndex() < 4 && curr.getReserveType() != LineReserveTypeEnum.JUMP) {
                peakKlineIterator.remove();
            } else if (prev.getShapeType() == LineShapeEnum.FLOOR && prev.getShapeType() != curr.getShapeType() && curr.getCombineKline().getKline().getHigh() < combineKlineList.get(prev.getCombineIndex() - 1).getKline().getHigh()) {
                peakKlineIterator.remove();
            } else if (prev.getShapeType() == LineShapeEnum.TOP && prev.getShapeType() != curr.getShapeType() && curr.getCombineKline().getKline().getLow() > combineKlineList.get(prev.getCombineIndex() - 1).getKline().getLow()) {
                peakKlineIterator.remove();
            } else {
                prev = curr;
            }
        }

        return ensureReservePeakKlineList;
    }



}
