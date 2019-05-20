package com.tdx.zq.draw;

import com.tdx.zq.enums.LineDirectEnum;
import com.tdx.zq.model.KLine;
import com.tdx.zq.utils.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
public class KLineDrawService implements InitializingBean {

    private List<KLine> kLineList = new LinkedList<>();


    //private LinkedList<DayLine> dayLines = new LinkedList<>();

    public void compute() {

        //kLineList = kLineList.stream().filter(kLine -> (kLine.getDate() > 20190312 && kLine.getDate() < 20190315)).collect(Collectors.toList());


        List<KLine> filterDates = filterData();

        //List<KLine> hlDates = getHLKlines(filterDates);

        //drawLine(LineDirectEnum.BALANCE, filterDates, 0, null, 0, 0);

        //log.info("hlDates: " + JacksonUtils.toJson(hlDates));

    }

    //---------------------------------------KLine高低值获取----------------------------------------

    private List<KLine> getHLKlines(List<KLine> klines) {

        int ups = 0;
        int downs = 0;
        int continueUps = 0;
        int continueDowns = 0;
        KLine lowest = null;
        KLine highest = null;
        LineDirectEnum prevDirect = LineDirectEnum.BALANCE;
        LineDirectEnum continueDirect = LineDirectEnum.BALANCE;

        List<KLine> result = new ArrayList<>();

        for (int i = 1; i < klines.size(); i++) {
            KLine leftKline = klines.get(i-1);
            KLine rightKline = klines.get(i);
            LineDirectEnum currentDirect = LineDirectEnum.BALANCE;
            LineDirectEnum currentContinueDirect = continueDirect;

            //1.获取currentDirect
            if (leftKline.getHigh() < rightKline.getHigh()
                    && leftKline.getLow() < rightKline.getLow()) {
                currentDirect = LineDirectEnum.UP;
            } else if (rightKline.getHigh() < leftKline.getHigh()
                    && rightKline.getLow() < leftKline.getLow()) {
                currentDirect = LineDirectEnum.DOWN;
            }

            //2.保存最高值最小值
            if (lowest == null && highest == null) {
                if (currentDirect == LineDirectEnum.UP) {
                    lowest = leftKline;
                    highest = rightKline;
                } else if (currentDirect == LineDirectEnum.DOWN) {
                    lowest = rightKline;
                    highest = leftKline;
                } else {
                    lowest = leftKline.getLow() < rightKline.getLow() ? leftKline : rightKline;
                    highest = leftKline.getHigh() > rightKline.getHigh() ? leftKline : rightKline;
                }
            } else {
                if (currentDirect == LineDirectEnum.UP) {
                    if (rightKline.getHigh() > highest.getHigh()) {
                        highest = rightKline;
                    }
                } else if (currentDirect == LineDirectEnum.DOWN){
                    if (rightKline.getLow() < lowest.getLow()) {
                        lowest = rightKline;
                    }
                } else {
                    if (rightKline.getLow() < lowest.getLow()) {
                        lowest = rightKline;
                    }
                    if (rightKline.getHigh() > lowest.getHigh()) {
                        highest = rightKline;
                    }
                }
            }

            //3.统计计数
            if (currentDirect == LineDirectEnum.UP) {
                ups++;
                if (prevDirect == LineDirectEnum.UP) {
                    continueUps++;
                    continueDowns = 0;
                }

            } else if (currentDirect == LineDirectEnum.DOWN){
                downs++;
                if (prevDirect == LineDirectEnum.DOWN) {
                    continueDowns++;
                    continueUps = 0;
                }
            }

            //4.跳空判断
            if (rightKline.getLow() > leftKline.getHigh()) {
                currentContinueDirect = LineDirectEnum.UP;
            }

            if (rightKline.getHigh() < leftKline.getLow()) {
                currentContinueDirect = LineDirectEnum.DOWN;
            }

            //5.趋势判断
            if (continueUps >= 4) {
                currentContinueDirect = LineDirectEnum.UP;
            }

            if (continueDowns >= 4) {
                currentContinueDirect = LineDirectEnum.DOWN;
            }

            //6.趋势反转逻辑
            boolean directChange = false;

            if(continueDirect == LineDirectEnum.UP && currentContinueDirect == LineDirectEnum.DOWN) {
                directChange = true;
                if (result.size() == 0) {
                    result.add(lowest);
                }
            }

            if (continueDirect == LineDirectEnum.DOWN && currentContinueDirect == LineDirectEnum.UP) {
                directChange = true;
                if (result.size() == 0) {
                    result.add(highest);
                }
            }

            if (directChange) {
                result.add(klines.get(i-ups-downs));
                ups = 0;
                downs = 0;
                lowest = null;
                highest = null;
            }

            if (currentContinueDirect != continueDirect) {
                continueDirect = currentContinueDirect;
            }

        }

        return result;
    }


    //----------------------------------------KLine原始数据合并-------------------------------------

