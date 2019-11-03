package com.tdx.zq.draw;

import com.tdx.zq.enums.LineShapeEnum;
import com.tdx.zq.enums.MatrixLineType;
import com.tdx.zq.model.Kline;
import com.tdx.zq.model.MatrixLine;
import com.tdx.zq.model.PeakKline;
import com.tdx.zq.utils.JacksonUtils;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class MatrixLineService {

    public List<MatrixLine> drawMatrix(List<PeakKline> peakKLines) {

        //1.获取前置MatirxLine
        List<MatrixLine> prevMatrixLineList = getPrevMatrixs(peakKLines);
        System.out.println("prevMatrixLineList: " + JacksonUtils.toJson(prevMatrixLineList));

        //2.获取后缀MatirxLine
        List<MatrixLine> suffMatrixLineList = getSuffMatrixs(peakKLines, prevMatrixLineList);
        System.out.println("suffMatrixLineList: " + JacksonUtils.toJson(suffMatrixLineList));

        //3. 合并前置后置矩阵集合
        List<MatrixLine> combineMatrixLineList =  getCombineMatrixList(prevMatrixLineList, suffMatrixLineList);
        System.out.println("combineMatrixLineList: " + JacksonUtils.toJson(combineMatrixLineList));

        return null;

    }

    private List<MatrixLine> getCombineMatrixList(List<MatrixLine> prevMatrixLineList, List<MatrixLine> suffMatrixLineList) {
        List<MatrixLine> combineMatrixList = new ArrayList();
        combineMatrixList.addAll(prevMatrixLineList);
        combineMatrixList.addAll(suffMatrixLineList);
        Collections.sort(combineMatrixList, new Comparator<MatrixLine>() {
            @Override
            public int compare(MatrixLine o1, MatrixLine o2) {
                if (o1.getBegin() < o2.getBegin()) {
                    return -1;
                } else if (o1.getBegin() == o2.getBegin()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        return combineMatrixList;
    }

    private List<MatrixLine> getPrevMatrixs(List<PeakKline> peakKLines) {

        List<MatrixLine> matrixLineList = new ArrayList<>();

        for (int index = 1; index < peakKLines.size() - 4; index++) {

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

            if (kline1.getDate() == 20190201) {
                System.out.println(20190201);
            }
            if (kline2.getDate() == 20190201) {
                System.out.println(20190201);
            }
            if (kline3.getDate() == 20190201) {
                System.out.println(20190201);
            }
            if (kline4.getDate() == 20190201) {
                System.out.println(20190201);
            }
            if (kline5.getDate() == 20190201) {
                System.out.println(20190201);
            }

            int begin = kline2.getDate();
            int end = kline5.getDate();

            if (peak1.getShapeType().equals(LineShapeEnum.FLOOR)) {
                int min = kline1.getLow();
                int max = Math.max(kline2.getHigh(), kline4.getHigh());
                if (kline3.getLow() >= kline1.getLow() && kline5.getLow() >= kline1.getLow() && kline5.getLow() <= kline2.getHigh()) {
                    int current = index + 5;
                    while (current < peakKLines.size()) {
                        Kline kline = peakKLines.get(current).getCombineKline().getKline();
                        if (kline.getHigh() >= max) {
                            PeakKline prevPeakKline = peakKLines.get(index - 1);
                            PeakKline lastPeakKline = peak5;
                            PeakKline suffPeakKline = peakKLines.get(current);
                            int low = Math.max(kline3.getLow(), kline5.getLow());
                            int high = Math.min(kline2.getHigh(), kline4.getHigh());
                            MatrixLine matrixLine = new MatrixLine(low, high, begin, end, prevPeakKline, lastPeakKline, suffPeakKline, MatrixLineType.UP);
                            matrixLineList.add(matrixLine);
                            index = index + 4;
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
                if (kline3.getHigh() <= kline1.getHigh() && kline5.getHigh() <= kline1.getHigh() && kline5.getHigh() <= kline2.getLow()) {
                    int current = index + 5;
                    while (current < peakKLines.size()) {
                        Kline kline = peakKLines.get(current).getCombineKline().getKline();
                        if (kline.getLow() <= min) {
                            PeakKline prevPeakKline = peakKLines.get(index - 1);
                            PeakKline lastPeakKline = peak5;
                            PeakKline suffPeakKline = peakKLines.get(current);
                            int low = Math.max(kline2.getLow(), kline4.getLow());
                            int high = Math.min(kline3.getHigh(), kline5.getHigh());
                            MatrixLine matrixLine = new MatrixLine(low, high, begin, end, prevPeakKline, lastPeakKline, suffPeakKline, MatrixLineType.DOWN);
                            matrixLineList.add(matrixLine);
                            index = index + 4;
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


    private List<MatrixLine> getSuffMatrixs(List<PeakKline> peakKLines, List<MatrixLine> prevMatrixLineList) {

        List<MatrixLine> suffMatrixs = new ArrayList<>();

        for(MatrixLine matrixLine: prevMatrixLineList) {

            if (matrixLine.getSuffPeakKline().getPeakIndex() - matrixLine.getLastPeakKline().getPeakIndex() >= 5) {
                int index =  matrixLine.getLastPeakKline().getPeakIndex();
                PeakKline peak1 = peakKLines.get(index);
                PeakKline peak2 = peakKLines.get(index + 1);
                PeakKline peak3 = peakKLines.get(index + 2);
                PeakKline peak4 = peakKLines.get(index + 3);
                PeakKline peak5 = peakKLines.get(index + 4);

                Kline kline2 = peak2.getCombineKline().getKline();
                Kline kline3 = peak3.getCombineKline().getKline();
                Kline kline4 = peak4.getCombineKline().getKline();
                Kline kline5 = peak5.getCombineKline().getKline();

                int begin = kline2.getDate();
                int end = kline5.getDate();

                if (peak1.getShapeType().equals(LineShapeEnum.FLOOR)) {
                    int min = Math.max(kline3.getLow(), kline5.getLow());
                    int max = Math.min(kline2.getHigh(), kline4.getHigh());
                    suffMatrixs.add(new MatrixLine(min, max, begin, end, matrixLine.getLastPeakKline(), peak5, matrixLine.getSuffPeakKline(), matrixLine.getMatrixLineType()));
                } else {
                    int min = Math.max(kline2.getLow(), kline4.getLow());
                    int max = Math.min(kline3.getHigh(), kline5.getHigh());
                    suffMatrixs.add(new MatrixLine(min, max, begin, end, matrixLine.getLastPeakKline(), peak5, matrixLine.getSuffPeakKline(), matrixLine.getMatrixLineType()));
                }
            }
        }

        return suffMatrixs;
    }



}
