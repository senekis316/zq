package com.tdx.zq.context;

import com.tdx.zq.draw.KLineDrawService;
import com.tdx.zq.draw.MatrixKlineProcessor;
import com.tdx.zq.draw.MergeKlineProcessor;
import com.tdx.zq.draw.PeakKlineProcessor;
import com.tdx.zq.draw.PeakKlineProcessor.*;
import com.tdx.zq.enums.KlineType;
import com.tdx.zq.model.Kline;
import com.tdx.zq.model.MergeKline;
import com.tdx.zq.model.PeakKline;
import com.tdx.zq.model.PeakKlinePrint;
import com.tdx.zq.utils.JacksonUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

@Getter
@Slf4j
public class KlineApplicationContext {

  private String outputPath;
  private KlineType klineType;
  private List<Kline> klineList;
  private Map<Long, Kline> klineMap;
  private List<MergeKline> mergeKlineList;
  private List<PeakKline> peakKlineList;
  private List<PeakKline> breakPeakKlineList;
  private List<PeakKline> jumpPeakKlineList;
  private List<PeakKline> turnPeakKlineList;
  private List<MatrixKlineRow> matrixKlineRowList;

  public KlineApplicationContext(String path, KlineType klineType, String outputPath) throws IOException {
    this.outputPath = outputPath;
    File file = new File(path);
    setKlineList(file, klineType);
    setKlineMap(klineList);
    setMergeKlineList(klineList);
    setPeakKlineList();
    setMatrixKlineList();
  }

  public KlineApplicationContext(File file, KlineType klineType, String outputPath) throws IOException {
    this.outputPath = outputPath;
    setKlineList(file, klineType);
    setKlineMap(klineList);
    setMergeKlineList(klineList);
    setPeakKlineList();
  }

  private void setKlineList(File file, KlineType klineType) throws IOException {

    FileReader fileReader = new FileReader(file, Charset.forName("GBK"));
    BufferedReader bufferedReader = new BufferedReader(fileReader);

    List<String> klineStrList = bufferedReader.lines().collect(Collectors.toList());
    if (klineStrList.size() == 3) return;
    if (klineType == KlineType.HOUR_LINE && klineStrList.size() < 15);
    if (klineType == KlineType.TEN_MINUTES_LINE && klineStrList.size() < 5);
    klineStrList.remove(klineStrList.size() - 1);
    klineStrList.remove(0);
    boolean hasTime = klineStrList.get(0).split("\t")[1].trim().equals("时间");
    klineStrList.remove(0);

    LinkedList<Kline> klineList = new LinkedList();

    if (klineType == KlineType.HOUR_LINE) {
      List<String> hourKlineList = new ArrayList<>();
      for (int i = 0; i < klineStrList.size(); i++) {
        hourKlineList.add(klineStrList.get(i));
        if (hourKlineList.size() == 12) {
          try {
            klineList.add(new Kline(hourKlineList, hasTime, klineList.size()));
            hourKlineList.clear();
          } catch (Exception e) {
            throw e;
          }
        }
      }
    } else if (klineType == KlineType.TEN_MINUTES_LINE) {
      for (int i = 1; i < klineStrList.size(); i += 2) {
        List<String> tenMinuteKlineList = new ArrayList<>();
        tenMinuteKlineList.add(klineStrList.get(i - 1));
        tenMinuteKlineList.add(klineStrList.get(i));
        klineList.add(new Kline(tenMinuteKlineList, hasTime, klineList.size()));
      }
    } else if (klineType == KlineType.DAY_LINE || klineType == KlineType.ONE_MINUTES_LINE) {
      for (String klineStr : klineStrList) {
        List<String> dayKlineList = new ArrayList<>();
        dayKlineList.add(klineStr);
        klineList.add(new Kline(dayKlineList, hasTime, klineList.size()));
      }
    }
    this.klineList = klineList;
  }

  private void setKlineList(String path, KlineType klineType) throws IOException {
    File file = new File(KLineDrawService.class.getResource(path).getFile());
    setKlineList(file, klineType);
  }

  private void setKlineMap(List<Kline> klineList) {
    if (CollectionUtils.isEmpty(klineList)) return;
    Map<Long, Kline> klineMap = new HashMap<>();
    for (Kline kline : klineList) {
      klineMap.put(kline.getDate(), kline);
    }
    this.klineMap = klineMap;
  }

  public void setMergeKlineList(List<Kline> klineList) {
    if (CollectionUtils.isEmpty(klineList)) return;
    this.mergeKlineList = new MergeKlineProcessor(klineList).getMergeKlineList();
  }

