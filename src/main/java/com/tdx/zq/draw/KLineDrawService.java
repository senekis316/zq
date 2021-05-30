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


@Slf4j
@Service
public class KLineDrawService implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws IOException, ParseException {
        String inputDirectory = "F:\\kline\\input";
        String outputDirectory = "F:\\kline\\output";
        File[] directories = new File(inputDirectory).listFiles();

        Map<String, Map<KlineType, PriorityQueue<BSPoint>>> bsPointMap = new HashMap<>();
        for (File directory: directories) {
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                for (File file : files) {
                    String klineCode = file.getName().split("\\.")[0].replace("#", "");;
                    if (bsPointMap.get(klineCode) == null) {
                        bsPointMap.put(klineCode, new HashMap<>());
                    }
                }
            }
        }

        for (File directory: directories) {
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                for (File file: files) {
                    List<KlineType> klineTypes = KlineType.getKlineType(directory.getName());
                    for (KlineType klineType : klineTypes) {
                        new KlineApplicationContext(file, klineType, bsPointMap);
                        //new KlineApplicationContext(file, klineType, outputPath, bsPointMap);
                    }
                }
            }
        }

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

            boolean hasValue = true;
            while (hasValue) {
                int sum = 0;
                for (PriorityQueue<BSPoint> priorityQueue : priorityQueueList) {
                    if (CollectionUtils.isNotEmpty(priorityQueue)) {
                        sb.append(priorityQueue.poll().toString()).append(",");
                    } else {
                        sb.append(",");
                    }
                    sum += priorityQueue.size();
                }
                sb.append("\n");
                hasValue = sum > 0;
            }
            sb.append("----------------------------------------\n");
        }

        String outputPath = outputDirectory + File.separator + "result.csv";
                //+ File.separator + file.getName().split("\\.")[0] + ".txt";
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
//                new KlineApplicationContext("src/main/resources/SZ300181_1.txt",
//                        KlineType.ONE_MINUTES_LINE, "src/main/resources/SZ300181_1.xlsx");
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
