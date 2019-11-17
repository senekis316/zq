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

    public List<MatrixLine> drawMatrix(List<PeakKline> peakKLines) {

        //1.获取前置MatirxLine
        List<MatrixLine> prevMatrixLineList = getPrevMatrixs(peakKLines);
        System.out.println("prevMatrixLineList: " + JacksonUtils.toJson(prevMatrixLineList));

        //2.获取后缀MatirxLine
        //List<MatrixLine> suffMatrixLineList = getSuffMatrixs(peakKLines, prevMatrixLineList);
        //System.out.println("suffMatrixLineList: " + JacksonUtils.toJson(suffMatrixLineList));

        //3. 合并前置后置矩阵集合
        //List<MatrixLine> combineMatrixLineList =  getCombineMatrixList(prevMatrixLineList, suffMatrixLineList);
        //System.out.println("combineMatrixLineList: " + JacksonUtils.toJson(combineMatrixLineList));

        return null;

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

            int begin = kline2.getDate();
            int end = kline5.getDate();

            if (begin == 20180212) {
                System.out.println(20180212);
            }

            if (peak1.getShapeType().equals(LineShapeEnum.FLOOR)) {
                int min = kline1.getLow();
                int max = Math.max(kline2.getHigh(), kline4.getHigh());
                if (kline3.getLow() >= kline1.getLow() && kline5.getLow() >= kline1.getLow()) {
                    int current = index + 5;
                    while (current < peakKLines.size()) {
                        Kline kline = peakKLines.get(current).getCombineKline().getKline();
                        if (kline.getHigh() >= max) {
                            int low = Math.max(kline3.getLow(), kline5.getLow());
                            int high = Math.min(kline2.getHigh(), kline4.getHigh());
                            PeakKline beginPeakKline = peak5;
                            PeakKline endPeakKline = getMatrixsEndPeakKline(peakKLines, current, MatrixLineType.UP);
                            getEndPoint(beginPeakKline, endPeakKline, MatrixLineType.UP, peakKLines);
                            MatrixLine matrixLine = new MatrixLine(low, high, begin, end, MatrixLineType.UP, beginPeakKline, endPeakKline);
                            matrixLineList.add(matrixLine);
                            getFollowMatrixs(peakKLines, matrixLineList);
                            index = matrixLineList.get(matrixLineList.size() - 1).getEndPeakKline().getPeakIndex();
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
                if (kline3.getHigh() <= kline1.getHigh() && kline5.getHigh() <= kline1.getHigh()) {
                    int current = index + 5;
                    while (current < peakKLines.size()) {
                        Kline kline = peakKLines.get(current).getCombineKline().getKline();
                        if (kline.getLow() <= min) {
                            int low = Math.max(kline2.getLow(), kline4.getLow());
                            int high = Math.min(kline3.getHigh(), kline5.getHigh());
                            PeakKline beginPeakKline = peak5;
                            PeakKline endPeakKline = getMatrixsEndPeakKline(peakKLines, current, MatrixLineType.DOWN);
                            getEndPoint(beginPeakKline, endPeakKline, MatrixLineType.UP, peakKLines);
                            MatrixLine matrixLine = new MatrixLine(low, high, begin, end, MatrixLineType.DOWN, beginPeakKline, endPeakKline);
                            matrixLineList.add(matrixLine);
                            getFollowMatrixs(peakKLines, matrixLineList);
                            index = matrixLineList.get(matrixLineList.size() - 1).getEndPeakKline().getPeakIndex();
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

    private void getEndPoint(PeakKline beginPeakKline, PeakKline endPeakKline,  MatrixLineType matrixLineType, List<PeakKline> peakKLines) {

        if (endPeakKline.getPeakIndex() - beginPeakKline.getPeakIndex() >= 4) {

            int beginIndex = beginPeakKline.getPeakIndex();
            int endIndex = endPeakKline.getPeakIndex();

            for (int index = beginIndex; index <= endIndex - 4; index++) {

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

                if (matrixLineType != matrixLineType.UP) {
                    int min = Math.min(kline2.getLow(), kline4.getLow());
                    int max = kline1.getHigh();
                    if (kline3.getHigh() <= kline1.getHigh() && kline5.getHigh() <= kline1.getHigh()) {
                        int current = index + 5;
                        while (current < peakKLines.size()) {
                            Kline kline = peakKLines.get(current).getCombineKline().getKline();
                            if (kline.getLow() <= min) {
                                if (peak5.getPeakIndex() < endPeakKline.getPeakIndex()) {
                                    endPeakKline = peak1;
                                }
                                break;
                            }
                            if (kline.getHigh() > max) {
                                break;
                            }
                            current++;
                        }
                    }
                } else {
                    int min = kline1.getLow();
                    int max = Math.max(kline2.getHigh(), kline4.getHigh());
                    if (kline3.getLow() >= kline1.getLow() && kline5.getLow() >= kline1.getLow()) {
                        int current = index + 5;
                        while (current < peakKLines.size()) {
                            Kline kline = peakKLines.get(current).getCombineKline().getKline();
                            if (kline.getHigh() >= max) {
                                if (peak5.getPeakIndex() < endPeakKline.getPeakIndex()) {
                                    endPeakKline = peak1;
                                }
                                break;
                            }
                            if (kline.getLow() < min) {
                                break;
                            }
                            current++;
                        }
                    }
                }

            }
        }
    }

    private void getFollowMatrixs(List<PeakKline> peakKLines, List<MatrixLine> matrixLineList) {

        MatrixLine matrixLine = matrixLineList.get(matrixLineList.size() - 1);

        PeakKline beginPeakKline = matrixLine.getBeginPeakKline();
        PeakKline endPeakKline = matrixLine.getEndPeakKline();

        if (matrixLine.getBegin() == 20190402) {
            System.out.println(20190402);
        }

        if (endPeakKline.getPeakIndex() - beginPeakKline.getPeakIndex() >= 4) {

            int beginIndex = beginPeakKline.getPeakIndex();
            int endIndex = endPeakKline.getPeakIndex();

            for (int index = beginIndex; index <= endIndex - 4; index++) {

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

                MatrixLineType matrixLineType = matrixLineList.get(matrixLineList.size() - 1).getMatrixLineType();

                int low = 0;
                int high = 0;

                if (matrixLineType == MatrixLineType.UP) {
                    low = Math.max(kline3.getLow(), kline5.getLow());
                    high = Math.min(kline2.getHigh(), kline4.getHigh());
                } else {
                   low = Math.max(kline2.getLow(), kline4.getLow());
                   high = Math.min(kline3.getHigh(), kline5.getHigh());
                }

                MatrixLine followMatrixLine = new MatrixLine(low, high, begin, end, matrixLineType, peak5, endPeakKline);
                matrixLineList.add(followMatrixLine);
                index += 4;

            }
            return;
        }
        return;
    }

    private PeakKline getMatrixsEndPeakKline(List<PeakKline> peakKLines, int pointer, MatrixLineType matrixLineType) {

        Kline tempKline1 = null;
        Kline tempKline2 = null;

        while (pointer + 3 < peakKLines.size()){

            PeakKline prevPeak2 = peakKLines.get(pointer - 2);
            PeakKline currPeak = peakKLines.get(pointer);
            PeakKline nextPeak1 = peakKLines.get(pointer + 1);
            PeakKline nextPeak2 = peakKLines.get(pointer + 2);
            PeakKline nextPeak3 = peakKLines.get(pointer + 3);

            Kline prevKline2 = prevPeak2.getCombineKline().getKline();
            Kline currKline = currPeak.getCombineKline().getKline();
            Kline nextKline1 = nextPeak1.getCombineKline().getKline();
            Kline nextKline2 = nextPeak2.getCombineKline().getKline();
            Kline nextKline3 = nextPeak3.getCombineKline().getKline();

            boolean isBreak = false;

            if (matrixLineType == MatrixLineType.UP) {
                if (currKline.getHigh() >= prevKline2.getHigh()) {
                    isBreak = true;
                }
            } else {
                if (currKline.getLow() <= prevKline2.getLow()) {
                    isBreak = true;
                }
            }

            if (isBreak) {
                if (matrixLineType == MatrixLineType.UP) {
                    if (currKline.getHigh() > prevKline2.getHigh()
                            && nextKline1.getLow() <= prevKline2.getHigh()
                            && nextKline2.getHigh() < currKline.getHigh()
                            && nextKline3.getLow() < nextKline1.getLow()) {
                        return currPeak;
                    }
                } else {
                    if (currKline.getLow() < prevKline2.getLow()
                            && nextKline1.getHigh() >= prevKline2.getLow()
                            && nextKline2.getLow() > currKline.getLow()
                            && nextKline3.getHigh() > nextKline1.getHigh()) {
                        return currPeak;
                    }
                }
                tempKline1 = nextKline1;
                tempKline2 = currKline;
                pointer += 3;
            } else {
                 if (matrixLineType == MatrixLineType.UP){
                    if (currKline.getHigh() < tempKline2.getHigh()
                            && nextKline1.getLow() < tempKline1.getLow()) {
                        return prevPeak2;
                    }
                 } else {
                    if (currKline.getLow() > tempKline2.getLow()
                            && nextKline1.getHigh() > tempKline1.getHigh()) {
                        return prevPeak2;
                    }
                 }
                 pointer += 2;
            }

        }

        return peakKLines.get(peakKLines.size() - 1);

        /*boolean isBreak = true;

        while (pointer + 3 < peakKLines.size()){

            if(isBreak) {

                PeakKline prevPeak2 = peakKLines.get(pointer - 2);
                PeakKline currPeak = peakKLines.get(pointer);
                PeakKline nextPeak1 = peakKLines.get(pointer + 1);
                PeakKline nextPeak2 = peakKLines.get(pointer + 2);
                PeakKline nextPeak3 = peakKLines.get(pointer + 3);

                Kline prevKline2 = prevPeak2.getCombineKline().getKline();
                Kline currKline = currPeak.getCombineKline().getKline();
                Kline nextKline1 = nextPeak1.getCombineKline().getKline();
                Kline nextKline2 = nextPeak2.getCombineKline().getKline();
                Kline nextKline3 = nextPeak3.getCombineKline().getKline();

                if (matrixLineType == MatrixLineType.UP) {
                    if (currKline.getHigh() > prevKline2.getHigh()
                            && nextKline1.getLow() <= prevKline2.getHigh()
                            && nextKline2.getHigh() < currKline.getHigh()
                            && nextKline3.getLow() < nextKline1.getLow()) {
                        return currPeak;
                    }
                } else {
                    if (currKline.getLow() < prevKline2.getLow()
                            && nextKline1.getHigh() >= prevKline2.getLow()
                            && nextKline2.getLow() > currKline.getLow()
                            && nextKline3.getHigh() > nextKline1.getHigh()) {
                        return currPeak;
                    }
                }
                isBreak = false;
            } else {
                PeakKline prevPeak1 = peakKLines.get(pointer - 1);
                PeakKline prevPeak2 = peakKLines.get(pointer - 2);
                PeakKline currPeak = peakKLines.get(pointer);
                PeakKline nextPeak1 = peakKLines.get(pointer + 1);

                Kline prevKline1 = prevPeak1.getCombineKline().getKline();
                Kline prevKline2 = prevPeak2.getCombineKline().getKline();
                Kline currKline = currPeak.getCombineKline().getKline();
                Kline nextKline1 = nextPeak1.getCombineKline().getKline();

                if (matrixLineType == MatrixLineType.DOWN) {
                    if (currKline.getLow() > prevKline2.getLow()
                            && nextKline1.getHigh() > prevKline1.getHigh()) {
                        return prevPeak2;
                    }
                } else {
                    if (currKline.getHigh() < prevKline2.getHigh()
                            && nextKline1.getLow() < prevKline1.getLow()) {
                        return prevPeak2;
                    }

                }

            }

            pointer++;
            pointer++;
        }

        return peakKLines.get(peakKLines.size() - 1);*/

    }


    /*private List<MatrixLine> getFollowMatrixs(List<PeakKline> peakKLines, List<MatrixLine> prevMatrixLineList) {

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
    }*/


    /*private boolean isUpMatrix(List<MatrixLine> matrixLines, PeakKline currPeakKline) {
        return getMatrixLineType(matrixLines, currPeakKline) == MatrixLineType.UP;
    }

    private MatrixLineType getMatrixLineType(List<MatrixLine> matrixLines, PeakKline currPeakKline) {

        if (!CollectionUtils.isEmpty(matrixLines)) {
            MatrixLine prevMatrixLine = matrixLines.get(matrixLines.size() - 1);
            Kline lastKline = prevMatrixLine.getEndPeakKline().getCombineKline().getKline();
            Kline currKline = currPeakKline.getCombineKline().getKline();
            if (lastKline.getDate() >= currKline.getDate()) {
                return prevMatrixLine.getMatrixLineType();
            }
        }

        return currPeakKline.getShapeType() == LineShapeEnum.FLOOR ? MatrixLineType.UP : MatrixLineType.DOWN;

    }

    private boolean isFollowMatrix(List<MatrixLine> matrixLines, PeakKline currPeakKline) {

        if (!CollectionUtils.isEmpty(matrixLines)) {
            MatrixLine prevMatrixLine = matrixLines.get(matrixLines.size() - 1);
            Kline lastKline = prevMatrixLine.getEndPeakKline().getCombineKline().getKline();
            Kline currKline = currPeakKline.getCombineKline().getKline();
            if (lastKline.getDate() >= currKline.getDate()) {
                return true;
            }
        }

        return false;

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
    }*/

}
