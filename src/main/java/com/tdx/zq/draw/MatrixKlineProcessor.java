package com.tdx.zq.draw;

import java.util.ArrayList;
import java.util.List;

import com.tdx.zq.draw.PeakKlineProcessor.MatrixKlineRow;
import com.tdx.zq.enums.PeakShapeEnum;
import lombok.Data;

public class MatrixKlineProcessor {

    private List<MatrixKlineRow> matrixKlineRowList;
    private List<MatrixKlineRow> upMatrixKlineRowList;
    private List<MatrixKlineRow> downMatrixKlineRowList;
    private List<List<MatrixKlineRow>> upMatrixList;
    private List<List<MatrixKlineRow>> downMatrixList;

    public MatrixKlineProcessor(List<MatrixKlineRow> matrixKlineRowList) {
        this.matrixKlineRowList = matrixKlineRowList;
        this.upMatrixList = new ArrayList<>();
        this.downMatrixList = new ArrayList<>();
        setMatrixTendency();
    }

    public class MatrixSegment {

        private long upper;
        private long lower;
        private long startDate;
        private long endDate;
        private PeakShapeEnum shape;

        public MatrixSegment(MatrixKlineRow row1, MatrixKlineRow row2) {
            if (row1.getShape() == PeakShapeEnum.FLOOR) {
                lower = row1.getLow();
                upper = row2.getHigh();
            } else {
                upper = row1.getHigh();
                lower = row2.getLow();
            }
            shape = row2.getShape();
            startDate = row1.getDate();
            endDate = row2.getDate();
        }
    }

    public class MatrixContext {

        private int range;
        private long upper;
        private long lower;
        private long startDate;

        public MatrixContext(MatrixSegment matrixSegment) {
            this.range = 1;
            this.lower = matrixSegment.lower;
            this.upper = matrixSegment.upper;
            this.startDate = matrixSegment.startDate;
        }

    }

