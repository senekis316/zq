package com.tdx.zq.draw;

import com.tdx.zq.context.KlineApplicationContext;
import com.tdx.zq.enums.LineDirectEnum;
import com.tdx.zq.model.CombineKline;
import com.tdx.zq.model.Kline;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CombineKlineHandler {

    private List<Kline> klineList;

    public CombineKlineHandler(KlineApplicationContext klineApplicationContext) {
        this.klineList = klineApplicationContext.getKlineList();
    }

    public List<CombineKline> computerCombineKlines() {

        LineDirectEnum lineDirectEnum = LineDirectEnum.BALANCE;

        List<CombineKline> combineKlineList = new LinkedList();
        combineKlineList.add(new CombineKline(klineList.get(0), 0));

        for (int i = 1; i < klineList.size(); i++) {
            CombineKline combineKline = combineKlineList.get(combineKlineList.size() - 1);
            Kline leftKline = combineKline.getKline();
            Kline rightKline = klineList.get(i);
            lineDirectEnum = computeDirect(leftKline, rightKline, lineDirectEnum);

            if (lineDirectEnum != LineDirectEnum.BALANCE) {
                if (isContain(leftKline, rightKline, lineDirectEnum)) {
                    kLineCombine(leftKline, rightKline, lineDirectEnum);
                    combineKline.getContains().add(new Kline(rightKline));
                } else {
                    combineKlineList.add(new CombineKline(rightKline, combineKlineList.size()));
                }
            }
        }

        return combineKlineList;

    }

    /*public List<CombineKline> computerCombineKlines(List<Kline> klineList) {

        LineDirectEnum lineDirectEnum = LineDirectEnum.BALANCE;

        LinkedList<Kline> filterKlineList = new LinkedList();
        filterKlineList.add(klineList.get(0));

        for (int i = 1; i < klineList.size(); i++) {
            Kline leftKline = filterKlineList.getLast();
            Kline rightKline = klineList.get(i);
            lineDirectEnum = computeDirect(leftKline, rightKline, lineDirectEnum);

            if (lineDirectEnum != LineDirectEnum.BALANCE) {
                if (isContain(leftKline, rightKline, lineDirectEnum)) {
                    leftKline = kLineCombine(leftKline, rightKline, lineDirectEnum);
                    filterKlineList.set(filterKlineList.size() - 1, leftKline);
                } else {
                    filterKlineList.add(rightKline);
                }
            }
        }

        List<CombineKline> combineKlineList = new ArrayList<>();
        for (int i = 0; i < filterKlineList.size(); i++) {
            combineKlineList.add(new CombineKline(filterKlineList.get(i), i));
        }

        return combineKlineList;

    }*/

    //1.判断K线涨跌趋势:
    //(1)如果右边的K线，High值比左边的High值高，Low值比左边的Low值高，则增长趋势。
    //(2)如果右边的K线，High值比左边的High值低，Low值比左边的Low值低，则下降趋势。
    //(3)除以上两种情况，均无法确定趋势。
    //(4)当无法确定K线的涨跌趋势时，已最近的前一个K线趋势，作为当前K线的趋势。
    public LineDirectEnum computeDirect(Kline leftKLine, Kline rightKLine, LineDirectEnum directEnum) {

        if (directEnum == null) {
            throw new IllegalArgumentException("LineDirectEnum不能为空!");
        }

        if (rightKLine.getHigh() > leftKLine.getHigh() && rightKLine.getLow() > leftKLine.getLow()) {
            return LineDirectEnum.UP;
        }

        if (rightKLine.getHigh() < leftKLine.getHigh() && rightKLine.getLow() < leftKLine.getLow()) {
            return LineDirectEnum.DOWN;
        }

        return directEnum;

    }

    //2.两条K线是否是包含关系:
    //(1)如果左边的K线，High值大于等于右边的High值，Low值小于等于右边的Low值，则为包含关系。
    //(2)如果右边的K线，High值大于等于左边的High值，Low值小于等于左边的Low值，则为包含关系。
    //(3)满足以上条件后，K线必须要能存在趋势，才能确定为包含关系。
    //(4)满足以上所有条件，左右K线为包含关系，其余情况均非包含关系。
    public boolean isContain(Kline leftKLine, Kline rightKLine, LineDirectEnum directEnum) {

        if (directEnum == LineDirectEnum.BALANCE) {
            return false;
        }

        if (leftKLine.getHigh() >= rightKLine.getHigh() && leftKLine.getLow() <= rightKLine.getLow()) {
            return true;
        }

        if (rightKLine.getHigh() >= leftKLine.getHigh() && rightKLine.getLow() <= leftKLine.getLow()) {
            return true;
        }

        return false;

    }

    //3.K线包含:
    //(1)只要是包含关系，包含后K线日期，永远取右边K线的日期。
    //(2)如果是增长趋势，合并后K线的High值取两条K线High值的最大值，合并后K线的Low值取两条K线Low值的最大值。
    //(3)如果是减少趋势，合并后K线的High值取两条K线High值的最小值，合并后K线的Low值取两条K线Low值的最小值。
    //(4)右边K线与左边K线只要满足包含关系，可以进行连续的包含合并。
    public Kline kLineCombine(Kline leftKLine, Kline rightKLine, LineDirectEnum directEnum) {

        if (directEnum == null) {
            throw new IllegalArgumentException("LineDirectEnum不能为空!");
        }

        if (directEnum == LineDirectEnum.BALANCE) {
            throw new IllegalArgumentException("KLineCombine方法不能处理LineDirectEnum.Balance的情况!");
        }

        leftKLine.setDate(rightKLine.getDate());

        if (directEnum == LineDirectEnum.UP) {
            leftKLine.setLow(Math.max(leftKLine.getLow(), rightKLine.getLow()));
            leftKLine.setHigh(Math.max(leftKLine.getHigh(), rightKLine.getHigh()));
        } else {
            leftKLine.setLow(Math.min(leftKLine.getLow(), rightKLine.getLow()));
            leftKLine.setHigh(Math.min(leftKLine.getHigh(), rightKLine.getHigh()));
        }

        leftKLine.setCode(rightKLine.getCode());
        leftKLine.setAmount(leftKLine.getAmount() + rightKLine.getAmount());
        leftKLine.setVolume(leftKLine.getVolume() + rightKLine.getVolume());
        leftKLine.setOpen(Math.max(leftKLine.getOpen(), rightKLine.getOpen()));
        leftKLine.setClose(Math.max(leftKLine.getClose(), rightKLine.getClose()));

        return leftKLine;

    }


}
