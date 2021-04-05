package com.tdx.zq.draw;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.tdx.zq.draw.PeakKlineProcessor.MatrixKlineRow;
import com.tdx.zq.enums.PeakShapeEnum;
import com.tdx.zq.enums.TendencyTypeEnum;
import lombok.Data;

public class MatrixKlineProcessor {

    private List<MatrixKlineRow> matrixKlineRowList;
    private List<MatrixKlineRow> upMatrixKlineRowList;
    private List<MatrixKlineRow> downMatrixKlineRowList;
    private List<List<MatrixKlineRow>> upMatrixList;
    private List<List<MatrixKlineRow>> downMatrixList;
    private List<MatrixRange> matrixRangeList;
    private List<Matrix> matrixList;

    public MatrixKlineProcessor(List<MatrixKlineRow> matrixKlineRowList) {
        this.matrixKlineRowList = matrixKlineRowList;
        this.upMatrixList = new ArrayList<>();
        this.downMatrixList = new ArrayList<>();
        setMatrixTendency();
        setMatrixList();
        setMatrixMerge();
        setWeak1B();
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

        private long upper;
        private long lower;
        private long startDate;

        public MatrixContext(MatrixSegment matrixSegment) {
            this.lower = matrixSegment.lower;
            this.upper = matrixSegment.upper;
            this.startDate = matrixSegment.startDate;
        }

    }