    private LinkedList<KLine> filterData() {

        System.out.println("过滤前的数据量:" + kLineList.size());
        System.out.println("过滤前的数据:" + JacksonUtils.toJson(kLineList));

        LineDirectEnum lineDirectEnum = LineDirectEnum.BALANCE;

        LinkedList<KLine> filterkLineList = new LinkedList();
        filterkLineList.add(kLineList.get(0));

        for (int i = 1; i < kLineList.size(); i++) {

            KLine leftKLine = filterkLineList.getLast();
            KLine rightKLine = kLineList.get(i);
            lineDirectEnum = computeDirect(leftKLine, rightKLine, lineDirectEnum);

            if (lineDirectEnum != LineDirectEnum.BALANCE) {
                if (isContain(leftKLine, rightKLine, lineDirectEnum)) {

                    KLine combineKLine = kLineCombine(leftKLine, rightKLine, lineDirectEnum);
                    //leftKLine = combineKLine;
                    //System.out.println(leftKLine == filterkLineList.getLast());
                    filterkLineList.set(filterkLineList.size()-1, combineKLine);
                } else {
                    filterkLineList.add(rightKLine);
                }
            } else {
                filterkLineList.add(rightKLine);
            }

        }

        System.out.println("过滤后的数据量:" + filterkLineList.size());
        System.out.println("过滤后的数据:" + JacksonUtils.toJson(filterkLineList));
        return filterkLineList;

    }

    //1.判断K线涨跌趋势:
    //(1)如果右边的K线，High值比左边的High值高，Low值比左边的Low值高，则增长趋势。
    //(2)如果右边的K线，High值比左边的High值低，Low值比左边的Low值低，则下降趋势。
    //(3)除以上两种情况，均无法确定趋势。
    //(4)当无法确定K线的涨跌趋势时，已最近的前一个K线趋势，作为当前K线的趋势。
    public LineDirectEnum computeDirect(KLine leftKLine, KLine rightKLine, LineDirectEnum directEnum) {

        if (directEnum == null) {
            throw new IllegalArgumentException("LineDirectEnum不能为空!");
        }

        if (rightKLine.getHigh() > leftKLine.getHigh() && rightKLine.getLow() > leftKLine.getLow()) {
            return LineDirectEnum.UP;
        }

        if (rightKLine.getHigh() < leftKLine.getHigh() && rightKLine.getLow() < leftKLine.getLow()) {
            return LineDirectEnum.DOWN;
        }

        return directEnum;

    }

    //2.两条K线是否是包含关系:
    //(1)如果左边的K线，High值大于等于右边的High值，Low值小于等于右边的Low值，则为包含关系。
    //(2)如果右边的K线，High值大于等于左边的High值，Low值小于等于左边的Low值，则为包含关系。
    //(3)满足以上条件后，K线必须要能存在趋势，才能确定为包含关系。
    //(4)满足以上所有条件，左右K线为包含关系，其余情况均非包含关系。
    public boolean isContain(KLine leftKLine, KLine rightKLine, LineDirectEnum directEnum) {

        if (directEnum == LineDirectEnum.BALANCE) {
            return false;
        }

        if (leftKLine.getHigh() >= rightKLine.getHigh() && leftKLine.getLow() <= rightKLine.getLow()) {
            return true;
        }

        if (rightKLine.getHigh() >= leftKLine.getHigh() && rightKLine.getLow() <= leftKLine.getLow()) {
            return true;
        }

        return false;

    }

    //3.K线包含:
    //(1)只要是包含关系，包含后K线日期，永远取右边K线的日期。
    //(2)如果是增长趋势，合并后K线的High值取两条K线High值的最大值，合并后K线的Low值取两条K线Low值的最大值。
    //(3)如果是减少趋势，合并后K线的High值取两条K线High值的最小值，合并后K线的Low值取两条K线Low值的最小值。
    //(4)右边K线与左边K线只要满足包含关系，可以进行连续的包含合并。
    public KLine kLineCombine(KLine leftKLine, KLine rightKLine, LineDirectEnum directEnum) {

        if (directEnum == null) {
            throw new IllegalArgumentException("LineDirectEnum不能为空!");
        }

        if (directEnum == LineDirectEnum.BALANCE) {
            throw new IllegalArgumentException("KLineCombine方法不能处理LineDirectEnum.Balance的情况!");
        }

        KLine kLine = new KLine();
        kLine.setDate(rightKLine.getDate());

        if (directEnum == LineDirectEnum.UP) {
            kLine.setLow(Math.max(leftKLine.getLow(), rightKLine.getLow()));
            kLine.setHigh(Math.max(leftKLine.getHigh(), rightKLine.getHigh()));
            kLine.setDirect(LineDirectEnum.UP);
        } else {
            kLine.setLow(Math.min(leftKLine.getLow(), rightKLine.getLow()));
            kLine.setHigh(Math.min(leftKLine.getHigh(), rightKLine.getHigh()));
            kLine.setDirect(LineDirectEnum.DOWN);
        }

        kLine.setCode(rightKLine.getCode());
        kLine.setAmount(leftKLine.getAmount() + rightKLine.getAmount());
        kLine.setVolume(leftKLine.getVolume() + rightKLine.getVolume());
        kLine.setOpen(Math.max(leftKLine.getOpen(), rightKLine.getOpen()));
        kLine.setClose(Math.max(leftKLine.getClose(), rightKLine.getClose()));

        return kLine;

    }

