package com.tdx.zq.model;

import com.tdx.zq.enums.LineReserveTypeEnum;
import com.tdx.zq.enums.LineShapeEnum;
import lombok.Data;

@Data
public class PeakKline{

    private CombineKline combineKline;

    private int combineIndex;

    private LineReserveTypeEnum reserveType = LineReserveTypeEnum.NONE;

    private LineShapeEnum shapeType;

    public PeakKline(CombineKline combineKline, int combineIndex, LineShapeEnum shapeType) {
        this.combineKline = combineKline;
        this.combineIndex = combineIndex;
        this.shapeType = shapeType;
    }

}
