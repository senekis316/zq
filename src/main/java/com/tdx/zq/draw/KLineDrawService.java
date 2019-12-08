package com.tdx.zq.draw;

import com.tdx.zq.enums.LineShapeEnum;
import com.tdx.zq.model.*;
import com.tdx.zq.utils.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class KLineDrawService implements InitializingBean {

    private List<Kline> originalKLineList = new LinkedList<>();

    private Map<Integer, Kline> originalKLineMap = new HashMap<>();

    @Autowired
    private PeakKlineService peakKlineService;

    @Autowired
    private MatrixLineService matrixLineService;

    @Autowired
    private CombineKLineService combineKLineService;


    public void compute() throws IOException {

        //1.合并所有的KLines
        List<CombineKline> combineKLineList = combineKLineService.computerCombineKlines(originalKLineList);
        System.out.println("combineKLineList: " + JacksonUtils.toJson(combineKLineList));

        //2.获取所有的高低点
        List<PeakKline> peakLineList = peakKlineService.computerPeakKlines(combineKLineList, originalKLineList);
        System.out.println("peakLineList: " + JacksonUtils.toJson(peakLineList));

        //3.获取矩形信息
        List<Matrix> matrixLineList = matrixLineService.drawMatrix(peakLineList);
        System.out.println("matrixLineList" + JacksonUtils.toJson(matrixLineList));

        //4.保存画线信息
        //drawKline(peakLines);

    }

    private boolean drawKline(List<PeakKline> peakKlines) throws IOException {
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
    }

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
        //File file = new File(KLineDrawService.class.getResource("/SZ300181.txt").getFile());
        //File file = new File(KLineDrawService.class.getResource("/SZ300136.txt").getFile());
        //File file = new File(KLineDrawService.class.getResource("/SZ300202.txt").getFile());
        File file = new File(KLineDrawService.class.getResource("/SZ300277.txt").getFile());
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> klineStrList = bufferedReader.lines().collect(Collectors.toList());
        klineStrList.remove(klineStrList.size() - 1);
        klineStrList.remove(0);
        klineStrList.remove(0);
        List<Kline> klineList = klineStrList.stream().map(str -> new Kline(str)).collect(Collectors.toList());
        for(Kline kline : klineList) {
            originalKLineList.add(kline);
            originalKLineMap.put(kline.getDate(), kline);
        }
        compute();

        /*File file = new File(KLineDrawService.class.getResource("/sz300181_2.day").getFile());
        //File file = new File(KLineDrawService.class.getResource("/sh600530.day").getFile());
        //File file = new File(KLineDrawService.class.getResource("/RU1909.day").getFile());
        InputStream inputStream = new FileInputStream(file);
        byte[] bytes = new byte[32];
        while(inputStream.read(bytes) != -1) {
            Kline kLine = new Kline(bytes, 300181);
            originalKLineList.add(kLine);
            originalKLineMap.put(kLine.getDate(), kLine);
        }
        compute();*/
    }




}
