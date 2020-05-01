package com.tdx.zq.draw;

import com.tdx.zq.enums.LineDirectEnum;
import com.tdx.zq.model.Kline;
import com.tdx.zq.model.MergeKline;
import java.util.ArrayList;
import java.util.List;

public class MergeKlineProcessor {

    private List<Kline> klineList;
    private LineDirectEnum lineDirectEnum;
    private List<MergeKline> mergeKlineList;

    public MergeKlineProcessor(List<Kline> klineList) {
        this.klineList = klineList;
        this.lineDirectEnum = LineDirectEnum.BALANCE;
        setMergeKlineList(klineList);
    }

    public List<MergeKline> getMergeKlineList() {
        return mergeKlineList;
    }

    public void setMergeKlineList(List<Kline> klineList) {
        List<Kline> klines = new ArrayList<>();
        Kline left = new Kline(klineList.get(0));
        List<MergeKline> mergeKlineList = new ArrayList<>();
        for (int i = 1; i < klineList.size(); i++) {
            Kline right = new Kline(klineList.get(i));
            if (computeDirect(left, right) != LineDirectEnum.BALANCE && shouldMerge(left, right)) {
                klines.add(left);
                left = mergeKline(left, right);
            } else {
                mergeKlineList.add(new MergeKline(left, klines));
                klines.clear();
                left = right;
            }
            if (i == klineList.size() - 1 && klineList.size() > 0) {
                mergeKlineList.add(new MergeKline(left, klines));
            }
        }
        this.mergeKlineList = mergeKlineList;
    }

    //1.判断K线涨跌趋势:
    //(1)如果右边的K线，High值比左边的High值高，Low值比左边的Low值高，则增长趋势。
    //(2)如果右边的K线，High值比左边的High值低，Low值比左边的Low值低，则下降趋势。
    //(3)除以上两种情况，均无法确定趋势。
    //(4)当无法确定K线的涨跌趋势时，已最近的前一个K线趋势，作为当前K线的趋势。
    public LineDirectEnum computeDirect(Kline leftKLine, Kline rightKLine) {
        if (rightKLine.getHigh() > leftKLine.getHigh() && rightKLine.getLow() > leftKLine.getLow()) {
            this.lineDirectEnum = LineDirectEnum.UP;
        } else if (rightKLine.getHigh() < leftKLine.getHigh() && rightKLine.getLow() < leftKLine.getLow()) {
            this.lineDirectEnum = LineDirectEnum.DOWN;
        }
        return lineDirectEnum;
    }

    //2.两条K线是否是包含关系:
    //(1)如果左边的K线，High值大于等于右边的High值，Low值小于等于右边的Low值，则为包含关系。
    //(2)如果右边的K线，High值大于等于左边的High值，Low值小于等于左边的Low值，则为包含关系。
    //(3)满足以上条件后，K线必须要能存在趋势，才能确定为包含关系。
    //(4)满足以上所有条件，左右K线为包含关系，其余情况均非包含关系。
    public boolean shouldMerge(Kline leftKLine, Kline rightKLine) {

        if (lineDirectEnum == LineDirectEnum.BALANCE) {
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
    public Kline mergeKline(Kline leftKLine, Kline rightKLine) {

        Kline combineKline = new Kline(leftKLine);

        if (lineDirectEnum == LineDirectEnum.BALANCE) {
            throw new IllegalArgumentException("KLineCombine方法不能处理LineDirectEnum.Balance的情况!");
        }

        combineKline.setDate(rightKLine.getDate());

        if (lineDirectEnum == LineDirectEnum.UP) {
            combineKline.setLow(Math.max(leftKLine.getLow(), rightKLine.getLow()));
            combineKline.setHigh(Math.max(leftKLine.getHigh(), rightKLine.getHigh()));
        } else {
            combineKline.setLow(Math.min(leftKLine.getLow(), rightKLine.getLow()));
            combineKline.setHigh(Math.min(leftKLine.getHigh(), rightKLine.getHigh()));
        }

        combineKline.setCode(rightKLine.getCode());
        combineKline.setAmount(leftKLine.getAmount() + rightKLine.getAmount());
        combineKline.setVolume(leftKLine.getVolume() + rightKLine.getVolume());
        combineKline.setOpen(Math.max(leftKLine.getOpen(), rightKLine.getOpen()));
        combineKline.setClose(Math.max(leftKLine.getClose(), rightKLine.getClose()));

        return combineKline;

    }


}
