package com.tdx.zq.draw;

import com.tdx.zq.context.KlineApplicationContext;
import com.tdx.zq.enums.KlineType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.DayOfWeek;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class KLineDrawService implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws IOException {
        String inputDirectory = "F:\\kline\\input";
        String outputDirectory = "F:\\kline\\output";
        File[] directories = new File(inputDirectory).listFiles();
        for (File directory: directories) {
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                for (File file: files) {
                    List<KlineType> klineTypes = KlineType.getKlineType(directory.getName());
                    for (KlineType klineType : klineTypes) {
                        String outputPath = outputDirectory + File.separator + klineType.getOutputDirectory() + File.separator + file.getName().split("\\.")[0] + ".xlsx";
                        new KlineApplicationContext(file, klineType, outputPath);
                        log.info(outputPath);
                    }
                }
            }
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
//            new KlineApplicationContext("src/main/resources/SZ002970.txt",
//                KlineType.DAY_LINE, "src/main/resources/SZ002970.xlsx");
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
