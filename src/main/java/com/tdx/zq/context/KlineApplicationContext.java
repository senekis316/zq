package com.tdx.zq.context;

import com.tdx.zq.draw.KLineDrawService;
import com.tdx.zq.model.Kline;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KlineApplicationContext {

  private LinkedList<Kline> klineList;

  private Map<Integer, Kline> klineMap;

  public KlineApplicationContext(String path) throws FileNotFoundException {
    this.klineList = readFileToKlineList(path);
    writeToKlineMap();
  }

  private LinkedList<Kline> readFileToKlineList(String path) throws FileNotFoundException {

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
    return klineList;
  }

  private void writeToKlineMap() {
    klineMap = new HashMap<>();
    for(Kline kline : klineList) {
      klineMap.put(kline.getDate(), kline);
    }
  }

  public LinkedList<Kline> getKlineList() {
    return klineList;
  }

  public Map<Integer, Kline> getKlineMap() {
    return klineMap;
  }

}
