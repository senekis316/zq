package com.tdx.zq.draw;

import com.tdx.zq.context.KlineApplicationContext;
import com.tdx.zq.enums.KlineType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import com.tdx.zq.enums.PointType;
import com.tdx.zq.model.BSPoint;
import com.tdx.zq.utils.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Slf4j
@Service
public class KLineDrawService implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws IOException, ParseException {

        InputStreamReader input = new InputStreamReader(System.in);
        //Taking the input data using the BufferedReader class
        BufferedReader reader = new BufferedReader(input);
        // Reading data using readLine
        System.out.print("请输入周期类型(D,W,M,H,T,O): ");
        String[] periods = reader.readLine().replace(" ", "").split("");

        // Printing the read line
        System.out.print("请输入点类型(B1,B2,B3,S1,S2,S3): ");
        String[] points = reader.readLine().split(" ");

        String inputDirectory;
        String outputDirectory;
        if (!System.getProperty("os.name").contains("Mac OS X")) {
            inputDirectory = "F:\\kline\\input";
            outputDirectory = "F:\\kline\\output";
        } else {
            inputDirectory = "/Users/yufangxing/Downloads/zq/input";
            outputDirectory = "/Users/yufangxing/Downloads/zq/output";
        }

        File[] directories = new File(inputDirectory).listFiles();

