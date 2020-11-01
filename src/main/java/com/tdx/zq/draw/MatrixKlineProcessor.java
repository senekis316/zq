package com.tdx.zq.draw;

import java.util.List;

import com.tdx.zq.draw.PeakKlineProcessor.MatrixKlineRow;
import com.tdx.zq.enums.PeakShapeEnum;
import com.tdx.zq.enums.TendencyTypeEnum;
import com.tdx.zq.utils.JacksonUtils;

public class MatrixKlineProcessor {

    private List<MatrixKlineRow> matrixKlineRowList;

    public MatrixKlineProcessor(List<MatrixKlineRow> matrixKlineRowList) {
        this.matrixKlineRowList = matrixKlineRowList;
        //setMatrixTendency();
    }

    public void setMatrixTendency() {
        for (int i = 0; i < matrixKlineRowList.size() - 3; i++) {
            if (matrixKlineRowList.get(i).getShape() == PeakShapeEnum.FLOOR) {
                if (matrixKlineRowList.get(i).getLow() <= matrixKlineRowList.get(i + 2).getLow()
                        && matrixKlineRowList.get(i + 1).getHigh() <= matrixKlineRowList.get(i + 3).getHigh()) {
                    matrixKlineRowList.get(i).setTendency(TendencyTypeEnum.UP);
                } else {
                    matrixKlineRowList.get(i).setTendency(TendencyTypeEnum.DOWN);
                }
            } else {
                if (matrixKlineRowList.get(i).getHigh() <= matrixKlineRowList.get(i + 2).getHigh()
                        && matrixKlineRowList.get(i + 1).getLow() <= matrixKlineRowList.get(i + 3).getLow()) {
                    matrixKlineRowList.get(i).setTendency(TendencyTypeEnum.UP);
                } else {
                    matrixKlineRowList.get(i).setTendency(TendencyTypeEnum.DOWN);
                }
            }
        }
        System.out.println(JacksonUtils.toJson(matrixKlineRowList));
    }


}
