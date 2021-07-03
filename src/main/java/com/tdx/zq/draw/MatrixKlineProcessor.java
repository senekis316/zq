package com.tdx.zq.draw;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import com.tdx.zq.context.KlineApplicationContext;
import com.tdx.zq.enums.KlineType;
import com.tdx.zq.enums.PeakShapeEnum;
import com.tdx.zq.enums.PointType;
import com.tdx.zq.enums.TendencyTypeEnum;
import com.tdx.zq.model.BSPoint;
import com.tdx.zq.model.Matrix;
import com.tdx.zq.model.MatrixKlineRow;
import com.tdx.zq.model.PeakKline;


public class MatrixKlineProcessor {

    private String klineCode;
    private KlineType klineType;
    private List<Matrix> matrixList;
    private List<PeakKline> peakKlineList;
    private List<MatrixRange> matrixRangeList;
    private List<MatrixKlineRow> matrixKlineRowList;
    private Map<String, Map<KlineType, PriorityQueue<BSPoint>>> bsPointMap;
    private Map<String, Map<KlineType, String>> enhanceMap;

    public MatrixKlineProcessor(KlineApplicationContext klineApplicationContext) {
        this.klineType = klineApplicationContext.getKlineType();
        this.klineCode = klineApplicationContext.getKlineCode();
        this.enhanceMap = klineApplicationContext.getEnhanceMap();
        this.bsPointMap = klineApplicationContext.getBsPointMap();
        this.matrixKlineRowList = klineApplicationContext.getMatrixKlineRowList();
        this.peakKlineList = klineApplicationContext.getPeakKlineList();
        if (matrixKlineRowList == null || matrixKlineRowList.size() < 3) return;
        setMatrixTendency();
        setMatrixList();
        setMatrixMerge();
        setBSPoint();
        setAngle();
    }

