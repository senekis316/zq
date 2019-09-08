package com.tdx.zq.draw;

import com.tdx.zq.model.CombineKline;
import com.tdx.zq.model.Kline;
import com.tdx.zq.utils.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;


@Slf4j
@Service
public class KLineDrawService implements InitializingBean {

    private List<Kline> originalKLineList = new LinkedList<>();

    private Map<Integer, Kline> originalKLineMap = new HashMap<>();

    @Autowired
    private CombineKLineService combineKLineService;

    @Autowired
    private PeakKlineService peakKlineService;

    public void compute() {

        //1.合并所有的KLines
        List<CombineKline> combineKLineList = combineKLineService.computerCombineKlines(originalKLineList);
        System.out.println("combineKLineList: " + JacksonUtils.toJson(combineKLineList));

        //2.获取所有的高低点
        List<Kline> peakLines = peakKlineService.computerPeakKlines(originalKLineMap, originalKLineList, combineKLineList);
        System.out.println("peakLines: " + JacksonUtils.toJson(combineKLineList));



        /*List<KLine> peakLines = peakKLines(combineKLines);

        System.out.println("peakLine size:" + peakLines.size());
        System.out.println("peakLine:" + JacksonUtils.toJson(peakLines));*/

    }

//    //---------------------------------------KLine高低值获取----------------------------------------
//
//    private List<KLine> peakKLines(List<CombineKLine> combineKLines) {
//
//        //1.获取所有形状线
//        List<ShapeKLine> shapeKLines = getShapeKLines(combineKLines);
//
//        System.out.println("shapeKLines size:" + shapeKLines.size());
//        System.out.println("shapeKLines:" + JacksonUtils.toJson(shapeKLines));
//
//        //2.得到所有的保留形状线
//        List<KLine> reserveKLines = getReserveShapeKLine(combineKLines, shapeKLines);
//        System.out.println("reserveKLines size:" + reserveKLines.size());
//        System.out.println("reserveKLines:" + JacksonUtils.toJson(reserveKLines));
//
//        return reserveKLines;
//
//    }
//
//    private List<KLine> getReserveShapeKLine(List<CombineKLine> combineKLines, List<ShapeKLine> shapeKLines) {
//
//        //1.过滤被保留的线
//        List<ShapeKLine> reserveShapeKLine =
//                shapeKLines.stream().filter(shapeKLine ->
//                        isReserveShapeLine(shapeKLine, combineKLines, shapeKLines)).collect(Collectors.toList());
//
//        System.out.println("reserveShapeKLines size:" + reserveShapeKLine.size());
//        System.out.println("reserveShapeKlines:" + JacksonUtils.toJson(reserveShapeKLine));
//
//        //2.去除相邻同方向的形状线
//        List<ShapeKLine> reserveShapeKLine1 = new ArrayList<>();
//        for(int i = 1; i < reserveShapeKLine.size(); i++) {
//            ShapeKLine prev = reserveShapeKLine.get(i-1);
//            ShapeKLine curr = reserveShapeKLine.get(i);
//            if (curr.getShapeEnum() != prev.getShapeEnum()) {
//                reserveShapeKLine1.add(curr);
//            }
//        }
//        System.out.println("reserveShapeKLines1 size:" + reserveShapeKLine1.size());
//        System.out.println("reserveShapeKlines1:" + JacksonUtils.toJson(reserveShapeKLine1));
//
//        //3.去除被破峰值的形状线
//
//        //Map<Integer, Integer> filterIndex = new HashMap<>(reserveShapeKLine1.size());
//
//        /*List<KLine> brokeKLines = reserveShapeKLine1.stream().filter(shapeKLine -> shapeKLine.isBroke()==true)
//                .map(shapeKLine -> new KLine(shapeKLine.getMiddle())).collect(Collectors.toList());
//
//        System.out.println("isBrokeKLines size:" + brokeKLines.size());
//        System.out.println("isBrokeKLines:" + JacksonUtils.toJson(brokeKLines));*/
//
//        //List<KLine> reserveShapeKLine2 = new ArrayList<>();
//        /*ListIterator<ShapeKLine> reserveShapeKLineIterator = reserveShapeKLine1.listIterator();
//
//        while (reserveShapeKLineIterator.hasNext()) {
//            ShapeKLine currShapeKLine = reserveShapeKLineIterator.next();
//            if (currShapeKLine.isBroke() == true) {
//                KLine curr = currShapeKLine.getMiddle();
//                if (!reserveShapeKLineIterator.hasNext()) break;
//                KLine next = reserveShapeKLineIterator.next().getMiddle();
//                reserveShapeKLineIterator.previous();
//                if (!reserveShapeKLineIterator.hasPrevious()) continue;
//                KLine prev1 = reserveShapeKLineIterator.previous().getMiddle();
//                if (!reserveShapeKLineIterator.hasPrevious()) {
//                    reserveShapeKLineIterator.next();
//                    continue;
//                }
//                KLine prev2 = reserveShapeKLineIterator.previous().getMiddle();
//
//                if (prev1.getHigh() < next.getHigh()
//                        && prev2.getLow() > curr.getLow()) {
//                    reserveShapeKLineIterator.remove();
//                    reserveShapeKLineIterator.next();
//                    reserveShapeKLineIterator.remove();
//                    reserveShapeKLineIterator.next();
//                } else {
//                    reserveShapeKLineIterator.next();
//                    reserveShapeKLineIterator.next();
//                    reserveShapeKLineIterator.next();
//                }
//            }
//        }
//        System.out.println("reserveShapeKLines1 size:" + reserveShapeKLine1.size());
//        System.out.println("reserveShapeKlines1:" + JacksonUtils.toJson(reserveShapeKLine1));*/
//
//        return reserveShapeKLine.stream()
//                .map(shapeKLine -> new KLine(shapeKLine.getMiddle())).collect(Collectors.toList());
//
//    }
//
//    //1.获取所有的形状线
//    private List<ShapeKLine> getShapeKLines(List<CombineKLine> combineKLines) {
//
//        CombineKLine[] combineKLineArray = combineKLines.stream().toArray(CombineKLine[]::new);
//
//        List<ShapeKLine> shapeKLines = new ArrayList<>();
//
//        for (int i=2; i < combineKLineArray.length; i++) {
//
//            CombineKLine left = combineKLineArray[i - 2];
//            CombineKLine middle = combineKLineArray[i - 1];
//            CombineKLine right = combineKLineArray[i];
//
//            LineShapeEnum shapeEnum = getShape(left, middle, right);
//
//            if (shapeEnum != LineShapeEnum.NONE) {
//                ShapeKLine shapeKLine = new ShapeKLine(left, middle, right, shapeEnum, i - 1, shapeKLines.size());
//                shapeKLines.add(shapeKLine);
//            }
//
//        }
//
//        return shapeKLines;
//    }
//
//    private LineShapeEnum getShape(KLine left, KLine middle, KLine right) {
//        if (middle.getHigh() > left.getHigh() && middle.getHigh() > right.getHigh()) {
//            return LineShapeEnum.TOP;
//        } else if (middle.getHigh() < left.getHigh() && middle.getHigh() < right.getHigh()) {
//            return LineShapeEnum.FLOOR;
//        } else {
//            return LineShapeEnum.NONE;
//        }
//    }
//
//    //2.判断Shape线是否保留
//    private Boolean isReserveShapeLine(ShapeKLine shapeKLine, List<CombineKLine> combineKLines, List<ShapeKLine> shapeKLines) {
//
//        if (shapeKLine.getShapeEnum() == LineShapeEnum.NONE) {
//            throw new RuntimeException("LineShape Exception: None!");
//        }
//
//        if (shapeKLine.getShapeEnum() == LineShapeEnum.TOP) {
//            return isReserveTopShapeLine(shapeKLine, shapeKLines, combineKLines);
//        } else {
//            return isReserveFloorShapeLine(shapeKLine, shapeKLines, combineKLines);
//        }
//
//    }
//
//    //3.判断ShapeTop线是否保留
//    private boolean isReserveTopShapeLine(ShapeKLine shapeKLine, List<ShapeKLine> shapeKLines, List<CombineKLine> combineKLines) {
//
//        KLine mid = shapeKLine.getMiddle();
//        KLine left = shapeKLine.getLeft();
//        KLine right = shapeKLine.getRight();
//
//        LineDirectEnum direct = LineDirectEnum.UP;
//
//        Integer max = mid.getHigh();
//        Integer min = Integer.min(left.getLow(), right.getLow());
//
//        int count = 1;
//
//        int rightIndex = shapeKLine.getMiddleIndex() + 1;
//
//        while(direct == LineDirectEnum.UP && rightIndex + count < combineKLines.size()) {
//
//            CombineKLine next = combineKLines.get(rightIndex + count);
//
//            //1.跳空逻辑判断
//            LineGapEnum gapEnum = getGapEnum(shapeKLine, next, max, min);
//
//            if(gapEnum == LineGapEnum.DOWN) {
//                return true;
//            }
//
//            //2.判断是否突破延续
//            if (isBreakContinue(shapeKLine, Integer.max(max, next.getHigh()), Integer.min(min, next.getLow()))) {
//                return false;
//            }
//
//            //3.突破前峰值
//            if (peekBroke(shapeKLine, shapeKLines, combineKLines, count)) {
//                return true;
//            }
//
//            //4.判断趋势变化
//            count++;
//
//            if (count >= 4) {
//                if (min > next.getLow()) {
//                    return true;
//                }
//            }
//
//            min = Integer.min(min, next.getLow());
//            max = Integer.max(max, next.getHigh());
//
//        }
//
//        return false;
//
//    }
//
//    //4.判断ShapeFloor线是否保留
//    private boolean isReserveFloorShapeLine(ShapeKLine shapeKLine, List<ShapeKLine> shapeKLines, List<CombineKLine> combineKLines) {
//
//        KLine mid = shapeKLine.getMiddle();
//        KLine left = shapeKLine.getLeft();
//        KLine right = shapeKLine.getRight();
//
//        LineDirectEnum direct = LineDirectEnum.DOWN;
//
//        Integer max = Integer.max(left.getHigh(), right.getHigh());
//        Integer min = mid.getLow();
//
//        int count = 1;
//
//        int rightIndex = shapeKLine.getMiddleIndex() + 1;
//
//        while(direct == LineDirectEnum.DOWN && rightIndex + count < combineKLines.size()) {
//
//            CombineKLine next = combineKLines.get(rightIndex + count);
//
//            //1.跳空逻辑判断
//            LineGapEnum gapEnum = getGapEnum(shapeKLine, next, max, min);
//
//            if(gapEnum == LineGapEnum.UP) {
//                return true;
//            }
//
//            //2.判断是否突破延续
//            if (isBreakContinue(shapeKLine, Integer.max(max, next.getHigh()), Integer.min(min, next.getLow()))) {
//                return false;
//            }
//
//            //3.突破前峰值
//            if (peekBroke(shapeKLine, shapeKLines, combineKLines, count)) {
//                return true;
//            }
//
//            //4.判断趋势变化
//            count++;
//
//            if (count >= 4) {
//                if (max < next.getHigh()) {
//                    return true;
//                }
//            }
//
//            min = Integer.min(min, next.getLow());
//            max = Integer.max(max, next.getHigh());
//
//        }
//
//        return false;
//
//    }
//
//
//    //5.判断跳空类型
//    private LineGapEnum getGapEnum(ShapeKLine shapeKLine, CombineKLine combineKLine, Integer max, Integer min) {
//
//        if (shapeKLine.getShapeEnum() == LineShapeEnum.NONE) {
//            throw new IllegalArgumentException();
//        }
//
//        if (shapeKLine.getShapeEnum() == LineShapeEnum.TOP) {
//
//            for(KLine kLine: combineKLine.getCombineKLineList()) {
//                if (kLine.getHigh() < min) {
//                    return LineGapEnum.DOWN;
//                }
//                min = Math.min(min, kLine.getLow());
//            }
//
//        } else {
//
//            for(KLine kLine: combineKLine.getCombineKLineList()) {
//                if (kLine.getLow() > max) {
//                    return LineGapEnum.UP;
//                }
//                max = Math.max(max, kLine.getHigh());
//            }
//
//        }
//
//        return LineGapEnum.NONE;
//
//    }
//
//    //6.峰值突破
//    private boolean peekBroke(ShapeKLine shapeKLine, List<ShapeKLine> shapeKLineList, List<CombineKLine> combineKLines, int count) {
//
//        int rightIndex = shapeKLine.getMiddleIndex() + 1;
//
//        if (shapeKLine.getShapeLineIndex() > 0 && count < 4) {
//            CombineKLine currKLine = combineKLines.get(rightIndex + count - 1);
//
//            ShapeKLine prevEqualShapeKLine = null;
//            for (int i = shapeKLine.getShapeLineIndex() - 1; i >= 0; i--) {
//                if (shapeKLineList.get(i).getShapeEnum() == shapeKLine.getShapeEnum()) {
//                    prevEqualShapeKLine = shapeKLineList.get(i);
//                    break;
//                }
//            }
//
//            if (prevEqualShapeKLine != null) {
//
//                if (shapeKLine.getShapeEnum() == LineShapeEnum.FLOOR) {
//                    if (currKLine.getHigh() > prevEqualShapeKLine.getMiddle().getHigh()) {
//                        shapeKLine.setBroke(true);
//                        return true;
//                    }
//                }
//
//                if (shapeKLine.getShapeEnum() == LineShapeEnum.TOP){
//                    if (currKLine.getLow() < prevEqualShapeKLine.getMiddle().getLow()) {
//                        shapeKLine.setBroke(true);
//                        return true;
//                    }
//                }
//
//            }
//
//        }
//
//        return false;
//    }
//
//    //7.突破延续
//    private boolean isBreakContinue(ShapeKLine shapeKLine, Integer max, Integer min) {
//
//        if (shapeKLine.getShapeEnum() == LineShapeEnum.NONE) {
//            throw new IllegalArgumentException();
//        }
//
//        if (shapeKLine.getShapeEnum() == LineShapeEnum.TOP) {
//            if (max > shapeKLine.getMiddle().getHigh()) {
//                return true;
//            }
//        } else {
//            if (min < shapeKLine.getMiddle().getLow()) {
//                return true;
//            }
//        }
//
//        return false;
//
//    }


    @Override
    public void afterPropertiesSet() throws Exception {
        File file = new File(KLineDrawService.class.getResource("/sz300181.day").getFile());
        //File file = new File(KLineDrawService.class.getResource("/sh600530.day").getFile());
        //File file = new File(KLineDrawService.class.getResource("/RU1909.day").getFile());
        InputStream inputStream = new FileInputStream(file);
        byte[] bytes = new byte[32];
        while(inputStream.read(bytes) != -1) {
            Kline kLine = new Kline(bytes, 300181);
            originalKLineList.add(kLine);
            originalKLineMap.put(kLine.getDate(), kLine);
        }
        compute();
    }
}
