package com.tdx.zq.draw;

import com.tdx.zq.model.CombineKline;
import com.tdx.zq.model.Kline;
import com.tdx.zq.utils.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;


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

    }


    @Override
    public void afterPropertiesSet() throws Exception {

        File file = new File(KLineDrawService.class.getResource("/SZ300181.txt").getFile());
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> klineStrList = bufferedReader.lines().collect(Collectors.toList());//.skip(2).limit(bufferedReader.lines().count() - 3).collect(Collectors.toList());
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