    private void setAngle() {

        MatrixRange prevRange = matrixRangeList.size() > 1 ? matrixRangeList.get(matrixRangeList.size() - 2) : null;
        MatrixRange currRange = matrixRangeList.get(matrixRangeList.size() - 1);

        if (currRange.getRows().size() < 4) {
            currRange = prevRange;
        }

        if (prevRange == null || prevRange.getRows().size() < 4) {
            return;
        }

        List<MatrixKlineRow> rows = currRange.getRows();

        float angle1 = 0;
        float angle2 = 0;
        boolean enhance = false;
        if (currRange.getMatrixs().size() == 0) {
            //currRange.getRows();
        } else {
            Matrix matrix = currRange.getMatrixs().get(currRange.getMatrixs().size() - 1);
            for (int i = 0; i < rows.size(); i++) {
                MatrixKlineRow row = rows.get(i);
                if (row.getDate() == matrix.getStartDate()) {
                    MatrixKlineRow row1 = rows.get(i - 1);
                    MatrixKlineRow row2 = row;
                    //long dayDiff = row2.getIdx() - row1.getIdx();
                    float percent;
                    long dayDiff = peakKlineList.stream()
                            .filter(peak -> peak.getMergeKline().getMergeKline().getDate() >= row1.getDate())
                            .filter(peak -> peak.getMergeKline().getMergeKline().getDate() <= row2.getDate()).count();
                    if (matrix.getTendency() == TendencyTypeEnum.UP) {
                        percent = ((float)(row2.getHigh() - row1.getLow())) / row1.getLow();
                    } else {
                        percent = ((float)(row1.getHigh() - row2.getLow())) / row1.getHigh();
                    }
                    angle1 = percent / dayDiff;
                }
                if (row.getDate() == matrix.getEndDate()) {
                    if (matrix.getTendency() == TendencyTypeEnum.UP) {
                        final MatrixRange matrixRange = currRange;
                        long dayDiff = peakKlineList.stream()
                                .filter(peak -> peak.getMergeKline().getMergeKline().getDate() >= matrix.getEndDate())
                                .filter(peak -> peak.getMergeKline().getMergeKline().getDate() <= matrixRange.upperRow.getDate()).count();
                        float percent = ((float)(currRange.upperRow.getHigh() - row.getLow())) / row.getLow();
                        angle2 = percent / dayDiff;
                    } else {
                        final MatrixRange matrixRange = currRange;
                        long dayDiff = peakKlineList.stream()
                                .filter(peak -> peak.getMergeKline().getMergeKline().getDate() >= matrix.getEndDate())
                                .filter(peak -> peak.getMergeKline().getMergeKline().getDate() <= matrixRange.lowerRow.getDate()).count();
                        float percent = ((float)(row.getHigh() - currRange.lowerRow.getLow())) / row.getHigh();
                        angle2 = percent / dayDiff;
                    }
                    enhance = angle1 <= angle2;
                }
            }
        }
        this.enhanceMap.get(klineCode).put(klineType, currRange.getTendency() + "趋势" + (enhance ? "增强" : "减弱") + ":" + angle1 + "->" + angle2);
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
        private boolean tendencyConfirm = true;
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
        public void setTendencyConfirm(boolean tendencyConfirm) {
            this.tendencyConfirm = tendencyConfirm;
        }
        public boolean getTendencyConfirm() {
            return tendencyConfirm;
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
                    MatrixRange upMatrixRange = new MatrixRange(upperContext.startDate, matrixSegmentList.get(matrixSegmentList.size() - 1).endDate, TendencyTypeEnum.UP);
                    if (tendency == PeakShapeEnum.NONE) {
                        upMatrixRange.setTendencyConfirm(false);
                    }
                    matrixRangeList.add(upMatrixRange);
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
                for (int i = idx - 1; i >= 0; i--) {
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
                for (int i = idx - 1; i >= 0; i--) {
                    headRows.remove(i);
                }
            }
        }

//        System.out.println();
//        System.out.println("---------- Tendency Range List ----------");
//        matrixRangeList.stream().forEach(matrixRange -> System.out.println(matrixRange));
//        System.out.println("-------------- END --------------");

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

//        System.out.println();
//        System.out.println("---------- Matrix List ----------");
//        matrixList.stream().forEach(matrix -> System.out.println(matrix));
//        System.out.println("-------------- END --------------");
//        System.out.println();

    }

    private void setMatrixMerge() {
        List<MatrixRange> matrixRangeList = this.matrixRangeList.stream().filter(matrixRange -> matrixRange.getRows().size() >= 5).collect(Collectors.toList());
        boolean hasMerge = false;
        for (int i = 1; i < matrixList.size();) {
            Matrix prev = matrixList.get(i - 1);
            Matrix curr = matrixList.get(i);
            if (prev.getTendency() == curr.getTendency()
                    && prev.getRangeIndex() == curr.getRangeIndex()
                    && ((!(prev.getRangeHigh() < curr.getRangeLow() || prev.getRangeLow() > curr.getRangeHigh()))
                        || isExtendJoin(i, prev, curr, matrixRangeList))) {
                Matrix matrix = new Matrix(
                        Math.max(prev.getHigh(), curr.getHigh()),
                        Math.min(prev.getLow(), curr.getLow()),
                        prev.getStartDate(), curr.getEndDate(), curr.getTendency(), prev.getRangeIndex(),
                        Math.min(prev.getRangeLow(), curr.getRangeLow()),
                        Math.max(prev.getRangeHigh(), curr.getRangeHigh()), curr.getTailRow());
                matrixList.set(i - 1, matrix);
                matrixList.remove(i);
                hasMerge = true;
            } else {
                i++;
            }

        }

        if (hasMerge) {
            setMatrixMerge();
        }

    }

