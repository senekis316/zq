package com.tdx.zq.enums;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public enum KlineType {

  WEEK_LINE("W", "day", "week"),
  MONTH_LINE("M", "day", "month"),
  DAY_LINE("D", "day", "day"),
  HOUR_LINE("H", "five_minute", "one_hour"),
  ONE_MINUTES_LINE("O", "one_minute", "one_minute"),
  TEN_MINUTES_LINE("T", "five_minute", "ten_minute");

  private String code;
  private String inputDirectory;
  private String outputDirectory;

  KlineType(String code, String inputDirectory, String outputDirectory) {
    this.code = code;
    this.inputDirectory = inputDirectory;
    this.outputDirectory = outputDirectory;
  }

  public static List<KlineType> getKlineType(String inputDirectory) {
    List<KlineType> klineTypes = new ArrayList<>();
    for (KlineType klineType: KlineType.values()) {
      if (klineType.getInputDirectory().equals(inputDirectory)) {
        klineTypes.add(klineType);
      }
    }
    if (CollectionUtils.isEmpty(klineTypes)) {
      throw new RuntimeException("Kline Type Error！InputDirectory = " + inputDirectory);
    }
    return klineTypes;
  }

  public String getInputDirectory() {
    return inputDirectory;
  }

  public String getOutputDirectory() {
    return outputDirectory;
  }

  public static KlineType create(String code) {
    for (KlineType klineType : KlineType.values()) {
      if (klineType.code.equalsIgnoreCase(code)) {
        return klineType;
      }
    }
    return null;
  }

}
