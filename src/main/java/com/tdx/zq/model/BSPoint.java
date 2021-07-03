package com.tdx.zq.model;

import com.tdx.zq.enums.PointType;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

@Data
public class BSPoint {
    private long date;
    private PointType pointType;
    private List<Matrix> matrixs;

    public BSPoint(long date, PointType pointType, List<Matrix> matrixs) {
        this.date = date;
        this.matrixs = matrixs;
        this.pointType = pointType;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(pointType).append(": ").append(date);
        if (CollectionUtils.isNotEmpty(matrixs)) {
            sb.append("; Tendency: " + matrixs.get(0).getTendency());
            sb.append(": " + matrixs.size());
        }
        return sb.toString();
    }
}