    private boolean isExtendJoin(int i, Matrix prev, Matrix curr, List<MatrixRange> matrixRangeList) {
        final Matrix next = i < matrixList.size() - 1 ? matrixList.get(i + 1) : null;
        if (next != null && next.getRangeIndex() != prev.getRangeIndex()) return false;
        MatrixRange matrixRange = matrixRangeList.get(prev.getRangeIndex());
        List<Long> prices;
        if (curr.getTendency() == TendencyTypeEnum.UP) {
            prices = matrixRange.getRows().stream().filter(row -> row.getShape() == PeakShapeEnum.FLOOR)
                    .filter(row -> row.getDate() > curr.getEndDate())
                    .filter(row -> row.getDate() < (next != null ? next.getStartDate() : matrixRange.endDate))
                    .map(row -> row.getLow()).collect(Collectors.toList());
        } else {
            prices = matrixRange.getRows().stream().filter(row -> row.getShape() == PeakShapeEnum.TOP)
                    .filter(row -> row.getDate() > curr.getEndDate())
                    .filter(row -> row.getDate() < (next != null ? next.getStartDate() : matrixRange.endDate))
                    .map(row -> row.getHigh()).collect(Collectors.toList());
        }
        return prices.stream().filter(price -> price >= prev.getLow() && price <= prev.getHigh()).count() > 0;
    }



