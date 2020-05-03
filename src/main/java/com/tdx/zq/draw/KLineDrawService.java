package com.tdx.zq.draw;

import com.tdx.zq.context.KlineApplicationContext;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class KLineDrawService implements InitializingBean {

//    @Autowired
//    private PeakKlineService peakKlineService;

//    @Autowired
//    private MatrixLineService matrixLineService;

    private KlineApplicationContext klineApplicationContext;


    public void compute() throws IOException {

        //1.合并所有的KLines
        //CombineKlineProcessor combineKlineHandler = new CombineKlineProcessor(klineApplicationContext);
        //List<CombineKline> combineKLineList = combineKlineHandler.computerCombineKlines();
        /*List<MergeKline> mergeKlineList = klineApplicationContext.getMergeKlineList();
        System.out.println("mergeKLineList: " +
            JacksonUtils.toJson(mergeKlineList.stream().map(MergeKline::getMergeKline).collect(Collectors.toList())));

        //2.获取所有的高低点
        List<PeakKline> peakLineList = peakKlineService.computerPeakKlines(combineKLineList, klineApplicationContext);
        //System.out.println("peakLineList: " + JacksonUtils.toJson(peakLineList));

        //3.获取矩形信息
        //List<Matrix> matrixLineList = matrixLineService.drawMatrix(peakLineList);
        //System.out.println("matrixLineList" + JacksonUtils.toJson(matrixLineList));*/

        //4.保存画线信息
        //drawKline(peakLines);*/

    }

    /*private boolean drawKline(List<PeakKline> peakKlines) throws IOException {
        return drawLineToFile("E:\\", "drawLines.txt", drawKlineToStr(peakKlines));
    }

    private List<String> drawKlineToStr(List<PeakKline> peakKlines) {
        //定位1:值:4.67/时:20190806;定位2:值:5.84/时:20190910;
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < peakKlines.size() - 1; i++) {
            PeakKline peakKline1 = peakKlines.get(i);
            PeakKline peakKline2 = peakKlines.get(i + 1);
            Kline kline1 = peakKline1.getCombineKline().getKline();
            Kline kline2 = peakKline2.getCombineKline().getKline();
            StringBuilder endfix = new StringBuilder("SZ300181 佐力药业\tSTANDK(K线)\t日线\t线段\t");
            if (peakKline1.getShapeType() == LineShapeEnum.TOP) {
                endfix.append("定位1:值:" + kline1.getHigh() + "/时:" + kline1.getDate() + ";");
                endfix.append("定位2:值:" + kline2.getLow() + "/时:" + kline2.getDate() + ";");
            } else {
                endfix.append("定位1:值:" + kline1.getLow() + "/时:" + kline1.getDate() + ";");
                endfix.append("定位2:值:" + kline2.getHigh() + "/时:" + kline2.getDate() + ";");
            }
            lines.add(endfix.toString());
        }

        return lines;
    }*/

    private boolean drawLineToFile(String filePath, String fileName, List<String> drawLines) throws IOException {

        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();// mkdirs创建多级目录
        }
        File checkFile = new File(filePath + File.separator +  File.separator + fileName);

        if (!checkFile.exists()) {
            checkFile.createNewFile();// 创建目标文件
        }

        StringBuilder drawLineAllStr = new StringBuilder();
        for (String drawLine : drawLines) {
            drawLineAllStr.append(drawLine + "\r\n");
        }
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filePath + fileName))){
            IOUtils.write(drawLineAllStr.toString(), out, Charset.forName("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        //SZ300181 佐力药业	STANDK(K线)	日线	线段	定位1:值:4.67/时:20190806;定位2:值:5.84/时:20190910;
        KlineApplicationContext klineApplicationContext = new KlineApplicationContext("/SZ300181.txt");
        klineApplicationContext.printMergeKlineList();
        klineApplicationContext.printPeakKlineList();
        klineApplicationContext.printBreakPeakKlineList();
        klineApplicationContext.printJumpPeakKlineList();


    }

}