        Map<String, Map<KlineType, PriorityQueue<BSPoint>>> bsPointMap = new HashMap<>();
        Map<String, Map<KlineType, String>> enhanceMap = new HashMap<>();
        Map<String, Map<KlineType, String>> matrixThresholdBreakMap = new HashMap<>();
        for (File directory: directories) {
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                for (File file : files) {
                    if (!file.getName().contains(".DS_Store")) {
                        String klineCode = file.getName().split("\\.")[0].replace("#", "");
                        bsPointMap.put(klineCode, new HashMap<>());
                        enhanceMap.put(klineCode, new HashMap<>());
                        matrixThresholdBreakMap.put(klineCode, new HashMap<>());
                    }
                }
            }
        }

        for (File directory: directories) {
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                for (File file: files) {
                    if (!file.getName().contains(".DS_Store")) {
                        List<KlineType> klineTypes = KlineType.getKlineType(directory.getName());
                        for (KlineType klineType : klineTypes) {
                            new KlineApplicationContext(file, klineType, bsPointMap, enhanceMap, matrixThresholdBreakMap);
                        }
                    }
                }
            }
        }

        boolean permit = true;
        StringBuilder sb = new StringBuilder();

        Map<KlineType, Set<String>> klineCodes = new HashMap<>();
        klineCodes.put(KlineType.DAY_LINE, new HashSet<>());
        klineCodes.put(KlineType.HOUR_LINE, new HashSet<>());
        klineCodes.put(KlineType.TEN_MINUTES_LINE, new HashSet<>());
        klineCodes.put(KlineType.WEEK_LINE, new HashSet<>());
        klineCodes.put(KlineType.ONE_MINUTES_LINE, new HashSet<>());
        klineCodes.put(KlineType.MONTH_LINE, new HashSet<>());


        for (Map.Entry<String, Map<KlineType, PriorityQueue<BSPoint>>> entry : bsPointMap.entrySet()) {

            Map<KlineType, PriorityQueue<BSPoint>> klinePointMap = entry.getValue();
            List<PriorityQueue<BSPoint>> priorityQueueList = new ArrayList<>();
            priorityQueueList.add(klinePointMap.get(KlineType.DAY_LINE));
            priorityQueueList.add(klinePointMap.get(KlineType.HOUR_LINE));
            priorityQueueList.add(klinePointMap.get(KlineType.TEN_MINUTES_LINE));
            priorityQueueList.add(klinePointMap.get(KlineType.WEEK_LINE));
            priorityQueueList.add(klinePointMap.get(KlineType.ONE_MINUTES_LINE));
            priorityQueueList.add(klinePointMap.get(KlineType.MONTH_LINE));

            for (BSPoint bsPoint : klinePointMap.getOrDefault(KlineType.DAY_LINE, new PriorityQueue<>())) {
                if (bsPoint.getMatrixs().size() >= 2) {
                    klineCodes.get(KlineType.DAY_LINE).add(entry.getKey());
                }
            }

            for (BSPoint bsPoint : klinePointMap.getOrDefault(KlineType.HOUR_LINE, new PriorityQueue<>())) {
                if (bsPoint.getMatrixs().size() >= 2) {
                    klineCodes.get(KlineType.HOUR_LINE).add(entry.getKey());
                }
            }

            for (BSPoint bsPoint : klinePointMap.getOrDefault(KlineType.TEN_MINUTES_LINE, new PriorityQueue<>())) {
                if (bsPoint.getMatrixs().size() >= 2) {
                    klineCodes.get(KlineType.TEN_MINUTES_LINE).add(entry.getKey());
                }
            }

            for (BSPoint bsPoint : klinePointMap.getOrDefault(KlineType.WEEK_LINE, new PriorityQueue<>())) {
                if (bsPoint.getMatrixs().size() >= 2) {
                    klineCodes.get(KlineType.WEEK_LINE).add(entry.getKey());
                }
            }

            for (BSPoint bsPoint : klinePointMap.getOrDefault(KlineType.ONE_MINUTES_LINE, new PriorityQueue<>())) {
                if (bsPoint.getMatrixs().size() >= 2) {
                    klineCodes.get(KlineType.ONE_MINUTES_LINE).add(entry.getKey());
                }
            }

            for (BSPoint bsPoint : klinePointMap.getOrDefault(KlineType.MONTH_LINE, new PriorityQueue<>())) {
                if (bsPoint.getMatrixs().size() >= 2) {
                    klineCodes.get(KlineType.MONTH_LINE).add(entry.getKey());
                }
            }

            List<String> enhanceList = new ArrayList<>();
            enhanceList.add(enhanceMap.get(entry.getKey()).get(KlineType.DAY_LINE));
            enhanceList.add(enhanceMap.get(entry.getKey()).get(KlineType.HOUR_LINE));
            enhanceList.add(enhanceMap.get(entry.getKey()).get(KlineType.TEN_MINUTES_LINE));
            enhanceList.add(enhanceMap.get(entry.getKey()).get(KlineType.WEEK_LINE));
            enhanceList.add(enhanceMap.get(entry.getKey()).get(KlineType.ONE_MINUTES_LINE));
            enhanceList.add(enhanceMap.get(entry.getKey()).get(KlineType.MONTH_LINE));

            List<String> thresholdList = new ArrayList<>();
            thresholdList.add(matrixThresholdBreakMap.get(entry.getKey()).get(KlineType.DAY_LINE));
            thresholdList.add(matrixThresholdBreakMap.get(entry.getKey()).get(KlineType.HOUR_LINE));
            thresholdList.add(matrixThresholdBreakMap.get(entry.getKey()).get(KlineType.TEN_MINUTES_LINE));
            thresholdList.add(matrixThresholdBreakMap.get(entry.getKey()).get(KlineType.WEEK_LINE));
            thresholdList.add(matrixThresholdBreakMap.get(entry.getKey()).get(KlineType.ONE_MINUTES_LINE));
            thresholdList.add(matrixThresholdBreakMap.get(entry.getKey()).get(KlineType.MONTH_LINE));

            Iterator<String> enhanceIterator = enhanceList.iterator();

            if (!StringUtils.isEmpty(periods[0])) {
                List<PriorityQueue<BSPoint>> queryPointList = new ArrayList<>();
                queryPointList.add(klinePointMap.get(KlineType.DAY_LINE));
                queryPointList.add(klinePointMap.get(KlineType.HOUR_LINE));
                queryPointList.add(klinePointMap.get(KlineType.TEN_MINUTES_LINE));
                queryPointList.add(klinePointMap.get(KlineType.WEEK_LINE));
                queryPointList.add(klinePointMap.get(KlineType.ONE_MINUTES_LINE));
                queryPointList.add(klinePointMap.get(KlineType.MONTH_LINE));

                List<PointType> pointTypes = new ArrayList<>();
                for (String point : points) {
                    pointTypes.add(PointType.create(point));
                }

                List<KlineType> periodTypes = new ArrayList<>();
                for (String period : periods) {
                    periodTypes.add(KlineType.create(period));
                }

                PriorityQueue<BSPoint> bsPoints = new PriorityQueue<>((o1, o2) -> {
                    if (o1.getDate() > o2.getDate()) {
                        return -1;
                    } else if (o1.getDate() < o2.getDate()) {
                        return 1;
                    } else {
                        return 0;
                    }
                });

                for (int i = 0; i < periodTypes.size(); i++) {
                    if (klinePointMap.get(periodTypes.get(i)) == null) continue;
                    bsPoints.addAll(klinePointMap.get(periodTypes.get(i)));
                    if (bsPoints.size() == 0) continue;
                    boolean isExist = false;
                    for (int j = 0; j < 3; j++) {
                        BSPoint bsPoint = bsPoints.poll();
                        if (bsPoint != null && bsPoint.getPointType() == pointTypes.get(i)) {
                            isExist = true;
                        }
                    }
                    if (!isExist) {
                        permit = false;
                        break;
                    }
                    bsPoints.clear();
                }

            }

            if (!permit) {
                continue;
            }

            sb.append("----------" + entry.getKey() + "----------\n");
            sb.append("日,小时,十分,周,分钟,月\n");

            boolean hasValue = true;
            while (hasValue) {
                int sum = 0;
                for (PriorityQueue<BSPoint> priorityQueue : priorityQueueList) {
                    if (CollectionUtils.isNotEmpty(priorityQueue)) {
                        sb.append(priorityQueue.poll().toString()).append(",");
                    } else {
                        sb.append(",");
                    }
                    sum += priorityQueue == null ? 0 : priorityQueue.size();
                }
                sb.append("\n");
                hasValue = sum > 0;
            }
            while (enhanceIterator.hasNext()) {
                String enhance = enhanceIterator.next();
                if (!StringUtils.isEmpty(enhance)) {
                    sb.append(enhance);
                }
                if (enhanceIterator.hasNext()) {
                    sb.append(",");
                }
            }
            sb.append("\n");

            Iterator<String> thresholdIterator = thresholdList.iterator();
            while (thresholdIterator.hasNext()) {
                String threshold = thresholdIterator.next();
                if (!StringUtils.isEmpty(threshold)) {
                    sb.append(threshold);
                }
                if (thresholdIterator.hasNext()) {
                    sb.append(",");
                }
            }

            if (!StringUtils.isEmpty(periods[0])) {
                sb.append("\n");
                sb.append("输入的点类型: " + JacksonUtils.toJson(points).replace(",", ":") + ";");
                sb.append("输入的周期类型: " + JacksonUtils.toJson(periods).replace(",", ":") + ";");
                if (permit) {
                    sb.append(entry.getKey() + "符合要求;");
                } else {
                    sb.append(entry.getKey() + "不符合要求;");
                }
            }

            sb.append("\n");
            sb.append("----------------------------------------\n");
        }

        if (klineCodes != null) {
            if (klineCodes.entrySet().stream().count() > 0) {
                sb.append("\n");
                sb.append("矩阵数量超过两个以上股票编码: \n");
                if (klineCodes.get(KlineType.DAY_LINE).size() > 0) {
                    sb.append("D,");
                    sb.append(klineCodes.get(KlineType.DAY_LINE).stream().collect(Collectors.joining(";")));
                }
                if (klineCodes.get(KlineType.HOUR_LINE).size() > 0) {
                    sb.append("H,");
                    sb.append(klineCodes.get(KlineType.HOUR_LINE).stream().collect(Collectors.joining(";")));
                }
                if (klineCodes.get(KlineType.TEN_MINUTES_LINE).size() > 0) {
                    sb.append("T,");
                    sb.append(klineCodes.get(KlineType.TEN_MINUTES_LINE).stream().collect(Collectors.joining(";")));
                }
                if (klineCodes.get(KlineType.WEEK_LINE).size() > 0) {
                    sb.append("W,");
                    sb.append(klineCodes.get(KlineType.WEEK_LINE).stream().collect(Collectors.joining(";")));
                }
                if (klineCodes.get(KlineType.ONE_MINUTES_LINE).size() > 0) {
                    sb.append("O,");
                    sb.append(klineCodes.get(KlineType.ONE_MINUTES_LINE).stream().collect(Collectors.joining(";")));
                }
                if (klineCodes.get(KlineType.MONTH_LINE).size() > 0) {
                    sb.append("M,");
                    sb.append(klineCodes.get(KlineType.MONTH_LINE).stream().collect(Collectors.joining(";")));
                }
                sb.append("\n");
            }
        }

        String outputPath = outputDirectory + File.separator + "result.csv";
        log.info(outputPath);

        File file = new File(outputPath);
        try (OutputStream fos = new FileOutputStream(file)) {
            fos.write(new byte[] { (byte) 0xEF, (byte) 0xBB,(byte) 0xBF});
            fos.write(sb.toString().getBytes("utf8"));
        }

        log.info("结束！！！！");
        System.exit(0);

        //SZ300181 佐力药业	STANDK(K线)	日线	线段	定位1:值:4.67/时:20190806;定位2:值:5.84/时:20190910;