    public void setBSPoint() {

        if (bsPointMap.get(klineCode).get(klineType) == null) {
            bsPointMap.get(klineCode).put(klineType, new PriorityQueue<>());
        }

        for (MatrixRange matrixRange : matrixRangeList) {
            for (Matrix matrix : matrixList) {
                if (matrix.getStartDate() > matrixRange.endDate) {
                    break;
                }
                if (matrix.getStartDate() > matrixRange.startDate && matrix.getEndDate() < matrixRange.endDate) {
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

        System.out.println(String.format("---------- %s %s Last BSPoints ----------", klineCode, klineType));

        // 最后的BSPoint
        MatrixRange prevRange = matrixRangeList.size() > 2 ? matrixRangeList.get(matrixRangeList.size() - 3) : null;
        MatrixRange currRange = matrixRangeList.size() > 1 ? matrixRangeList.get(matrixRangeList.size() - 2) : null;
        MatrixRange nextRange = matrixRangeList.get(matrixRangeList.size() - 1);

        if (currRange != null) {
            printBSPoints(bsPoints, prevRange, currRange);
        }

        if (prevRange == null && currRange == null && nextRange != null && nextRange.getTendencyConfirm() == false) {
            return;
        }

        if (nextRange != null) {
            printBSPoints(bsPoints, currRange, nextRange);
        }

//        int length = bsPoints.size();
//        for (int i = 0; i < length; i++) {
//            System.out.println(bsPoints.poll());
//        }

        Set<BSPoint> bsPointSet = new HashSet<>();
        bsPointSet.addAll(bsPoints);

        bsPoints.clear();
        bsPoints.addAll(bsPointSet);

        bsPointMap.get(klineCode).put(klineType, bsPoints);

        System.out.println("-------------- END --------------");
        System.out.println();
    }

    // Print BSPoint
    private void printBSPoints(PriorityQueue<BSPoint> bsPoints, MatrixRange prevRange, MatrixRange currRange) {

        List<Matrix> currMatrixList = currRange.getMatrixs();

        if (prevRange == null) {
            if (currMatrixList.size() == 0) {
                if (currRange.getTendency() == TendencyTypeEnum.UP) {
                    bsPoints.add(new BSPoint(currRange.getRows().get(0).getDate(), PointType.WEAK_B1, currMatrixList));
                    bsPoints.add(new BSPoint(currRange.upperRow.getDate(), PointType.WEAK_S1, currMatrixList));
                    if (currRange.getRows().size() >= 3) {
                        bsPoints.add(new BSPoint(currRange.getRows().get(2).getDate(), PointType.B2, currMatrixList));
                    }
                    if (currRange.getRows().size() > currRange.upperRowIdx + 2) {
                        bsPoints.add(new BSPoint(currRange.getRows().get(currRange.upperRowIdx + 2).getDate(), PointType.S2, currMatrixList));
                    }
                } else {
                    bsPoints.add(new BSPoint(currRange.getRows().get(0).getDate(), PointType.WEAK_S1, currMatrixList));
                    bsPoints.add(new BSPoint(currRange.lowerRow.getDate(), PointType.WEAK_B1, currMatrixList));
                    if (currRange.getRows().size() >= 3) {
                        bsPoints.add(new BSPoint(currRange.getRows().get(2).getDate(), PointType.S2, currMatrixList));
                    }
                    if (currRange.getRows().size() > currRange.lowerRowIdx + 2) {
                        bsPoints.add(new BSPoint(currRange.getRows().get(currRange.lowerRowIdx + 2).getDate(), PointType.B2, currMatrixList));
                    }
                }
            } else {
                if (currRange.getTendency() == TendencyTypeEnum.UP) {
                    bsPoints.add(new BSPoint(currRange.getRows().get(0).getDate(), PointType.B1, currMatrixList));
                    bsPoints.add(new BSPoint(currRange.upperRow.getDate(), PointType.S1, currMatrixList));
                    if (currRange.getRows().size() >= 3) {
                        bsPoints.add(new BSPoint(currRange.getRows().get(2).getDate(), PointType.B2, currMatrixList));
                    }
                    if (currRange.getRows().size() > currRange.upperRowIdx + 2) {
                        bsPoints.add(new BSPoint(currRange.getRows().get(currRange.upperRowIdx + 2).getDate(), PointType.S2, currMatrixList));
                    }
                } else {
                    bsPoints.add(new BSPoint(currRange.getRows().get(0).getDate(), PointType.S1, currMatrixList));
                    bsPoints.add(new BSPoint(currRange.lowerRow.getDate(), PointType.B1, currMatrixList));
                    if (currRange.getRows().size() >= 3) {
                        bsPoints.add(new BSPoint(currRange.getRows().get(2).getDate(), PointType.S2, currMatrixList));
                    }
                    if (currRange.getRows().size() > currRange.lowerRowIdx + 2) {
                        bsPoints.add(new BSPoint(currRange.getRows().get(currRange.lowerRowIdx + 2).getDate(), PointType.B2, currMatrixList));
                    }
                }
            }
        } else {
            List<Matrix> prevMatrixList = prevRange.getMatrixs();
            if (currRange.getRows().size() > 1) {
                if (prevRange.getTendency() == TendencyTypeEnum.DOWN && currRange.getTendency() == TendencyTypeEnum.UP) {
                    if (prevMatrixList.size() > 0) {
                        bsPoints.add(new BSPoint(currRange.getRows().get(0).getDate(), PointType.B1, prevMatrixList));
                    } else {
                        bsPoints.add(new BSPoint(currRange.getRows().get(0).getDate(), PointType.WEAK_B1, prevMatrixList));
                    }
                    if (currMatrixList.size() > 0) {
                        bsPoints.add(new BSPoint(currRange.upperRow.getDate(), PointType.S1, currMatrixList));
                    } else {
                        bsPoints.add(new BSPoint(currRange.upperRow.getDate(), PointType.WEAK_S1, currMatrixList));
                        if (prevMatrixList.size() == 0 && currRange.getRows().size() >= 4) {
                            bsPoints.add(new BSPoint(currRange.upperRow.getDate(), PointType.SIMILAR_WEAK_S2, currMatrixList));
                        }
                    }
                    if (currRange.getRows().size() >= 3) {
                        bsPoints.add(new BSPoint(currRange.getRows().get(2).getDate(), PointType.B2, prevMatrixList));
                    }
                    if (currRange.getRows().size() > currRange.upperRowIdx + 2) {
                        bsPoints.add(new BSPoint(currRange.getRows().get(currRange.upperRowIdx + 2).getDate(), PointType.S2, currMatrixList));
                    }
                }
                if (prevRange.getTendency() == TendencyTypeEnum.UP && currRange.getTendency() == TendencyTypeEnum.DOWN) {
                    if (prevMatrixList.size() > 0) {
                        bsPoints.add(new BSPoint(currRange.getRows().get(0).getDate(), PointType.S1, prevMatrixList));
                    } else {
                        bsPoints.add(new BSPoint(currRange.getRows().get(0).getDate(), PointType.WEAK_S1, prevMatrixList));
                    }
                    if (currMatrixList.size() > 0) {
                        bsPoints.add(new BSPoint(currRange.lowerRow.getDate(), PointType.B1, currMatrixList));
                    } else {
                        bsPoints.add(new BSPoint(currRange.lowerRow.getDate(), PointType.WEAK_B1, currMatrixList));
                        if (prevMatrixList.size() == 0 && currRange.getRows().size() >= 4) {
                            bsPoints.add(new BSPoint(currRange.lowerRow.getDate(), PointType.SIMILAR_WEAK_B2, prevMatrixList));
                        }
                    }
                    if (currRange.getRows().size() >= 3) {
                        bsPoints.add(new BSPoint(currRange.getRows().get(2).getDate(), PointType.S2, prevMatrixList));
                    }
                    if (currRange.getRows().size() > currRange.lowerRowIdx + 2) {
                        bsPoints.add(new BSPoint(currRange.getRows().get(currRange.lowerRowIdx + 2).getDate(), PointType.B2, currMatrixList));
                    }
                }
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
                        bsPoints.add(new BSPoint(row5.getDate(), PointType.SIMILAR_B2, currMatrixList));
                    } else {
                        bsPoints.add(new BSPoint(row5.getDate(), PointType.SIMILAR_WEAK_B2, currMatrixList));
                    }
                } else {
                    if (row4.getLow() < row2.getLow()) {
                        bsPoints.add(new BSPoint(row5.getDate(), PointType.SIMILAR_S2, currMatrixList));
                    } else {
                        bsPoints.add(new BSPoint(row5.getDate(), PointType.SIMILAR_WEAK_S2, currMatrixList));
                    }
                }
            } else {
                if (currRange.getTendency() == TendencyTypeEnum.UP) {
                    if (row5.getLow() <= row2.getHigh()) {
                        if (row4.getHigh() > row2.getHigh()) {
                            bsPoints.add(new BSPoint(row5.getDate(), PointType.SIMILAR_B2, currMatrixList));
                        } else {
                            bsPoints.add(new BSPoint(row5.getDate(), PointType.SIMILAR_WEAK_B2, currMatrixList));
                        }
                    }
                } else {
                    if (row5.getHigh() >= row2.getLow()) {
                        if (row4.getLow() < row2.getLow()) {
                            bsPoints.add(new BSPoint(row5.getDate(), PointType.SIMILAR_S2, currMatrixList));
                        } else {
                            bsPoints.add(new BSPoint(row5.getDate(), PointType.SIMILAR_WEAK_S2, currMatrixList));
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

        // 3.查找第三类买卖点
        MatrixRange matrixRange = currRange;//matrixRangeList.get(matrixRangeList.size() - 1);
        List<Matrix> matrixList = matrixRange.getMatrixs();
        List<MatrixKlineRow> matrixKlineRowList = matrixRange.getRows();

        if (matrixList.size() == 0 && matrixRangeList.size() > 1) {
            matrixRange = matrixRangeList.get(matrixRangeList.size() - 2);
            matrixList = matrixRange.getMatrixs();
            matrixKlineRowList = new ArrayList<>();
            matrixKlineRowList.addAll(matrixRange.getRows());
            matrixKlineRowList.addAll(matrixRangeList.get(matrixRangeList.size() - 1).getRows());
        }

        if (matrixList.size() > 0) {

            Matrix head = matrixList.get(0);
            Matrix tail = matrixList.get(matrixList.size() - 1);

            // 3.1 查找上升趋势点买点和下降趋势的卖点
            if (matrixRange.getTendency() == TendencyTypeEnum.UP) {
                for (int i = matrixKlineRowList.size() - 1; i >= 0 ; i--) {
                    MatrixKlineRow row = matrixKlineRowList.get(i);
                    if (row.getDate() > head.getEndDate()
                            && row.getDate() >= currRange.startDate
                            && row.getDate() <= currRange.endDate
                            && row.getShape() == PeakShapeEnum.FLOOR
                            && row.getLow() > head.getHigh()) {
                        bsPoints.add(new BSPoint(row.getDate(), PointType.B3, currMatrixList));
                        break;
                    }
                }
            } else {
                for (int i = matrixKlineRowList.size() - 1; i >= 0 ; i--) {
                    MatrixKlineRow row = matrixKlineRowList.get(i);
                    if (row.getDate() > head.getEndDate()
                            && row.getDate() >= currRange.startDate
                            && row.getDate() <= currRange.endDate
                            && row.getShape() == PeakShapeEnum.TOP
                            && row.getHigh() < head.getLow()) {
                        bsPoints.add(new BSPoint(row.getDate(), PointType.S3, matrixList));
                        break;
                    }
                }
            }

            // 3.2 查找上升趋势的卖点和下降趋势的买点
            if (matrixRange.getTendency() == TendencyTypeEnum.UP) {
                for (int i = matrixKlineRowList.size() - 1; i >= 0 ; i--) {
                    MatrixKlineRow row = matrixKlineRowList.get(i);
                    if (row.getDate() > tail.getEndDate()
                            && row.getDate() >= currRange.startDate
                            && row.getDate() <= currRange.endDate
                            && row.getShape() == PeakShapeEnum.TOP
                            && row.getHigh() < tail.getLow()) {
                        bsPoints.add(new BSPoint(row.getDate(), PointType.S3, matrixList));
                        break;
                    }
                }
            } else {
                for (int i = matrixKlineRowList.size() - 1; i >= 0 ; i--) {
                    MatrixKlineRow row = matrixKlineRowList.get(i);
                    if (row.getDate() > tail.getEndDate()
                            && row.getDate() >= currRange.startDate
                            && row.getDate() <= currRange.endDate
                            && row.getShape() == PeakShapeEnum.FLOOR
                            && row.getLow() > tail.getHigh()) {
                        bsPoints.add(new BSPoint(row.getDate(), PointType.B3, currMatrixList));
                        break;
                    }
                }
            }
        }
    }

    // 创建假设MatrixRange
    private void mockMatrixRange() {

//        System.out.println();
//        System.out.println("---------- Before Split Matrix List ----------");
//        matrixList.stream().forEach(matrix -> System.out.println(matrix));
//        System.out.println("-------------- END --------------");
//        System.out.println();

        MatrixRange range = matrixRangeList.get(matrixRangeList.size() - 1);
        List<MatrixKlineRow> rows = range.getRows();

        // 作出假设的range范围, 找出最低或最高的点作为假设的range分割点
        MatrixKlineRow row = range.upperRow.getDate() > range.lowerRow.getDate()
                ? range.upperRow : range.lowerRow;

        // 根据是否存在矩阵, 判断是否切分矩阵
        boolean split = range.matrixs.size() == 0 ? false : true;

        // 判断假设的分割点是否有效
        if (row == rows.get(0) || range.getTendencyConfirm() == false) { // || row == rows.get(rows.size() - 1)) {
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
                } else if (rows.get(i).getDate() > row.getDate()) {
                    rows2.add(rows.get(i));
                } else {
                    rows1.add(rows.get(i));
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

//        System.out.println();
//        System.out.println("---------- Merge Matrix List ----------");
//        for (MatrixRange matrixRange : matrixRangeList) {
//            matrixRange.getMatrixs().stream().forEach(matrix -> System.out.println(matrix));
//        }
//        System.out.println("-------------- END --------------");
//        System.out.println();

    }





}
