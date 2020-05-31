package com.tdx.zq.enums;

import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum KlineType {

  DAY_LINE("day", "day"),
  HOUR_LINE("five_minute", "one_hour"),
  ONE_MINUTES_LINE("one_minute", "one_minute"),
  TEN_MINUTES_LINE("five_minute", "ten_minute");

  private String inputDirectory;
  private String outputDirectory;

  KlineType(String inputDirectory, String outputDirectory) {
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

}
