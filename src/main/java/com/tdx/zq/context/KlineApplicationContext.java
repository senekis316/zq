package com.tdx.zq.context;

import com.tdx.zq.draw.KLineDrawService;
import com.tdx.zq.draw.MergeKlineProcessor;
import com.tdx.zq.draw.PeakKlineProcessor;
import com.tdx.zq.model.Kline;
import com.tdx.zq.model.MergeKline;
import com.tdx.zq.model.PeakKline;
import com.tdx.zq.tuple.ThreeTuple;
import com.tdx.zq.tuple.TwoTuple;
import com.tdx.zq.utils.JacksonUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KlineApplicationContext {

  private List<Kline> klineList;
  private Map<Integer, Kline> klineMap;
  private List<MergeKline> mergeKlineList;
  private List<PeakKline> peakKlineList;

  public KlineApplicationContext(String path) throws FileNotFoundException {
    setKlineList(path);
    setKlineMap(klineList);
    setMergeKlineList(klineList);
    setPeakKlineList();
  }

  private void setKlineList(String path) throws FileNotFoundException {

    File file = new File(KLineDrawService.class.getResource(path).getFile());
    FileReader fileReader = new FileReader(file);
    BufferedReader bufferedReader = new BufferedReader(fileReader);

    List<String> klineStrList = bufferedReader.lines().collect(Collectors.toList());
    klineStrList.remove(klineStrList.size() - 1);
    klineStrList.remove(0);
    klineStrList.remove(0);

    LinkedList<Kline> klineList = new LinkedList();
    for (String klineStr : klineStrList) {
      klineList.add(new Kline(klineStr));
    }
    this.klineList = klineList;
  }

  private void setKlineMap(List<Kline> klineList) {
    Map<Integer, Kline> klineMap = new HashMap<>();
    for(Kline kline : klineList) {
      klineMap.put(kline.getDate(), kline);
    }
    this.klineMap = klineMap;
  }

  public void setMergeKlineList(List<Kline> klineList) {
    this.mergeKlineList = new MergeKlineProcessor(klineList).getMergeKlineList();
  }

  private void setPeakKlineList() {
    this.peakKlineList = new PeakKlineProcessor(this).getPeakKlineList();
  }

  public List<Kline> getKlineList() {
    return klineList;
  }

  public Map<Integer, Kline> getKlineMap() {
    return klineMap;
  }


  public List<MergeKline> getMergeKlineList() {
    return mergeKlineList;
  }

  public void printMergeKlineList() {
    System.out.println("mergeKlineList: " + JacksonUtils.toJson(
        mergeKlineList.stream().map(MergeKline::getMergeKline).collect(Collectors.toList())));
  }

  public void printPeakKlineList() {
    System.out.println("peakKlineList: " + JacksonUtils.toJson(
        peakKlineList.stream().map(peakKline ->
            new TwoTuple(peakKline.getPeakShape(), peakKline.getMergeKline().getMergeKline()))
                .collect(Collectors.toList())));
  }

  public void printTwoThreeBreakPeakKlineList() {
    System.out.println("twoThreeBeakPeakKlineList: " + JacksonUtils.toJson(
        peakKlineList.stream().filter(peakKline -> peakKline.isTwoTreeBreakPeak())
          .map(peakKline -> new TwoTuple(peakKline.getPeakShape(), peakKline.getMergeKline().getMergeKline()))
          .collect(Collectors.toList())));
  }

}