    public void setMatrixTendency() {

        if (matrixKlineRowList.size() < 3) {
            return;
        }

        // 1.每两根K线组成一个矩阵区间
        List<MatrixSegment> matrixSegmentList = new ArrayList<>();
        for (int i = 1; i < matrixKlineRowList.size(); i++) {
            MatrixKlineRow prev = matrixKlineRowList.get(i - 1);
            MatrixKlineRow curr = matrixKlineRowList.get(i);
            matrixSegmentList.add(new MatrixSegment(prev, curr));
        }

        MatrixContext lowerContext = null;
        MatrixContext upperContext = null;
        PeakShapeEnum tendency = PeakShapeEnum.NONE;
        for (int i = 0; i < matrixSegmentList.size(); i++) {
            MatrixSegment curr = matrixSegmentList.get(i);
            MatrixSegment next = null;
            if (i != matrixSegmentList.size() - 1) {
                next = matrixSegmentList.get(i + 1);
            }
            if (curr.shape == PeakShapeEnum.FLOOR) {
                if (lowerContext == null) {
                    lowerContext = new MatrixContext(curr);
                } else {
                    if (curr.lower < lowerContext.lower) {
                        if (next != null && upperContext != null
                                && next.upper > upperContext.upper && tendency == PeakShapeEnum.TOP) {
                            continue;
                        }
                        if (tendency == PeakShapeEnum.TOP && upperContext != null) {
                            System.out.println("up  : " + upperContext.startDate + " - " + lowerContext.startDate);
                        }
                        upperContext = null;
                        lowerContext.lower = curr.lower;
                        tendency = PeakShapeEnum.FLOOR;
                    }
                }
            } else {
                if (upperContext == null) {
                    upperContext = new MatrixContext(curr);
                } else {
                    if (curr.upper > upperContext.upper) {
                        if (next != null && lowerContext != null
                                && next.lower < lowerContext.lower && tendency == PeakShapeEnum.FLOOR) {
                            continue;
                        }
                        if (tendency == PeakShapeEnum.FLOOR && lowerContext != null) {
                            System.out.println("down: " + lowerContext.startDate + " - " + upperContext.startDate);
                        }
                        lowerContext = null;
                        upperContext.upper = curr.upper;
                        tendency = PeakShapeEnum.TOP;
                    }
                }
            }

            if (i == matrixSegmentList.size() - 1 && tendency != null) {
                if (tendency == PeakShapeEnum.FLOOR) {
                    System.out.println("down: " + lowerContext.startDate + " - " + matrixSegmentList.get(matrixSegmentList.size() - 1).endDate);
                } else {
                    System.out.println("up:   " + upperContext.startDate + " - " + matrixSegmentList.get(matrixSegmentList.size() - 1).endDate);

                }
            }
        }

    }


//    // 对上升区间进行处理
//    public void upperSegment(List<MatrixSegment> upperSegmentList, List<MatrixSegment> matrixSegmentList) {
//        long lower = upperSegmentList.get(0).getLower();
//        long upper = upperSegmentList.get(0).getUpper();
//        long startDate = upperSegmentList.get(0).getStartDate();
//        int range = 1;
//        PeakShapeEnum tendency = PeakShapeEnum.NONE;
//        for (int i = 1; i < upperSegmentList.size(); i++) {
//            MatrixSegment prev = upperSegmentList.get(i - 1);
//            MatrixSegment curr = upperSegmentList.get(i);
//            if (curr.lower < lower) {
//                lower = curr.lower;
//                upper = curr.upper;
//                if (range > 1 && tendency == PeakShapeEnum.TOP) {
//                    System.out.println("up: " + startDate + " - " + prev.endDate);
//                    tendency = PeakShapeEnum.NONE;
//                }
//                startDate = curr.getStartDate();
//                range = 1;
//            } else if (curr.upper > upper){
//                upper = curr.upper;
//                range++;
//                tendency = PeakShapeEnum.TOP;
//            } else {
//                range++;
//                if (i == upperSegmentList.size() - 1 && tendency == PeakShapeEnum.TOP) {
//                    System.out.println("up: " + startDate + " - " + matrixSegmentList.get(matrixSegmentList.size() - 1).getEndDate());
//                }
//            }
//        }
//    }
//
//    // 对下降区间进行处理
//    public void lowerSegment(List<MatrixSegment> lowerSegmentList, List<MatrixSegment> matrixSegmentList) {
//        long lower = lowerSegmentList.get(0).getLower();
//        long upper = lowerSegmentList.get(0).getUpper();
//        long startDate = lowerSegmentList.get(0).getStartDate();
//        int range = 1;
//        PeakShapeEnum tendency = PeakShapeEnum.NONE;
//        for (int i = 1; i < lowerSegmentList.size(); i++) {
//            MatrixSegment prev = lowerSegmentList.get(i - 1);
//            MatrixSegment curr = lowerSegmentList.get(i);
//            if (curr.upper > upper) {
//                lower = curr.lower;
//                upper = curr.upper;
//                if (range > 1 && tendency == PeakShapeEnum.FLOOR) {
//                    System.out.println("down: " + startDate + " - " + prev.endDate);
//                    tendency = PeakShapeEnum.NONE;
//                }
//                startDate = curr.getStartDate();
//                range = 1;
//            } else if (curr.lower < lower){
//                lower = curr.lower;
//                range++;
//                tendency = PeakShapeEnum.FLOOR;
//            } else {
//                range++;
//                if (i == lowerSegmentList.size() - 1 && tendency == PeakShapeEnum.FLOOR) {
//                    System.out.println("down: " + startDate + " - " + matrixSegmentList.get(matrixSegmentList.size() - 1).getEndDate());
//                }
//            }
//        }
//    }