    public class MatrixRange {
        private long startDate;
        private long endDate;
        private TendencyTypeEnum tendency;
        private List<MatrixKlineRow> rows;
        private List<Matrix> matrixs;
        private int upperRowIdx;
        private int lowerRowIdx;
        private MatrixKlineRow upperRow;
        private MatrixKlineRow lowerRow;
        public MatrixRange(long startDate, long endDate, TendencyTypeEnum tendency) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.tendency = tendency;
            this.rows = new ArrayList<>();
            this.matrixs = new ArrayList<>();
        }
        public void setRows(List<MatrixKlineRow> rows) {
            this.rows = rows;
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;
            for (MatrixKlineRow row : rows) {
                min = Math.min(min, row.getLow());
                max = Math.max(max, row.getHigh());
            }
            for (int i = 0; i < rows.size(); i++) {
                if (rows.get(i).getHigh() == max) {
                    upperRowIdx = i;
                    upperRow = rows.get(i);
                }
                if (rows.get(i).getLow() == min) {
                    lowerRowIdx = i;
                    lowerRow = rows.get(i);
                }
            }
        }
        public void addMatrix(Matrix matrix) {
            matrixs.add(matrix);
        }
        public List<MatrixKlineRow> getRows() {
            return this.rows;
        }
        public TendencyTypeEnum getTendency() {
            return this.tendency;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder()
                    .append(tendency.name().toLowerCase())
                    .append(tendency == TendencyTypeEnum.UP ? "  : " : ": ")
                    .append(startDate)
                    .append(" - ")
                    .append(endDate);
            return sb.toString();
        }
    }

    @Data
    public class Matrix {
        private long high;
        private long low;
        private long startDate;
        private long endDate;
        private TendencyTypeEnum tendency;
        private int rangeIndex;
        private long rangeLow;
        private long rangeHigh;
        private MatrixKlineRow tailRow;
        public Matrix(long high, long low, long startDate, long endDate,
                      TendencyTypeEnum tendency, int rangeIndex, long rangeLow, long rangeHigh, MatrixKlineRow tailRow) {
            this.high = high;
            this.low = low;
            this.startDate = startDate;
            this.endDate = endDate;
            this.tendency = tendency;
            this.rangeIndex = rangeIndex;
            this.rangeLow = rangeLow;
            this.rangeHigh = rangeHigh;
            this.tailRow = tailRow;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("{");
            sb.append("\"tendency\":")
                    .append(tendency);
            sb.append(",\"startDate\":")
                    .append(startDate);
            sb.append(",\"endDate\":")
                    .append(endDate);
            sb.append(",\"high\":")
                    .append(high);
            sb.append(",\"low\":")
                    .append(low);
            sb.append('}');
            return sb.toString();
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

        // 2020-07-14 -- 2020-12-25
        // 2.获取所有的上升下降区间
        List<MatrixRange> matrixRangeList = new ArrayList<>();
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
                                && next.upper > upperContext.upper
                                && tendency == PeakShapeEnum.TOP
                                && curr.lower > upperContext.lower) {
                            continue;
                        }
                        if (tendency == PeakShapeEnum.TOP && upperContext != null) {
                            matrixRangeList.add(new MatrixRange(upperContext.startDate, lowerContext.startDate, TendencyTypeEnum.UP));
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
                                && next.lower < lowerContext.lower
                                && tendency == PeakShapeEnum.FLOOR
                                && curr.upper < lowerContext.upper) {
                            continue;
                        }
                        if (tendency == PeakShapeEnum.FLOOR && lowerContext != null) {
                            matrixRangeList.add(new MatrixRange(lowerContext.startDate, upperContext.startDate, TendencyTypeEnum.DOWN));
                        }
                        lowerContext = null;
                        upperContext.upper = curr.upper;
                        tendency = PeakShapeEnum.TOP;
                    }
                }
            }

            if (i == matrixSegmentList.size() - 1 && tendency != null) {
                if (tendency == PeakShapeEnum.FLOOR) {
                    matrixRangeList.add(new MatrixRange(lowerContext.startDate, matrixSegmentList.get(matrixSegmentList.size() - 1).endDate, TendencyTypeEnum.DOWN));
                } else {
                    matrixRangeList.add(new MatrixRange(upperContext.startDate, matrixSegmentList.get(matrixSegmentList.size() - 1).endDate, TendencyTypeEnum.UP));
                }
            }
        }

        // 3.对上升下降区间内的K线列表进行赋值
        int index = 0;
        for (MatrixRange matrixRange : matrixRangeList) {
            List<MatrixKlineRow> rows = new ArrayList<>();
            for (int i = index; i < matrixKlineRowList.size(); i++) {
                MatrixKlineRow row = matrixKlineRowList.get(i);
                if (row.getDate() >= matrixRange.startDate && row.getDate() <= matrixRange.endDate) {
                    rows.add(row);
                }
                if (row.getDate() == matrixRange.endDate) {
                    matrixRange.setRows(rows);
                    index = i;
                    break;
                }
            }
        }

        // 3.1 头部趋势调整范围
        MatrixRange head = matrixRangeList.get(0);
        List<MatrixKlineRow> headRows = head.getRows();
        if (head.getTendency() == TendencyTypeEnum.DOWN) {
            long max = Long.MIN_VALUE;
            for (int i = 0; i < headRows.size(); i++) {
                MatrixKlineRow headRow = headRows.get(i);
                if (headRow.getShape() == PeakShapeEnum.TOP) {
                    max = Math.max(headRow.getHigh(), max);
                }
            }
            int idx = 0;
            for (int i = 0; i < headRows.size(); i++) {
                MatrixKlineRow headRow = headRows.get(i);
                if (headRow.getHigh() == max) {
                    idx = i;
                    head.startDate = headRow.getDate();
                    break;
                }
            }
            if (idx > 0) {
                for (int i = idx; i >= 0; i--) {
                    headRows.remove(i);
                }
            }
        } else if (head.getTendency() == TendencyTypeEnum.DOWN) {
            long min = Long.MAX_VALUE;
            for (int i = 0; i < headRows.size(); i++) {
                MatrixKlineRow headRow = headRows.get(i);
                if (headRow.getShape() == PeakShapeEnum.FLOOR) {
                    min = Math.min(headRow.getLow(), min);
                }
            }
            int idx = 0;
            for (int i = 0; i < headRows.size(); i++) {
                MatrixKlineRow headRow = headRows.get(i);
                if (headRow.getLow() == min) {
                    idx = i;
                    head.startDate = headRow.getDate();
                    break;
                }
            }
            if (idx > 0) {
                for (int i = idx; i >= 0; i--) {
                    headRows.remove(i);
                }
            }
        }

        System.out.println();
        System.out.println("---------- Tendency Range List ----------");
        matrixRangeList.stream().forEach(matrixRange -> System.out.println(matrixRange));
        System.out.println("-------------- END --------------");

        this.matrixRangeList = matrixRangeList;

        //this.matrixRangeList = matrixRangeList.stream().filter(matrixRange -> matrixRange.getRows().size() >= 5).collect(Collectors.toList());
    }

    private void setMatrixList() {
        List<MatrixRange> matrixRangeList = this.matrixRangeList.stream().filter(matrixRange -> matrixRange.getRows().size() >= 5).collect(Collectors.toList());
        List<Matrix> matrixList = new ArrayList<>();
        for (int i = 0; i < matrixRangeList.size(); i++) {
            MatrixRange range = matrixRangeList.get(i);
            List<MatrixKlineRow> rows = range.getRows();
            for (int j = 0; j <= rows.size() - 5;) {
                MatrixKlineRow r1 = rows.get(j + 1);
                MatrixKlineRow r2 = rows.get(j + 2);
                MatrixKlineRow r3 = rows.get(j + 3);
                MatrixKlineRow r4 = rows.get(j + 4);
                MatrixKlineRow r6 = j + 6 < rows.size() ? rows.get(j + 6) : null;
                if (range.getTendency() == TendencyTypeEnum.DOWN && r1.getLow() <= r4.getHigh()) {
                    long low = Math.max(r1.getLow(), r3.getLow());
                    long high = Math.min(r2.getHigh(), r4.getHigh());
                    long rangeLow = Math.min(r1.getLow(), r3.getLow());
                    long rangeHigh = Math.max(r2.getHigh(), r4.getHigh());
                    matrixList.add(new Matrix(high, low, r1.getDate(), r4.getDate(),
                            TendencyTypeEnum.DOWN, i, rangeLow, rangeHigh, r6));
                    j = j + 4;
                } else if (range.getTendency() == TendencyTypeEnum.UP && r1.getHigh() >= r4.getLow()) {
                    long high = Math.min(r1.getHigh(), r3.getHigh());
                    long low = Math.max(r2.getLow(), r4.getLow());
                    long rangeLow = Math.min(r2.getLow(), r4.getLow());
                    long rangeHigh = Math.max(r1.getHigh(), r3.getHigh());
                    matrixList.add(new Matrix(high, low, r1.getDate(), r4.getDate(),
                            TendencyTypeEnum.UP, i, rangeLow, rangeHigh, r6));
                    j = j + 4;
                } else {
                    j++;
                }
                if (j > rows.size() - 5) {
                    boolean isBreak = false;
                    if (range.getTendency() == TendencyTypeEnum.DOWN) {
                        for (int z = j; z < rows.size(); z++) {
                            MatrixKlineRow r5 = rows.get(z);
                            if (r5.getShape() == PeakShapeEnum.FLOOR
                                    && r5.getLow() < r3.getLow()
                                    && r5.getLow() < r1.getLow()) {
                                isBreak = true;
                            }
                        }
                        if (isBreak == false) {
                            matrixList.remove(matrixList.size() - 1);
                        }
                    } else if (range.getTendency() == TendencyTypeEnum.UP) {
                        for (int z = j; z < rows.size(); z++) {
                            MatrixKlineRow r5 = rows.get(z);
                            if (r5.getShape() == PeakShapeEnum.TOP
                                    && r5.getHigh() > r3.getHigh()
                                    && r5.getHigh() > r1.getHigh()) {
                                isBreak = true;
                            }
                        }
                        if (isBreak == false) {
                            matrixList.remove(matrixList.size() - 1);
                        }
                    }
                }
            }
        }
        this.matrixList = matrixList;

        System.out.println();
        System.out.println("---------- Matrix List ----------");
        matrixList.stream().forEach(matrix -> System.out.println(matrix));
        System.out.println("-------------- END --------------");

    }

    private void setMatrixMerge() {
        boolean hasMerge = false;
        for (int i = 1; i < matrixList.size();) {
            Matrix prev = matrixList.get(i - 1);
            Matrix curr = matrixList.get(i);
            if (prev.getTendency() == curr.getTendency()
                    && prev.getRangeIndex() == curr.getRangeIndex()
                    && (!(prev.getRangeHigh() < curr.getRangeLow() || prev.getRangeLow() > curr.getRangeHigh())
                        || !(prev.getRangeHigh() < curr.getTailRow().getLow() || prev.getRangeLow() > curr.getTailRow().getHigh()))) {
                Matrix matrix = new Matrix(
                        Math.max(prev.getHigh(), curr.getHigh()),
                        Math.min(prev.getLow(), curr.getLow()),
                        prev.getStartDate(), curr.getEndDate(), curr.getTendency(), prev.rangeIndex,
                        Math.min(prev.getRangeLow(), curr.getRangeLow()),
                        Math.max(prev.getRangeHigh(), curr.getRangeHigh()), curr.tailRow);
                matrixList.set(i - 1, matrix);
                matrixList.remove(i);
                hasMerge = true;
            } else {
                i++;
            }
        }

        if (hasMerge) {
            setMatrixMerge();
        } else {
            System.out.println();
            System.out.println("---------- Merge Matrix List ----------");
            matrixList.stream().forEach(matrix -> System.out.println(matrix));
            System.out.println("-------------- END --------------");
            System.out.println();
        }

    }

    public void setWeak1B() {

        for (MatrixRange matrixRange : matrixRangeList) {
            for (Matrix matrix : matrixList) {
                if (matrix.startDate > matrixRange.endDate) {
                    break;
                }
                if (matrix.startDate > matrixRange.startDate && matrix.endDate < matrixRange.endDate) {
                    matrixRange.addMatrix(matrix);
                }
            }
        }

        System.out.println("---------- 1B Point ----------");
        List<String> b1PointTypes = new ArrayList<>();
        List<MatrixKlineRow> b1PointDates = new ArrayList<>();
        for (MatrixRange matrixRange : matrixRangeList) {
            if (matrixRange.getTendency() == TendencyTypeEnum.DOWN) {
                if (matrixRange.matrixs.size() == 0) {
                    b1PointTypes.add("Weak1B");
                    System.out.println("Weak1B Date: " + matrixRange.lowerRow.getDate());
                } else {
                    b1PointTypes.add("1B");
                    System.out.println("1B Date    : " + matrixRange.lowerRow.getDate() + ", Matrix Count: " + matrixRange.matrixs.size());
                }
                b1PointDates.add(matrixRange.lowerRow);
            }
        }
        System.out.println("-------------- END --------------");
        System.out.println();

        System.out.println("---------- 1S Point ----------");
        List<String> s1PointTypes = new ArrayList<>();
        List<MatrixKlineRow> s1PointDates = new ArrayList<>();
        for (MatrixRange matrixRange : matrixRangeList) {
            if (matrixRange.getTendency() == TendencyTypeEnum.UP) {
                if (matrixRange.matrixs.size() == 0) {
                    s1PointTypes.add("Weak1S");
                    System.out.println("Weak1S Date: " + matrixRange.upperRow.getDate());
                } else {
                    s1PointTypes.add("1S");
                    System.out.println("1S Date    : " + matrixRange.upperRow.getDate() + ", Matrix Count: " + matrixRange.matrixs.size());
                }
                s1PointDates.add(matrixRange.upperRow);
            }
        }
        System.out.println("-------------- END --------------");
        System.out.println();

        System.out.println("---------- 2B Point ----------");
        List<MatrixKlineRow> b2PointDates = new ArrayList<>();
        for (int i = 0, j = 0; i + 2 < matrixKlineRowList.size() && j < b1PointDates.size(); i++) {
            if (matrixKlineRowList.get(i).getDate() == b1PointDates.get(j).getDate()) {
                System.out.println("2B Date: " + matrixKlineRowList.get(i + 2).getDate());
                b2PointDates.add(matrixKlineRowList.get(i + 2));
                j++;
            }
        }

        System.out.println("-------------- END --------------");
        System.out.println();

        System.out.println("---------- 2S Point ----------");
        List<MatrixKlineRow> s2PointDates = new ArrayList<>();
        for (int i = 0, j = 0; i + 2 < matrixKlineRowList.size() && j < s1PointDates.size(); i++) {
            if (matrixKlineRowList.get(i).getDate() == s1PointDates.get(j).getDate()) {
                System.out.println("2S Date: " + matrixKlineRowList.get(i + 2).getDate());
                s2PointDates.add(matrixKlineRowList.get(i + 2));
                j++;
            }
        }
        System.out.println("-------------- END --------------");
        System.out.println();

        System.out.println("---------- NewB Point ----------");
        if (b1PointDates.get(b1PointDates.size() - 1).getDate() > b2PointDates.get(b2PointDates.size() - 1).getDate()) {
            System.out.println(b1PointTypes.get(b1PointTypes.size() - 1) + ": " + b1PointDates.get(b1PointDates.size() - 1).getDate());
        } else {
            System.out.println("2B    : " + b2PointDates.get(b2PointDates.size() - 1).getDate());
        }
        System.out.println("-------------- END --------------");
        System.out.println();

        System.out.println("---------- NewS Point ----------");
        if (b1PointDates.get(s1PointDates.size() - 1).getDate() > b2PointDates.get(s2PointDates.size() - 1).getDate()) {
            System.out.println(s1PointTypes.get(s1PointTypes.size() - 1) + ": " + s1PointDates.get(s1PointDates.size() - 1).getDate());
        } else {
            System.out.println("2S :" + s2PointDates.get(s2PointDates.size() - 1).getDate());
        }
        System.out.println("-------------- END --------------");
        System.out.println();


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
