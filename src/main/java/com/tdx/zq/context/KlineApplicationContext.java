package com.tdx.zq.context;

import com.tdx.zq.draw.KLineDrawService;
import com.tdx.zq.draw.MatrixKlineProcessor;
import com.tdx.zq.draw.MergeKlineProcessor;
import com.tdx.zq.draw.PeakKlineProcessor;
import com.tdx.zq.enums.KlineType;
import com.tdx.zq.model.BSPoint;
import com.tdx.zq.model.Kline;
import com.tdx.zq.model.MatrixKlineRow;
import com.tdx.zq.model.MergeKline;
import com.tdx.zq.model.PeakKline;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;


public class KlineApplicationContext {

  //private String outputPath;
  private String klineCode;
  private KlineType klineType;
  private List<Kline> klineList;
  private Map<Long, Kline> klineMap;
  private List<MergeKline> mergeKlineList;
  private List<PeakKline> peakKlineList;
  private List<PeakKline> breakPeakKlineList;
  private List<PeakKline> jumpPeakKlineList;
  private List<PeakKline> turnPeakKlineList;
  private List<MatrixKlineRow> matrixKlineRowList;
  private Calendar calendar = Calendar.getInstance();
  private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
  private Map<String, Map<KlineType, PriorityQueue<BSPoint>>> bsPointMap;
  private Map<String, Map<KlineType, String>> enhanceMap;
  private Map<String, Map<KlineType, String>> matrixThresholdBreakMap;

  public KlineApplicationContext(String path, KlineType klineType, Map<String, Map<KlineType, PriorityQueue<BSPoint>>> bsPointMap) throws IOException, ParseException {
    this.klineType = klineType;
    //this.outputPath = outputPath;
    this.bsPointMap = bsPointMap;
    File file = new File(path);
    setKlineList(file, klineType);
    setKlineMap(klineList);
    setMergeKlineList(klineList);
    setPeakKlineList();
    setMatrixKlineList();
  }

  public KlineApplicationContext(File file, KlineType klineType,
                                 Map<String, Map<KlineType, PriorityQueue<BSPoint>>> bsPointMap,
                                 Map<String, Map<KlineType, String>> enhanceMap,
                                 Map<String, Map<KlineType, String>> matrixThresholdBreakMap) throws IOException, ParseException {
    this.klineType = klineType;
    //this.outputPath = outputPath;
    this.bsPointMap = bsPointMap;
    this.enhanceMap = enhanceMap;
    this.matrixThresholdBreakMap = matrixThresholdBreakMap;
    setKlineList(file, klineType);
    setKlineMap(klineList);
    setMergeKlineList(klineList);
    setPeakKlineList();
    setMatrixKlineList();
  }

  private void setKlineList(File file, KlineType klineType) throws IOException, ParseException {

    FileReader fileReader = new FileReader(file, Charset.forName("GBK"));
    BufferedReader bufferedReader = new BufferedReader(fileReader);

    List<String> klineStrList = bufferedReader.lines().collect(Collectors.toList());
    if (klineStrList.size() == 3) return;
    if (klineType == KlineType.HOUR_LINE && klineStrList.size() < 15);
    if (klineType == KlineType.TEN_MINUTES_LINE && klineStrList.size() < 5);
    klineStrList.remove(klineStrList.size() - 1);
    klineCode = file.getName().split("\\.")[0].replace("#", "");
    //klineCode = klineStrList.get(0).split(" ")[0];
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
            klineList.add(new Kline(hourKlineList, hasTime, klineList.size(), klineType));
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
        klineList.add(new Kline(tenMinuteKlineList, hasTime, klineList.size(), klineType));
      }
    } else if (klineType == KlineType.DAY_LINE || klineType == KlineType.ONE_MINUTES_LINE) {
      for (String klineStr : klineStrList) {
        List<String> dayKlineList = new ArrayList<>();
        dayKlineList.add(klineStr);
        klineList.add(new Kline(dayKlineList, hasTime, klineList.size(), klineType));
      }
    } else if (klineType == KlineType.MONTH_LINE) {
      List<String> monthKlineList = new ArrayList<>();
      long month = -1;
      for (int i = 0; i < klineStrList.size(); i++) {
        String[] values = klineStrList.get(i).split("\t");
        long date = Integer.valueOf(values[0].trim().replace("/", ""));
        if (month == -1) {
          month = date / 100;
        } else if (month != date / 100){
          month = date / 100;
          klineList.add(new Kline(monthKlineList, hasTime, klineList.size(), klineType));
          monthKlineList = new ArrayList<>();
        }
        monthKlineList.add(klineStrList.get(i));
      }
      if (monthKlineList.size() > 0) {
        klineList.add(new Kline(monthKlineList, hasTime, klineList.size(), klineType));
      }
    } else if (klineType == KlineType.WEEK_LINE) {
      List<String> weekKlineList = new ArrayList<>();
      int prevWeek = -1;
      int prevYear = -1;
      for (int i = 0; i < klineStrList.size(); i++) {
        String[] values = klineStrList.get(i).split("\t");
        String dateStr = values[0].trim().replace("/", "");
        Date dateJava = dateFormat.parse(dateStr);
        calendar.setTime(dateJava);
        int currYear = Integer.valueOf(yearFormat.format(dateJava));
        int currWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        if (prevWeek != -1 && prevYear != -1 && prevWeek != currWeek) {
          klineList.add(new Kline(weekKlineList, hasTime, klineList.size(), klineType));
          weekKlineList = new ArrayList<>();
        }
        prevWeek = currWeek;
        prevYear = currYear;
        weekKlineList.add(klineStrList.get(i));
      }
      if (weekKlineList.size() > 0) {
        klineList.add(new Kline(weekKlineList, hasTime, klineList.size(), klineType));
      }
    }
    this.klineList = klineList;
  }

  private void setKlineList(String path, KlineType klineType) throws IOException, ParseException {
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
    MatrixKlineProcessor matrixKlineProcessor = new MatrixKlineProcessor(this);
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

//  public void printPeakKlineList() {
//      System.out.println("peakKlineList: " + JacksonUtils.toJson(
//      breakPeakKlineList.stream().map(PeakKlinePrint::new).filter(PeakKlinePrint::isTendencyPeak).collect(Collectors.toList())));
//  }

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

//  public String getOutputPath() {
//    return outputPath;
//  }

  public KlineType getKlineType() {
    return klineType;
  }

  public List<PeakKline> getPeakKlineList() {
    return peakKlineList;
  }

  public List<PeakKline> getBreakPeakKlineList() {
    return breakPeakKlineList;
  }

  public List<PeakKline> getJumpPeakKlineList() {
    return jumpPeakKlineList;
  }

  public List<PeakKline> getTurnPeakKlineList() {
    return turnPeakKlineList;
  }

  public List<MatrixKlineRow> getMatrixKlineRowList() {
    return matrixKlineRowList;
  }

  public String getKlineCode() {
    return klineCode;
  }

  public Map<String, Map<KlineType, PriorityQueue<BSPoint>>> getBsPointMap() {
    return bsPointMap;
  }

  public Map<String, Map<KlineType, String>> getEnhanceMap() {
    return enhanceMap;
  }

  public Map<String, Map<KlineType, String>> getMatrixThresholdBreakMap() {
    return matrixThresholdBreakMap;
  }

}
