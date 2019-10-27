package com.tdx.zq.model;

import com.tdx.zq.enums.LineShapeEnum;
import com.tdx.zq.enums.MatrixLineType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatrixLine {

    private int low;

    private int high;

    private int begin;

    private int end;

    private MatrixLineType matrixLineType;

   /* private int left;

    private int right;

    private boolean isBreak = false;

    private MatrixLineType matrixLineType;*/

    public MatrixLine(int low, int high, int begin, int end, MatrixLineType matrixLineType) {
        this.low = low;
        this.high = high;
        this.begin = begin;
        this.end = end;
        this.matrixLineType = matrixLineType;
    }


    /*public MatrixLine(int low, int high, int begin, int end, MatrixLineType matrixLineType) {
        this.low = low;
        this.high = high;
        this.begin = begin;
        this.end = end;
        this.matrixLineType = matrixLineType;
    }

    public MatrixLine(int low, int high, int begin, int left, LineShapeEnum lineShapeEnum) {
        this.low = low;
        this.high = high;
        this.begin = begin;
        this.left = left;
        this.matrixLineType = lineShapeEnum == LineShapeEnum.TOP ? MatrixLineType.DOWN : MatrixLineType.UP;
    }*/

}