    /*public void setMatrixTendency() {
        for (int i = 0; i <= matrixKlineRowList.size() - 6; i++) {
            List<MatrixKlineRow> matrixKlineRows = new ArrayList<>();
            MatrixKlineRow r1 = matrixKlineRowList.get(i);
            MatrixKlineRow r2 = matrixKlineRowList.get(i + 1);
            MatrixKlineRow r3 = matrixKlineRowList.get(i + 2);
            MatrixKlineRow r4 = matrixKlineRowList.get(i + 3);
            MatrixKlineRow r5 = matrixKlineRowList.get(i + 4);
            MatrixKlineRow r6 = matrixKlineRowList.get(i + 5);
            if (matrixKlineRowList.get(i).getShape() == PeakShapeEnum.FLOOR) {
                if (upMatrixList.size() == 0) {
                    if (r1.getLow() <= r3.getLow()) {
                        matrixKlineRows.add(r1);
                        matrixKlineRows.add(r2);
                        matrixKlineRows.add(r3);
                        matrixKlineRows.add(r4);
                        upMatrixList.add(matrixKlineRows);
                    }
                } else {
                    List<MatrixKlineRow> lastUpMatrixList = upMatrixList.get(upMatrixList.size() - 1);
                    if (lastUpMatrixList.get(lastUpMatrixList.size() - 1) == r2) {
                        int max = - 1;
                        boolean up = false;
                        for (int j = lastUpMatrixList.size() - 1; j > 1; j -= 2) {
                            if (!up && lastUpMatrixList.get(j).getHigh() >= lastUpMatrixList.get(1).getHigh()) {
                                up = true;
                            }
                            if (max == -1) {
                                max = j;
                            } else {
                                max = lastUpMatrixList.get(max).getHigh() > lastUpMatrixList.get(j).getHigh() ? max : j;
                            }
                        }
                        if (up) {
                            if (max == lastUpMatrixList.size() - 1) {
                                if (r5.getLow() < r3.getLow()
                                        && r6.getHigh() <= r2.getHigh()
                                        && r4.getHigh() <= lastUpMatrixList.get(max).getHigh()) {
                                    for (int z = lastUpMatrixList.size() - 1; z > max; z--) {
                                        lastUpMatrixList.remove(z);
                                    }
                                } else if (r5.getLow() > r3.getLow()
                                            && r4.getHigh() <= r2.getHigh()
                                            && r6.getHigh() <= r2.getHigh()) {
                                    int j = i + 6;
                                    while (j < matrixKlineRowList.size()) {
                                        MatrixKlineRow r7 = matrixKlineRowList.get(j);
                                        if (r7.getShape() == PeakShapeEnum.FLOOR) {
                                            if (r3.getLow() > r7.getLow()) {
                                                for (int z = lastUpMatrixList.size() - 1; z > max; z--) {
                                                    lastUpMatrixList.remove(z);
                                                }
                                                break;
                                            }
                                        } else {
                                            if (r7.getHigh() > r2.getHigh()) {
                                                lastUpMatrixList.add(r3);
                                                lastUpMatrixList.add(r4);
                                                break;
                                            }
                                        }
                                        j++;
                                    }
                                } else {
                                    lastUpMatrixList.add(r3);
                                    lastUpMatrixList.add(r4);
                                }
                            } else {
                                if (r5.getLow() < lastUpMatrixList.get(max + 1).getLow()
                                        && r4.getHigh() <= lastUpMatrixList.get(max).getLow()
                                        && r6.getHigh() <= lastUpMatrixList.get(max).getHigh()) {
                                    for (int z = lastUpMatrixList.size() - 1; z > max; z--) {
                                        lastUpMatrixList.remove(z);
                                    }
                                } else {
                                    lastUpMatrixList.add(r3);
                                    lastUpMatrixList.add(r4);
                                }
                            }
                        } else {
                            if (r3.getLow() >= lastUpMatrixList.get(0).getLow()) {
                                lastUpMatrixList.add(r3);
                                lastUpMatrixList.add(r4);
                            } else {
                                upMatrixList.remove(upMatrixList.size() - 1);
                            }
                        }
                    } else {
                        if (r1.getLow() <= r3.getLow()) {
                            matrixKlineRows.add(r1);
                            matrixKlineRows.add(r2);
                            matrixKlineRows.add(r3);
                            matrixKlineRows.add(r4);
                            upMatrixList.add(matrixKlineRows);
                        }
                    }
                }
                if (i == matrixKlineRowList.size() - 7) {
                    MatrixKlineRow r7 = matrixKlineRowList.get(i + 6);
                    List<MatrixKlineRow> lastUpMatrixList = upMatrixList.get(upMatrixList.size() - 1);
                    if (r6.getHigh() > r4.getHigh()
                        && r6.getHigh() > r2.getHigh()
                        && r1.getLow() < r3.getLow()) {
                        //&& r1.getLow() < r5.getLow()) {
                        lastUpMatrixList.add(r5);
                        lastUpMatrixList.add(r6);
                    } else if (lastUpMatrixList.get(lastUpMatrixList.size() - 1).getDate() < r3.getDate()
                            && r3.getLow() < r5.getLow()
                            && r4.getHigh() < r6.getHigh()) {
                        List<MatrixKlineRow> upMatrixKlineRows = new ArrayList<>();
                        upMatrixKlineRows.add(r3);
                        upMatrixKlineRows.add(r4);
                        upMatrixKlineRows.add(r5);
                        upMatrixKlineRows.add(r6);
                        upMatrixList.add(upMatrixKlineRows);
                    } else if (r4.getHigh() > r6.getHigh() && r5.getLow() > r7.getLow()) {
                        List<MatrixKlineRow> lastDownMatrixList = downMatrixList.get(downMatrixList.size() - 1);
                        MatrixKlineRow lastDownRow = lastDownMatrixList.get(lastDownMatrixList.size() - 1);
                        if (r5.getLow() > lastDownRow.getLow()
                                && r3.getDate() > lastDownRow.getDate()) {
                            List<MatrixKlineRow> downMatrixKlineRows = new ArrayList<>();
                            downMatrixKlineRows.add(r4);
                            downMatrixKlineRows.add(r5);
                            downMatrixKlineRows.add(r6);
                            downMatrixKlineRows.add(r7);
                            downMatrixList.add(downMatrixKlineRows);
                        }
                    }
                }
            } else {
                if (downMatrixList.size() == 0) {
                    if (r1.getHigh() >= r3.getHigh()) {
                        matrixKlineRows.add(r1);
                        matrixKlineRows.add(r2);
                        matrixKlineRows.add(r3);
                        matrixKlineRows.add(r4);
                        downMatrixList.add(matrixKlineRows);
                    }
                } else {
                    List<MatrixKlineRow> lastDownMatrixList = downMatrixList.get(downMatrixList.size() - 1);
                    if (lastDownMatrixList.get(lastDownMatrixList.size() - 1) == r2) {
                        int min = - 1;
                        boolean down = false;
                        for (int j = lastDownMatrixList.size() - 1; j > 1; j -= 2) {
                            if (!down && lastDownMatrixList.get(j).getLow() <= lastDownMatrixList.get(1).getLow()) {
                                down = true;
                            }
                            if (min == -1) {
                                min = j;
                            } else {
                                min = lastDownMatrixList.get(min).getLow() < lastDownMatrixList.get(j).getLow() ? min : j;
                            }
                        }
                        if (down) {
                            if (min == lastDownMatrixList.size() - 1) {
                                if (r5.getHigh() > r3.getHigh()
                                        && r6.getLow() >= r2.getLow()
                                        && r4.getLow() >= lastDownMatrixList.get(min).getLow()) {
                                    for (int z = lastDownMatrixList.size() - 1; z > min; z--) {
                                        lastDownMatrixList.remove(z);
                                    }
                                } else if (r5.getHigh() < r3.getHigh()
                                        && r4.getLow() >= r2.getLow()
                                        && r6.getLow() >= r2.getLow()) {
                                    int j = i + 6;
                                    while (j < matrixKlineRowList.size()) {
                                        MatrixKlineRow r7 = matrixKlineRowList.get(j);
                                        if (r7.getShape() == PeakShapeEnum.TOP) {
                                            if (r3.getHigh() < r7.getHigh()) {
                                                for (int z = lastDownMatrixList.size() - 1; z > min; z--) {
                                                    lastDownMatrixList.remove(z);
                                                }
                                                break;
                                            }
                                        } else {
                                            if (r7.getLow() < r2.getLow()) {
                                                lastDownMatrixList.add(r3);
                                                lastDownMatrixList.add(r4);
                                                break;
                                            }
                                        }
                                        j++;
                                    }
                                } else {
                                    lastDownMatrixList.add(r3);
                                    lastDownMatrixList.add(r4);
                                }
                            } else {
                                if (r5.getHigh() > lastDownMatrixList.get(min + 1).getHigh()
                                        && r4.getLow() >= lastDownMatrixList.get(min).getLow()
                                        && r6.getLow() >= lastDownMatrixList.get(min).getLow()) {
                                    for (int z = lastDownMatrixList.size() - 1; z > min; z--) {
                                        lastDownMatrixList.remove(z);
                                    }
                                } else {
                                    lastDownMatrixList.add(r3);
                                    lastDownMatrixList.add(r4);
                                }
                            }
                        } else {
                            if (r3.getHigh() <= lastDownMatrixList.get(0).getHigh()) {
                                lastDownMatrixList.add(r3);
                                lastDownMatrixList.add(r4);
                            } else {
                                downMatrixList.remove(downMatrixList.size() - 1);
                            }
                        }
                    } else {
                        if (r1.getHigh() >= r3.getHigh()) {
                            matrixKlineRows.add(r1);
                            matrixKlineRows.add(r2);
                            matrixKlineRows.add(r3);
                            matrixKlineRows.add(r4);
                            downMatrixList.add(matrixKlineRows);
                        }
                    }
                }
                if (i == matrixKlineRowList.size() - 7) {
                    MatrixKlineRow r7 = matrixKlineRowList.get(i + 6);
                    List<MatrixKlineRow> lastDownMatrixList = downMatrixList.get(downMatrixList.size() - 1);
                    if (r6.getLow() < r4.getLow()
                            && r6.getLow() < r2.getLow()
                            && r1.getHigh() > r3.getHigh()) {
                            //&& r1.getHigh() > r5.getHigh()) {
                        lastDownMatrixList.add(r5);
                        lastDownMatrixList.add(r6);
                    } else if (lastDownMatrixList.get(lastDownMatrixList.size() - 1).getDate() < r3.getDate()
                            && r3.getHigh() > r5.getHigh()
                            && r4.getLow() > r6.getLow()) {
                        List<MatrixKlineRow> downMatrixKlineRows = new ArrayList<>();
                        downMatrixKlineRows.add(r3);
                        downMatrixKlineRows.add(r4);
                        downMatrixKlineRows.add(r5);
                        downMatrixKlineRows.add(r6);
                        downMatrixList.add(downMatrixKlineRows);
                    } else if (r4.getLow() < r6.getLow() && r5.getHigh() < r7.getHigh()) {
                        List<MatrixKlineRow> lastUpMatrixList = upMatrixList.get(upMatrixList.size() - 1);
                        MatrixKlineRow lastUpRow = lastUpMatrixList.get(lastUpMatrixList.size() - 1);
                        if (r5.getHigh() < lastUpRow.getHigh()
                            && r3.getDate() > lastUpRow.getDate()) {
                            List<MatrixKlineRow> upMatrixKlineRows = new ArrayList<>();
                            upMatrixKlineRows.add(r4);
                            upMatrixKlineRows.add(r5);
                            upMatrixKlineRows.add(r6);
                            upMatrixKlineRows.add(r7);
                            upMatrixList.add(upMatrixKlineRows);
                        }
                    }
                }
            }
        }

        if (upMatrixList.size() > 0) {
            List<MatrixKlineRow> lastUpMatrixList = upMatrixList.get(upMatrixList.size() - 1);
            List<MatrixKlineRow> lastDownMatrixList = downMatrixList.get(downMatrixList.size() - 1);
            MatrixKlineRow lastUpMatrixRow = lastUpMatrixList.get(lastUpMatrixList.size() - 1);
            MatrixKlineRow lastDownMatrixRow = lastDownMatrixList.get(lastDownMatrixList.size() - 1);

            if (lastDownMatrixRow.getDate() <= matrixKlineRowList.get(matrixKlineRowList.size() - 2).getDate()
                    && lastUpMatrixRow.getDate() == matrixKlineRowList.get(matrixKlineRowList.size() - 3).getDate()
                    && lastUpMatrixRow.getHigh() < matrixKlineRowList.get(matrixKlineRowList.size() - 1).getHigh()
                    && lastUpMatrixList.get(lastUpMatrixList.size() - 1).getHigh() < matrixKlineRowList.get(matrixKlineRowList.size() - 1).getHigh()) {
                lastUpMatrixList.add(matrixKlineRowList.get(matrixKlineRowList.size() - 2));
                lastUpMatrixList.add(matrixKlineRowList.get(matrixKlineRowList.size() - 1));
            }
        }

        if (downMatrixList.size() > 0) {
            List<MatrixKlineRow> lastUpMatrixList = upMatrixList.get(upMatrixList.size() - 1);
            List<MatrixKlineRow> lastDownMatrixList = downMatrixList.get(downMatrixList.size() - 1);
            MatrixKlineRow lastUpMatrixRow = lastUpMatrixList.get(lastUpMatrixList.size() - 1);
            MatrixKlineRow lastDownMatrixRow = lastDownMatrixList.get(lastDownMatrixList.size() - 1);

            if (lastUpMatrixRow.getDate() <= matrixKlineRowList.get(matrixKlineRowList.size() - 2).getDate()
                    && lastDownMatrixRow.getDate() == matrixKlineRowList.get(matrixKlineRowList.size() - 3).getDate()
                    && lastDownMatrixRow.getLow() > matrixKlineRowList.get(matrixKlineRowList.size() - 1).getLow()
                    && lastDownMatrixList.get(lastDownMatrixList.size() - 1).getLow() > matrixKlineRowList.get(matrixKlineRowList.size() - 1).getLow()) {
                lastDownMatrixList.add(matrixKlineRowList.get(matrixKlineRowList.size() - 2));
                lastDownMatrixList.add(matrixKlineRowList.get(matrixKlineRowList.size() - 1));
            }
        }

        while (true) {
            if (upMatrixList.size() != 0 && downMatrixList.size() != 0) {
                List<MatrixKlineRow> lastUpMatrixList = upMatrixList.get(upMatrixList.size() - 1);
                List<MatrixKlineRow> lastDownMatrixList = downMatrixList.get(downMatrixList.size() - 1);
                MatrixKlineRow lastUpRow = lastUpMatrixList.get(lastUpMatrixList.size() - 1);
                MatrixKlineRow lastDownRow = lastDownMatrixList.get(lastDownMatrixList.size() - 1);
                if (lastUpRow.getDate() > lastDownRow.getDate()) {
                    if (lastUpMatrixList.size() == 4 && lastUpMatrixList.get(1).getHigh() > lastUpMatrixList.get(3).getHigh()) {
                        upMatrixList.remove(upMatrixList.size() - 1);
                        continue;
                    }
                } else {
                    if (lastDownMatrixList.size() == 4 && lastDownMatrixList.get(1).getLow() < lastDownMatrixList.get(3).getLow()) {
                        downMatrixList.remove(downMatrixList.size() - 1);
                        continue;
                    }
                }
            }
            break;
        }

        upMatrixList.stream().forEach(m -> System.out.println("up:" + m.get(0).getDate() + "_" + m.get(m.size() - 1).getDate()));
        System.out.println("---------------------------------------");
        System.out.println("---------------------------------------");
        System.out.println("---------------------------------------");
        downMatrixList.stream().forEach(m -> System.out.println("down:" + m.get(0).getDate() + "_" + m.get(m.size() - 1).getDate()));
        System.out.println("---------------------------------------");
        System.out.println("---------------------------------------");
        System.out.println("---------------------------------------");


        if (upMatrixList.size() == 0 || downMatrixList.size() == 0) {
            if (upMatrixList.size() > 0) {
                System.out.println("up:     " + upMatrixList.get(0).get(0).getDate() + "_" + upMatrixList.get(0).get(upMatrixList.get(0).size() - 1).getDate());
            }
            if (downMatrixList.size() > 0) {
                System.out.println("down:     " + downMatrixList.get(0).get(0).getDate() + "_" + downMatrixList.get(0).get(downMatrixList.get(0).size() - 1).getDate());
            }
            return;
        }

        long targetDate = upMatrixList.get(0).get(0).getDate() < downMatrixList.get(0).get(0).getDate()
                ? upMatrixList.get(0).get(0).getDate() : downMatrixList.get(0).get(0).getDate();
        boolean searchUp = upMatrixList.get(0).get(0).getDate() < downMatrixList.get(0).get(0).getDate() ? true : false;

        if (searchUp) {
            if (upMatrixList.get(0).get(upMatrixList.get(0).size() - 1).getDate()
                    >= downMatrixList.get(0).get(downMatrixList.get(0).size() - 1).getDate()
                    && upMatrixList.get(0).get(0).getDate() == matrixKlineRowList.get(0).getDate()
                    && upMatrixList.get(0).get(3).getDate() > downMatrixList.get(0).get(0).getDate()) {
                for (int i = 0; i < matrixKlineRowList.size() - 3; i++) {
                    if (matrixKlineRowList.get(i).getDate() == downMatrixList.get(0).get(downMatrixList.get(0).size() - 1).getDate()
                        && matrixKlineRowList.get(i + 1).getHigh() <= upMatrixList.get(0).get(1).getHigh()
                            && matrixKlineRowList.get(i + 3).getHigh() <= upMatrixList.get(0).get(1).getHigh()) {
                        new MatrixKlineProcessor(matrixKlineRowList.stream().filter(row -> row.getDate() >= downMatrixList.get(0).get(0).getDate()).collect(Collectors.toList()));
                        return;
                    }
                }
            }
        } else {
            if (downMatrixList.get(0).get(downMatrixList.get(0).size() - 1).getDate()
                    >= upMatrixList.get(0).get(upMatrixList.get(0).size() - 1).getDate()
                    && downMatrixList.get(0).get(0).getDate() == matrixKlineRowList.get(0).getDate()
                    && downMatrixList.get(0).get(3).getDate() > upMatrixList.get(0).get(0).getDate()) {
                for (int i = 0; i < matrixKlineRowList.size() - 3; i++) {
                    if (matrixKlineRowList.get(i).getDate() == upMatrixList.get(0).get(upMatrixList.get(0).size() - 1).getDate()
                            && matrixKlineRowList.get(i + 1).getLow() >= downMatrixList.get(0).get(1).getLow()
                            && matrixKlineRowList.get(i + 3).getLow() >= downMatrixList.get(0).get(1).getLow()) {
                        new MatrixKlineProcessor(matrixKlineRowList.stream().filter(row -> row.getDate() >= upMatrixList.get(0).get(0).getDate()).collect(Collectors.toList()));
                        return;
                    }
                }
            }
        }

        while (true) {
            List<MatrixKlineRow> currKlineRowList;
            if (searchUp) {
                for (int i = 0; i < upMatrixList.size(); i++) {
                    if (targetDate >= upMatrixList.get(i).get(0).getDate()
                            && targetDate < upMatrixList.get(i).get(upMatrixList.get(i).size() - 1).getDate()) {
                        currKlineRowList = upMatrixList.get(i);
                        System.out.println("up:     " + targetDate + "_" + currKlineRowList.get(currKlineRowList.size() - 1).getDate());
                        targetDate = currKlineRowList.get(currKlineRowList.size() - 1).getDate();
                        searchUp = false;
                        break;
                    }
                }
                if (searchUp) {
                    break;
                }
            } else {
                for (int i = 0; i < downMatrixList.size(); i++) {
                    if (targetDate >= downMatrixList.get(i).get(0).getDate()
                            && targetDate < downMatrixList.get(i).get(downMatrixList.get(i).size() - 1).getDate()) {
                        currKlineRowList = downMatrixList.get(i);
                        System.out.println("down:   " + targetDate + "_" + currKlineRowList.get(currKlineRowList.size() - 1).getDate());
                        targetDate = currKlineRowList.get(currKlineRowList.size() - 1).getDate();
                        searchUp = true;
                        break;
                    }
                }
                if (!searchUp) {
                    break;
                }
            }

        }

    }*/



}
