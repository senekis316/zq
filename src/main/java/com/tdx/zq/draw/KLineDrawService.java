package com.tdx.zq.draw;

import com.tdx.zq.context.KlineApplicationContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class KLineDrawService implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        //SZ300181 佐力药业	STANDK(K线)	日线	线段	定位1:值:4.67/时:20190806;定位2:值:5.84/时:20190910;
        KlineApplicationContext klineApplicationContext = new KlineApplicationContext("/SZ300181.txt");
        //KlineApplicationContext klineApplicationContext = new KlineApplicationContext("/SZ300181_5.txt");
        klineApplicationContext.printMergeKlineList();
        klineApplicationContext.printPeakKlineList();
        klineApplicationContext.printBreakPeakKlineList();
        klineApplicationContext.printJumpPeakKlineList();
        klineApplicationContext.printTendencyPeakKlineList();
        klineApplicationContext.printOppositeTendencyPeakKlineList();
        klineApplicationContext.printInDependentTendencyPeakKlineList();
        klineApplicationContext.printAnglePeakKlineList();
    }

}
