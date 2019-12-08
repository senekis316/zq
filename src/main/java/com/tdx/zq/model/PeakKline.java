package com.tdx.zq.model;

import com.tdx.zq.enums.LineReserveTypeEnum;
import com.tdx.zq.enums.LineShapeEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PeakKline{

    private int peakIndex;

    private int combineIndex;

    private LineShapeEnum shapeType;

    private CombineKline combineKline;

    private List<CombineKline> combineKlines;

    private LineReserveTypeEnum reserveType = LineReserveTypeEnum.NONE;

    public PeakKline(CombineKline combineKline, int combineIndex, LineShapeEnum shapeType) {
        this.combineKline = combineKline;
        this.combineIndex = combineIndex;
        this.shapeType = shapeType;
    }

    public PeakKline(List<CombineKline> combineKlines, CombineKline combineKline, int combineIndex, LineShapeEnum shapeType) {
        this.shapeType = shapeType;
        this.combineKline = combineKline;
        this.combineIndex = combineIndex;
        this.combineKlines = combineKlines;
    }

    public static List<PeakKline> computerAllPeakKline(List<CombineKline> combineKlineList) {
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

}
