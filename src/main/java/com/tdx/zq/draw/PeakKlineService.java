package com.tdx.zq.draw;

import com.tdx.zq.model.CombineKline;
import com.tdx.zq.model.Kline;
import com.tdx.zq.model.PeakKline;
import com.tdx.zq.utils.JacksonUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        List<PeakKline> jumpReservePeakKlineList = jumpReservePeak(combineKlineList, noEnsureReservePeakKlineList);
        System.out.println("jumpReservePeakKlineList: " +
                JacksonUtils.toJson(jumpReservePeakKlineList.stream().map(peakKline -> peakKline.getCombineKline().getKline()).collect(Collectors.toList())));

        return null;
    }

    public List<PeakKline> computerAllPeakKline(List<CombineKline> combineKlineList) {
        List<PeakKline> allPeakKlineList = new ArrayList<>();
        for (int i = 1; i < combineKlineList.size() - 1; i++) {
            Kline left = combineKlineList.get(i - 1).getKline();
            Kline middle = combineKlineList.get(i).getKline();
            Kline right = combineKlineList.get(i + 1).getKline();
            if (middle.getHigh() > left.getHigh() && middle.getHigh() > right.getHigh()) {
                allPeakKlineList.add(new PeakKline(combineKlineList.get(i), i));
            } else if (middle.getLow() < left.getLow() && middle.getLow() < right.getLow()) {
                allPeakKlineList.add(new PeakKline(combineKlineList.get(i), i));
            }
        }
        return allPeakKlineList;
    }

    private List<PeakKline> filterTwoThreeBreak(
            List<CombineKline> combineKlineList,
            List<PeakKline> allPeakKlineList) {

        List<PeakKline> noEnsureReservePeakKlineList = new ArrayList<>();

        for (int i = 0; i < allPeakKlineList.size(); i++) {
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


    private List<PeakKline> jumpReservePeak(List<CombineKline> combineKlineList,
                    List<PeakKline> noEnsureReservePeakKlineList) {

        List<PeakKline> jumpReservePeakKlineList = new ArrayList<>();

        for (int i = 0; i < noEnsureReservePeakKlineList.size(); i++) {
            Integer index = noEnsureReservePeakKlineList.get(i).getCombineIndex();
            Kline left = combineKlineList.get(index - 1).getKline();
            Kline middle = combineKlineList.get(index).getKline();
            Kline right = combineKlineList.get(index + 1).getKline();
            Kline second = combineKlineList.get(index + 2).getKline();
            Kline third = combineKlineList.get(index + 3).getKline();

            if (middle.getLow() < right.getLow()) {
                if (right.getHigh() < second.getLow() && second.getLow() < left.getHigh()) {
                    jumpReservePeakKlineList.add(noEnsureReservePeakKlineList.get(i));
                    continue;
                }
                if (left.getHigh() < third.getLow() && right.getHigh() < third.getLow() && second.getHigh() < third.getLow()) {
                    jumpReservePeakKlineList.add(noEnsureReservePeakKlineList.get(i));
                    continue;
                }
            } else if (middle.getHigh() > right.getHigh()) {
                if (left.getLow() > second.getHigh() && right.getLow() > second.getHigh()) {
                    jumpReservePeakKlineList.add(noEnsureReservePeakKlineList.get(i));
                    continue;
                }
                if (left.getLow() > third.getHigh() && right.getLow() > third.getHigh() && second.getLow() > third.getHigh()) {
                    jumpReservePeakKlineList.add(noEnsureReservePeakKlineList.get(i));
                    continue;
                }
            }
        }

        return jumpReservePeakKlineList;

    }
}
