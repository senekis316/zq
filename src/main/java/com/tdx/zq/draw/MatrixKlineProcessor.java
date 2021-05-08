package com.tdx.zq.draw;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
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
        setBSPoint();
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
        public List<Matrix> getMatrixs() {
            return this.matrixs;
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
                MatrixKlineRow r6 = r4.getIdx() + 2 < matrixKlineRowList.size() ? matrixKlineRowList.get(r4.getIdx() + 2) : null;
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
                if (j > rows.size() - 5 && range.matrixs.size() > 0) {
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

    public void setBSPoint() {

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

        PriorityQueue<BSPoint> bsPoints = new PriorityQueue<>((o1, o2) -> {
            if (o1.getDate() > o2.getDate()) {
                return 1;
            } else if (o1.getDate() < o2.getDate()) {
                return -1;
            } else {
                return 0;
            }
        });


        // 获取假设的MatrixRange
        mockMatrixRange();

        System.out.println("---------- Last BSPoints ----------");

        // 最后的BSPoint
        MatrixRange prevRange = matrixRangeList.get(matrixRangeList.size() - 2);
        MatrixRange currRange = matrixRangeList.get(matrixRangeList.size() - 1);
        if (prevRange.getMatrixs().size() > 0) {
            if (currRange.getTendency() == TendencyTypeEnum.UP) {
                bsPoints.add(new BSPoint(currRange.getRows().get(0).getDate(), PointType.B1));
            } else {
                bsPoints.add(new BSPoint(currRange.getRows().get(0).getDate(), PointType.S1));
            }
        } else {
            if (currRange.getTendency() == TendencyTypeEnum.UP) {
                bsPoints.add(new BSPoint(currRange.getRows().get(0).getDate(), PointType.WEAK_B1));
            } else {
                bsPoints.add(new BSPoint(currRange.getRows().get(0).getDate(), PointType.WEAK_S1));
            }
        }

        if (currRange.getMatrixs().size() > 0) {
            if (currRange.getTendency() == TendencyTypeEnum.DOWN) {
                bsPoints.add(new BSPoint(currRange.lowerRow.getDate(), PointType.B1));
            } else {
                bsPoints.add(new BSPoint(currRange.upperRow.getDate(), PointType.S1));
            }
        } else {
            if (currRange.getTendency() == TendencyTypeEnum.DOWN) {
                if (currRange.lowerRow.getLow() < currRange.getRows().get(1).getLow()) {
                    bsPoints.add(new BSPoint(currRange.lowerRow.getDate(), PointType.WEAK_B1));
                }
            } else {
                if (currRange.upperRow.getHigh() > currRange.getRows().get(1).getHigh()) {
                    bsPoints.add(new BSPoint(currRange.upperRow.getDate(), PointType.WEAK_S1));
                }
            }
        }

        if (currRange.getRows().size() >= 3) {
            if (currRange.getTendency() == TendencyTypeEnum.UP) {
                bsPoints.add(new BSPoint(currRange.getRows().get(2).getDate(), PointType.B2));
            } else {
                bsPoints.add(new BSPoint(currRange.getRows().get(2).getDate(), PointType.S2));
            }
        }

        List<MatrixKlineRow> rows = currRange.getRows();

        for (int i = 0; i <= rows.size() - 5; i += 2) {
            MatrixKlineRow row2 = rows.get(i + 1);
            MatrixKlineRow row4 = rows.get(i + 3);
            MatrixKlineRow row5 = rows.get(i + 4);
            if (currRange.matrixs.size() > 0 && row2.getDate() == currRange.startDate) {
                if (currRange.getTendency() == TendencyTypeEnum.UP) {
                    if (row4.getHigh() > row2.getHigh()) {
                        bsPoints.add(new BSPoint(row5.getDate(), PointType.SIMILAR_B2));
                    } else {
                        bsPoints.add(new BSPoint(row5.getDate(), PointType.SIMILAR_WEAK_B2));
                    }
                } else {
                    if (row4.getLow() < row2.getLow()) {
                        bsPoints.add(new BSPoint(row5.getDate(), PointType.SIMILAR_S2));
                    } else {
                        bsPoints.add(new BSPoint(row5.getDate(), PointType.SIMILAR_WEAK_S2));
                    }
                }
            } else {
                if (currRange.getTendency() == TendencyTypeEnum.UP) {
                    if (row5.getLow() <= row2.getHigh()) {
                        if (row4.getHigh() > row2.getHigh()) {
                            bsPoints.add(new BSPoint(row5.getDate(), PointType.SIMILAR_B2));
                        } else {
                            bsPoints.add(new BSPoint(row5.getDate(), PointType.SIMILAR_WEAK_B2));
                        }
                    }
                } else {
                    if (row5.getHigh() >= row2.getLow()) {
                        if (row4.getLow() < row2.getLow()) {
                            bsPoints.add(new BSPoint(row5.getDate(), PointType.SIMILAR_S2));
                        } else {
                            bsPoints.add(new BSPoint(row5.getDate(), PointType.SIMILAR_WEAK_S2));
                        }
                    }
                }
            }

            if (i + 6 < rows.size()) {
                MatrixKlineRow row6 = rows.get(i + 5);
                if (currRange.getTendency() == TendencyTypeEnum.UP) {
                    if (row6.getHigh() > row2.getHigh() && row6.getHigh() > row4.getHigh()) {
                        break;
                    }
                } else {
                    if (row6.getLow() < row2.getLow() && row6.getLow() < row4.getLow()) {
                        break;
                    }
                }
            }
        }

        int length = bsPoints.size();
        for (int i = 0; i < length; i++) {
            System.out.println(bsPoints.poll());
        }

        System.out.println("-------------- END --------------");
        System.out.println();
    }

    // 创建假设MatrixRange
    private void mockMatrixRange() {

        MatrixRange range = matrixRangeList.get(matrixRangeList.size() - 1);
        List<MatrixKlineRow> rows = range.getRows();

        // 作出假设的range范围, 找出最低或最高的点作为假设的range分割点
        MatrixKlineRow row = range.upperRow.getDate() > range.lowerRow.getDate()
                ? range.upperRow : range.lowerRow;

        // 根据是否存在矩阵, 判断是否切分矩阵
        boolean split = range.matrixs.size() == 0 ? false : true;

        // 判断假设的分割点是否有效
        if (row == rows.get(0) || row == rows.get(rows.size() - 1)) {
            split = false;
        }

        // 拆分原来的MatrixRange
        if (split) {
            // 先拆分MatrixKlineRow
            List<MatrixKlineRow> rows1 = new ArrayList<>();
            List<MatrixKlineRow> rows2 = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                if (rows.get(i).getDate() < row.getDate()) {
                    rows1.add(rows.get(i));
                } else {
                    rows2.add(rows.get(i));
                }
            }

            // 创建拆分开的第一个矩阵
            MatrixRange matrixRange1 = new MatrixRange(rows1.get(0).getDate(), rows1.get(rows1.size() - 1).getDate(), range.tendency);
            matrixRange1.setRows(rows1);
            matrixRange1.matrixs = range.getMatrixs();

            // 创建拆分开的第二个矩阵
            MatrixRange matrixRange2 = new MatrixRange(rows2.get(0).getDate(), rows2.get(rows2.size() - 1).getDate(),
                    range.tendency == TendencyTypeEnum.DOWN ? TendencyTypeEnum.UP : TendencyTypeEnum.DOWN);
            matrixRange2.setRows(rows2);

            matrixRangeList.remove(matrixRangeList.size() - 1);
            matrixRangeList.add(matrixRange1);
            matrixRangeList.add(matrixRange2);

        }
    }

    public enum PointType {
        WEAK_B1, B1, B2, WEAK_S1, S1, S2,
        SIMILAR_WEAK_B2, SIMILAR_B2, SIMILAR_WEAK_S2, SIMILAR_S2;
    }

    @Data
    public class BSPoint {
        private long date;
        private PointType pointType;
        public BSPoint(long date, PointType pointType) {
            this.date = date;
            this.pointType = pointType;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append(pointType).append(": ").append(date);
            return sb.toString();
        }
    }

}