//        KlineApplicationContext klineApplicationContext =
//            new KlineApplicationContext("/SZ300181_5.txt", KlineType.HOUR_LINE);
//        KlineApplicationContext klineApplicationContext =
//            new KlineApplicationContext("/30RBL9.txt", KlineType.DAY_LINE);
//        KlineApplicationContext klineApplicationContext =
//            new KlineApplicationContext("/SZ300181.txt", KlineType.DAY_LINE);
//        KlineApplicationContext klineApplicationContext =
//            new KlineApplicationContext("src/main/resources/SZ002157.txt",
//                KlineType.DAY_LINE, "src/main/resources/SZ002157.xlsx");
//        KlineApplicationContext klineApplicationContext =
//                new KlineApplicationContext("src/main/resources/SZ300663_D.txt",
//                        KlineType.DAY_LINE, new HashMap<>());
//        klineApplicationContext.printMergeKlineList();
//        klineApplicationContext.printPeakKlineList();
//        klineApplicationContext.printBreakPeakKlineList();
//        klineApplicationContext.printJumpPeakKlineList();
//        klineApplicationContext.printTendencyPeakKlineList();
//        klineApplicationContext.printOppositeTendencyPeakKlineList();
//        klineApplicationContext.printInDependentTendencyPeakKlineList();
//        klineApplicationContext.printAnglePeakKlineList();
//        System.exit(0);
    }

}
