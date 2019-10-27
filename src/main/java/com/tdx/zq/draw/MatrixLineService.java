package com.tdx.zq.draw;

import com.tdx.zq.enums.LineShapeEnum;
import com.tdx.zq.enums.MatrixLineType;
import com.tdx.zq.model.Kline;
import com.tdx.zq.model.MatrixLine;
import com.tdx.zq.model.PeakKline;
import com.tdx.zq.utils.JacksonUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class MatrixLineService {

    public List<MatrixLine> drawMatrix(List<PeakKline> peakLines) {

        //1.获取全部MatirxLine, 后期删除不符合条件的Matrix
        List<MatrixLine> allMatrixLineList = getAllMatrixs(peakLines);
        System.out.println("allMatrixLineList: " + JacksonUtils.toJson(allMatrixLineList));

        return null;

    }


    private List<MatrixLine> getAllMatrixs(List<PeakKline> peakKLines) {

        List<MatrixLine> matrixLineList = new ArrayList<>();

        for (int index = 0; index < peakKLines.size() - 4; index++) {

            PeakKline peak1 = peakKLines.get(index);
            PeakKline peak2 = peakKLines.get(index + 1);
            PeakKline peak3 = peakKLines.get(index + 2);
            PeakKline peak4 = peakKLines.get(index + 3);
            PeakKline peak5 = peakKLines.get(index + 4);

            Kline kline1 = peak1.getCombineKline().getKline();
            Kline kline2 = peak2.getCombineKline().getKline();
            Kline kline3 = peak3.getCombineKline().getKline();
            Kline kline4 = peak4.getCombineKline().getKline();
            Kline kline5 = peak5.getCombineKline().getKline();

            int begin = kline2.getDate();
            int end = kline5.getDate();

            if (kline1.getDate() == 20190819) {
                System.out.println(20190819);
            }

            if (peak1.getShapeType().equals(LineShapeEnum.FLOOR)) {
                int min = kline1.getLow();
                int max = Math.max(kline2.getHigh(), kline4.getHigh());
                if (kline3.getLow() >= kline1.getLow() && kline5.getLow() >= kline1.getLow() && kline5.getLow() <= kline2.getHigh()) {
                    int current = index + 5;
                    while (current < peakKLines.size()) {
                        Kline kline = peakKLines.get(current).getCombineKline().getKline();
                        if (kline.getHigh() >= max) {
                            int low = Math.max(kline3.getLow(), kline5.getLow());
                            int high = Math.min(kline2.getHigh(), kline4.getHigh());
                            MatrixLine matrixLine = new MatrixLine(low, high, begin, end, MatrixLineType.UP);
                            matrixLineList.add(matrixLine);
                            index = index + 5;
                            break;
                        }
                        if (kline.getLow() < min) {
                            break;
                        }
                        current++;
                    }
                }
            } else {
                int min = Math.min(kline2.getLow(), kline4.getLow());
                int max = kline1.getHigh();
                if (kline3.getHigh() <= kline1.getHigh() && kline5.getHigh() <= kline1.getHigh() && kline5.getHigh() >= kline2.getLow()) {
                    int current = index + 5;
                    while (current < peakKLines.size()) {
                        Kline kline = peakKLines.get(current).getCombineKline().getKline();
                        if (kline.getLow() <= min) {
                            int low = Math.max(kline2.getLow(), kline4.getLow());
                            int high = Math.min(kline3.getHigh(), kline5.getHigh());
                            MatrixLine matrixLine = new MatrixLine(low, high, begin, end, MatrixLineType.DOWN);
                            matrixLineList.add(matrixLine);
                            index = index + 5;
                            break;
                        }
                        if (kline.getHigh() > max) {
                            break;
                        }
                        current++;
                    }
                }
            }
        }
        return matrixLineList;
    }



    /*private Boolean isValidMatrix(int min, int max, int index, List<PeakKline> peakKlines) {

    }*/




    /*public List<MatrixLine> drawMatrix(List<PeakKline> peakLines) {

        List<MatrixLine> matrixLineList = new ArrayList<>();

        for (int i = 0; i < peakLines.size(); i++) {

            PeakKline beginPeakKline = peakLines.get(i);
            Kline beginKline = beginPeakKline.getCombineKline().getKline();

            if (beginKline.getDate() == 20190507) {
                System.out.println(20190507);
            }

            MatrixLine matrixLine;

            if  (i == 0) {
                matrixLine = new MatrixLine(Integer.MIN_VALUE, Integer.MAX_VALUE, beginKline.getDate(), i, beginPeakKline.getShapeType());
            } else {
                PeakKline prevPeakKline = peakLines.get(i - 1);
                Kline prevKline = prevPeakKline.getCombineKline().getKline();
                if (beginPeakKline.getShapeType() == LineShapeEnum.TOP) {
                    matrixLine = new MatrixLine(prevKline.getLow(), Integer.MAX_VALUE, beginKline.getDate(), i, beginPeakKline.getShapeType());
                } else {
                    matrixLine = new MatrixLine(Integer.MIN_VALUE, prevKline.getHigh(), beginKline.getDate(), i, beginPeakKline.getShapeType());
                }
            }

            for (int j = i + 1; j < peakLines.size(); j++) {

                boolean isMinLengthMatrix = isMinLengthMatrix(i, j);
                boolean isEndMatrix = isEndMatrix(peakLines, beginPeakKline, j);

                if (!isMinLengthMatrix && isEndMatrix) {
                    break;
                }

                if (isMinLengthMatrix && !matrixLine.isBreak()) {
                    matrixLine.setBreak(isBreak(beginPeakKline, peakLines.get(j), matrixLine));
                }

                updateMatrixLine(peakLines, matrixLine, j);

                if (isEndMatrix) {
                    if (isValidMatrix(matrixLine)) {
                        i = j + 1;
                    }
                    break;
                }

            }
            if (isValidMatrix(matrixLine)) {
                matrixLineList.add(matrixLine);
            }
        }

        return matrixLineList;

    }*/

    /*private Boolean isBreak(PeakKline beginPeakKline, PeakKline peakKline, MatrixLine matrixLine) {
        Kline kline = peakKline.getCombineKline().getKline();
        if (beginPeakKline.getShapeType() == LineShapeEnum.TOP) {
            return matrixLine.getLow() > kline.getLow();
        } else {
            return matrixLine.getHigh() < kline.getHigh();
        }
    }

    private void updateMatrixLine(List<PeakKline> peakLines, MatrixLine matrixLine, int index) {
        PeakKline prevPeakKline = peakLines.get(index - 1);
        PeakKline currentPeakKline = peakLines.get(index);
        Kline prevKline = prevPeakKline.getCombineKline().getKline();
        Kline currentKline = currentPeakKline.getCombineKline().getKline();
        if (currentPeakKline.getShapeType() == LineShapeEnum.TOP) {
            matrixLine.setHigh(Math.min(matrixLine.getHigh(), currentKline.getHigh()));
        } else {
            matrixLine.setLow(Math.max(matrixLine.getLow(), currentKline.getHigh()));
        }
        matrixLine.setRight(index);
        matrixLine.setEnd(currentKline.getDate());
    }

    private boolean isMinLengthMatrix(int begin, int end) {
        return end - begin >= 5;
    }

    private boolean isValidMatrix(MatrixLine matrixLine) {
        if (matrixLine.getRight() - matrixLine.getLeft() >= 5
                && matrixLine.isBreak()
                    && matrixLine.getLow() != Integer.MIN_VALUE
                        && matrixLine.getHigh() != Integer.MAX_VALUE) {
            return true;
        }
        return false;
    }

    private boolean isEndMatrix(List<PeakKline> peakKlines, PeakKline beginKline, int index) {

        if (index < peakKlines.size() - 3) {

            PeakKline prevPeakKline = peakKlines.get(index - 1);
            PeakKline currPeakKline = peakKlines.get(index);
            PeakKline afterPeakKline1 =  peakKlines.get(index + 1);
            PeakKline afterPeakKline2 =  peakKlines.get(index + 2);
            PeakKline afterPeakKline3 =  peakKlines.get(index + 3);

            Kline currKline = currPeakKline.getCombineKline().getKline();
            Kline prevKline = prevPeakKline.getCombineKline().getKline();
            Kline afterKline1 = afterPeakKline1.getCombineKline().getKline();
            Kline afterKline2 = afterPeakKline2.getCombineKline().getKline();
            Kline afterKline3 = afterPeakKline3.getCombineKline().getKline();

            if (beginKline.getShapeType() == LineShapeEnum.TOP) {
                if (currPeakKline.getShapeType() == LineShapeEnum.TOP) {
                    if (afterKline1.getLow() <= prevKline.getHigh()
                            && afterKline2.getHigh() < currKline.getHigh()
                            && afterKline3.getLow() <= afterKline1.getLow()) {
                        return true;
                    }
                }
            } else {
                if (currPeakKline.getShapeType() == LineShapeEnum.FLOOR) {
                    if (afterKline1.getHigh() >= prevKline.getLow()
                            && afterKline2.getLow() > currKline.getLow()
                            && afterKline3.getHigh() >= afterKline1.getHigh()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }*/

}