  private void setPeakKlineList() throws IOException {
    if (CollectionUtils.isEmpty(klineList)) return;
    PeakKlineProcessor peakKlineProcessor = new PeakKlineProcessor(this);
    this.peakKlineList = peakKlineProcessor.getPeakKlineList();
    this.breakPeakKlineList = peakKlineProcessor.getBreakPeakKlineList();
    this.jumpPeakKlineList = peakKlineProcessor.getJumpPeakKlineList();
    this.turnPeakKlineList = peakKlineProcessor.getTurnPeakKlineList();
    this.matrixKlineRowList = peakKlineProcessor.getMatrixKlineRowList();
  }

  private void setMatrixKlineList() {
    MatrixKlineProcessor matrixKlineProcessor = new MatrixKlineProcessor(matrixKlineRowList);

  }

  public List<Kline> getKlineList() {
    return klineList;
  }

  public Map<Long, Kline> getKlineMap() {
    return klineMap;
  }

  public List<MergeKline> getMergeKlineList() {
    return mergeKlineList;
  }

  public void printPeakKlineList() {
      System.out.println("peakKlineList: " + JacksonUtils.toJson(
      breakPeakKlineList.stream().map(PeakKlinePrint::new).filter(PeakKlinePrint::isTendencyPeak).collect(Collectors.toList())));
  }

//  public void printMergeKlineList() {
//    System.out.println("mergeKlineList: " + JacksonUtils.toJson(
//        mergeKlineList.stream().map(MergeKline::getMergeKline).collect(Collectors.toList())));
//  }
//
//  public void printPeakKlineList() {
//    System.out.println("peakKlineList: " + JacksonUtils.toJson(
//        peakKlineList.stream().map(peakKline ->
//            new TwoTuple(peakKline.getPeakShape(), peakKline.getMergeKline().getMergeKline()))
//            .collect(Collectors.toList())));
//  }
//
//  public void printBreakPeakKlineList() {
//    System.out.println("beakPeakKlineList: " + JacksonUtils.toJson(
//        breakPeakKlineList.stream().filter(peakKline -> peakKline.isBreakPeak())
//            .map(peakKline -> new TwoTuple(peakKline.getPeakShape(), peakKline.getMergeKline().getMergeKline()))
//            .collect(Collectors.toList())));
//  }
//
//  public void printJumpPeakKlineList() {
//    System.out.println("jumpPeakKlineList: " + JacksonUtils.toJson(
//        jumpPeakKlineList.stream().filter(peakKline -> peakKline.isJumpPeak())
//            .map(peakKline -> new TwoTuple(peakKline.getPeakShape(), peakKline.getMergeKline().getMergeKline()))
//            .collect(Collectors.toList())));
//  }
//
//  public void printTurnPeakKlineList() {
//    System.out.println("turnPeakKlineList: " + JacksonUtils.toJson(
//        turnPeakKlineList.stream().filter(peakKline -> peakKline.isTurnPeak())
//            .map(peakKline -> new TwoTuple(peakKline.getPeakShape(), peakKline.getMergeKline().getMergeKline()))
//            .collect(Collectors.toList())));
//  }

//  public void printTendencyPeakKlineList() {
//    System.out.println("tendencyPeakKlineList: " + JacksonUtils.toJson(
//        tendencyPeakKlineList.stream().filter(peakKline -> peakKline.isTendencyPeak())
//            .map(peakKline -> new TwoTuple(peakKline.getPeakShape(), peakKline.getMergeKline().getMergeKline()))
//            .collect(Collectors.toList())));
//  }
//
//  public void printOppositeTendencyPeakKlineList() {
//    System.out.println("OppositePeakKlineList: " + JacksonUtils.toJson(
//        oppositeTendencyPeakKlineList.stream().map(peakKline ->
//            new TwoTuple(peakKline.getPeakShape(), peakKline.getMergeKline().getMergeKline())).collect(Collectors.toList())));
//  }
//
//  public void printInDependentTendencyPeakKlineList() {
//    System.out.println("IndependentDependencyPeakKlineList: " + JacksonUtils.toJson(
//        independentTendencyPeakKlineList.stream().map(peakKline ->
//            new TwoTuple(peakKline.getPeakShape(), peakKline.getMergeKline().getMergeKline())).collect(Collectors.toList())));
//  }
//
//  public void printAnglePeakKlineList() {
//    System.out.println("PeakKlineAngleList: " + JacksonUtils.toJson(
//        anglePeakKlineList.stream().map(peakKline ->
//            new TwoTuple(peakKline.getMergeKline().getMergeKline().getDate(), peakKline.getAngle())).collect(Collectors.toList())));
//
//  }



}
