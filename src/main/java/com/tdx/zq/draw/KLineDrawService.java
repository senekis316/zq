package com.tdx.zq.draw;

import com.tdx.zq.context.KlineApplicationContext;
import com.tdx.zq.enums.KlineType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.*;
import com.tdx.zq.draw.MatrixKlineProcessor.BSPoint;

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
        for (File directory: directories) {
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                for (File file : files) {
                    if (!file.getName().contains(".DS_Store")) {
                        String klineCode = file.getName().split("\\.")[0].replace("#", "");
                        bsPointMap.put(klineCode, new HashMap<>());
                        enhanceMap.put(klineCode, new HashMap<>());
                        List<KlineType> klineTypes = KlineType.getKlineType(directory.getName());
                        for (KlineType klineType : klineTypes) {
                            new KlineApplicationContext(file, klineType, bsPointMap, enhanceMap);
                        }
                    }
                }
            }
        }

//        for (File directory: directories) {
//            if (directory.isDirectory()) {
//                File[] files = directory.listFiles();
//                for (File file: files) {
//                    List<KlineType> klineTypes = KlineType.getKlineType(directory.getName());
//                    for (KlineType klineType : klineTypes) {
//                        new KlineApplicationContext(file, klineType, bsPointMap, enhanceMap);
//                    }
//                }
//            }
//        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Map<KlineType, PriorityQueue<BSPoint>>> entry : bsPointMap.entrySet()) {
            sb.append("----------" + entry.getKey() + "----------\n");
            sb.append("日,小时,十分,周,分钟,月\n");
            Map<KlineType, PriorityQueue<BSPoint>> klinePointMap = entry.getValue();
            List<PriorityQueue<BSPoint>> priorityQueueList = new ArrayList<>();
            priorityQueueList.add(klinePointMap.get(KlineType.DAY_LINE));
            priorityQueueList.add(klinePointMap.get(KlineType.HOUR_LINE));
            priorityQueueList.add(klinePointMap.get(KlineType.TEN_MINUTES_LINE));
            priorityQueueList.add(klinePointMap.get(KlineType.WEEK_LINE));
            priorityQueueList.add(klinePointMap.get(KlineType.ONE_MINUTES_LINE));
            priorityQueueList.add(klinePointMap.get(KlineType.MONTH_LINE));

            List<String> enhanceList = new ArrayList<>();
            enhanceList.add(enhanceMap.get(entry.getKey()).get(KlineType.DAY_LINE));
            enhanceList.add(enhanceMap.get(entry.getKey()).get(KlineType.HOUR_LINE));
            enhanceList.add(enhanceMap.get(entry.getKey()).get(KlineType.TEN_MINUTES_LINE));
            enhanceList.add(enhanceMap.get(entry.getKey()).get(KlineType.WEEK_LINE));
            enhanceList.add(enhanceMap.get(entry.getKey()).get(KlineType.ONE_MINUTES_LINE));
            enhanceList.add(enhanceMap.get(entry.getKey()).get(KlineType.MONTH_LINE));

            Iterator<String> enhanceIterator = enhanceList.iterator();

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
            sb.append("----------------------------------------\n");
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