    //----------------------------------------------------------------------------------

    /*private void drawLine(LineDirectEnum direct, List<DayDate> dates, Integer index, DayDate beginDate, Integer ups, Integer downs) {

        if (index == dates.size() - 1) {
            return;
        }

        DayDate currDayDate = dates.get(index);
        DayDate nextDayDate = dates.get(index + 1);

        log.info("curr: low = {}, high = {}", currDayDate.getLow(), currDayDate.getHigh());
        log.info("next: low = {}, high = {}", nextDayDate.getLow(), nextDayDate.getHigh());

        if (currDayDate.getHigh() < nextDayDate.getHigh()
                && currDayDate.getLow() < nextDayDate.getLow()) {
            ups++;
            if (direct != LineDirectEnum.DOWN) {
                downs = 0;
            }
        }

        if (currDayDate.getHigh() > nextDayDate.getHigh()
                && currDayDate.getLow() > nextDayDate.getLow()) {
            downs++;
            if (direct != LineDirectEnum.UP) {
                ups = 0;
            }
        }

        if (ups >= 4 ) {
            if (direct == LineDirectEnum.BALANCE) {
                direct = LineDirectEnum.UP;
            } else if (direct == LineDirectEnum.DOWN) {
                dayLines.add(new DayLine(beginDate, currDayDate, LineDirectEnum.DOWN));
                direct = LineDirectEnum.UP;
                beginDate = currDayDate;
            }
        }

        if (downs >= 4 ) {
            if (direct == LineDirectEnum.BALANCE) {
                direct = LineDirectEnum.DOWN;
            } else if (direct == LineDirectEnum.UP) {
                dayLines.add(new DayLine(beginDate, currDayDate, LineDirectEnum.UP));
                direct = LineDirectEnum.DOWN;
                beginDate = currDayDate;
            }
        }

        if (direct == LineDirectEnum.UP) {
            if (currDayDate.getLow() < beginDate.getLow()) {
                ups = 0;
                direct = LineDirectEnum.BALANCE;
            }
        }

        if (direct == LineDirectEnum.DOWN) {
            if (currDayDate.getHigh() > beginDate.getHigh()) {
                downs = 0;
                direct = LineDirectEnum.BALANCE;
            }
        }

        log.info("ups = {}, downs = {}", ups, downs);
        log.info("direct = {}", direct);

        if (index == 0) {
            drawLine(direct, dates, ++index, currDayDate, ups, downs);
        } else {
            if (direct == LineDirectEnum.BALANCE) {
                drawLine(direct, dates, ++index, beginDate, ups, downs);
            } else if (direct == LineDirectEnum.UP) {
                drawLine(direct, dates, ++index, beginDate, ups, downs);
            } else {
                drawLine(direct, dates, ++index, beginDate, ups, downs);
            }
        }

        /*if (index == 0) {
            drawLine(direct, dates, ++index, currDayDate, ups, downs);
        }else if (currentDirect == LineDirectEnum.UP && direct == LineDirectEnum.UP) {
            drawLine(currentDirect, dates, ++index, beginDate, ++continues, 0);
        } else if (currentDirect == LineDirectEnum.DOWN && direct == LineDirectEnum.DOWN) {
            drawLine(currentDirect, dates, ++index, beginDate, ++continues, 0);
        } else if (currentDirect == LineDirectEnum.UP && direct == LineDirectEnum.DOWN) {
            if (dates.get(index).getLow() < beginDate.getLow()) {
                if (continues >= 4) {
                    dayLines.add(new DayLine(beginDate, dates.get(index - 1), LineDirectEnum.DOWN));
                    beginDate = dates.get(index - 1);
                }
            } else {
                if (reverses >= 4) {
                    if (continues >= 4) {
                        dayLines.add(new DayLine(beginDate, dates.get(index - 1), LineDirectEnum.DOWN));
                        beginDate = dates.get(index - 1);
                    }
                } else {
                    reverses++;
                }
            }
            drawLine(currentDirect, dates, ++index, beginDate, 0, reverses);
        } else {
            if (dates.get(index).getHigh() > beginDate.getHigh()) {
                if (continues >= 4) {
                    dayLines.add(new DayLine(beginDate, dates.get(index - 1), LineDirectEnum.UP));
                    beginDate = dates.get(index);
                }
            } else {
                if (reverses >= 4) {
                    if (continues >= 4) {
                        dayLines.add(new DayLine(beginDate, dates.get(index - 1), LineDirectEnum.UP));
                        beginDate = dates.get(index - 1);
                    }
                } else {
                    reverses++;
                }
            }
            drawLine(currentDirect, dates, ++index, beginDate, 0, reverses);
        }

    }*/

    @Override
    public void afterPropertiesSet() throws Exception {
        File file = new File(KLineDrawService.class.getResource("/sz300181.day").getFile());
        InputStream inputStream = new FileInputStream(file);
        byte[] bytes = new byte[32];
        while(inputStream.read(bytes) != -1) {
            kLineList.add(new KLine(bytes, 300181));
        }
        compute();
    }
}
