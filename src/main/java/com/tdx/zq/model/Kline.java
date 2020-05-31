package com.tdx.zq.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Slf4j
public class Kline {

    private int time;

    private int code;

    private long date;

    private int open;

    private int high;

    private int low;

    private int close;

    //成交额
    private float amount;

    //成交量
    private int volume;

    private int index;

    public Kline(byte[] bytes, int code) {

        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        this.date = bb.getInt();
        this.open = bb.getInt();
        this.high = bb.getInt();
        this.low = bb.getInt();
        this.close = bb.getInt();
        this.amount = bb.getFloat();
        this.volume = bb.getInt();
        this.code = code;
    }

    public Kline(Kline kline) {
        this.date = kline.getDate();
        this.open = kline.getOpen();
        this.high = kline.getHigh();
        this.low = kline.getLow();
        this.close = kline.getClose();
        this.amount = kline.getAmount();
        this.volume = kline.getVolume();
        this.code = kline.getCode();
    }

//    public Kline(String klineStr, int index) {
//        String[] strs = klineStr.split("\t");
//        if (strs.length == 7) {
//            this.date = Integer.valueOf(strs[0].trim().replace("/", ""));
//            this.open = Integer.valueOf(strs[1].trim().replace(".", ""));
//            this.high = Integer.valueOf(strs[2].trim().replace(".", ""));
//            this.low = Integer.valueOf(strs[3].trim().replace(".", ""));
//            this.close = Integer.valueOf(strs[4].trim().replace(".", ""));
//            this.volume = Integer.valueOf(strs[5].trim());
//            this.amount = Float.valueOf(strs[6].trim());
//        } else if (strs.length >= 8 ) {
//            this.date = Integer.valueOf(strs[0].trim().replace("/", ""));
//            this.time = Integer.valueOf(strs[1].trim());
//            this.open = Integer.valueOf(strs[2].trim().replace(".", ""));
//            this.high = Integer.valueOf(strs[3].trim().replace(".", ""));
//            this.low = Integer.valueOf(strs[4].trim().replace(".", ""));
//            this.close = Integer.valueOf(strs[5].trim().replace(".", ""));
//            this.volume = Integer.valueOf(strs[6].trim());
//            this.amount = Float.valueOf(strs[7].trim());
//        }
//        this.index = index;
//    }
//
//    public Kline(String prev, String curr, int index) {
//        String[] values1 = prev.split("\t");
//        String[] values2 = curr.split("\t");
//        this.date = Long.valueOf(values2[0].trim().replace("/", "") + values2[1].trim());
//        this.time = Integer.valueOf(values2[1].trim());
//        this.open = Integer.valueOf(values2[2].trim().replace(".", ""));
//        this.high = Math.max(Integer.valueOf(values1[3].trim().replace(".", "")), Integer.valueOf(values2[3].trim().replace(".", "")));
//        this.low = Math.min(Integer.valueOf(values1[4].trim().replace(".", "")), Integer.valueOf(values2[4].trim().replace(".", "")));
//        this.close = Integer.valueOf(values2[5].trim().replace(".", ""));
//        this.volume = Integer.valueOf(values1[6].trim());
//        this.amount = Float.valueOf(values1[7].trim());
//        this.index = index;
//    }

    public Kline(List<String> lines, boolean hasTime, int index) {
        this.low = Integer.MAX_VALUE;
        for (int i = 0; i < lines.size(); i++) {
            String[] values = lines.get(i).split("\t");
            if (hasTime) {
                this.date = Long.valueOf(values[0].trim().replace("/", "") + values[1].trim());
                this.time = Integer.valueOf(values[1].trim());
                this.open = Integer.valueOf(values[2].trim().replace(".", ""));
                this.high = Math.max(high, Integer.valueOf(values[3].trim().replace(".", "")));
                this.low = Math.min(low, Integer.valueOf(values[4].trim().replace(".", "")));
                this.close = Integer.valueOf(values[5].trim().replace(".", ""));
                this.volume = Integer.valueOf(values[6].trim());
                this.amount = Float.valueOf(values[7].trim());
            } else {
                this.date = Integer.valueOf(values[0].trim().replace("/", ""));
                this.open = Integer.valueOf(values[1].trim().replace(".", ""));
                this.high = Integer.valueOf(values[2].trim().replace(".", ""));
                this.low = Integer.valueOf(values[3].trim().replace(".", ""));
                this.close = Integer.valueOf(values[4].trim().replace(".", ""));
                this.volume = Integer.valueOf(values[5].trim());
                this.amount = Float.valueOf(values[6].trim());
            }
        }
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Kline kline = (Kline) o;
        return time == kline.time &&
            code == kline.code &&
            date == kline.date &&
            open == kline.open &&
            high == kline.high &&
            low == kline.low &&
            close == kline.close &&
            Float.compare(kline.amount, amount) == 0 &&
            volume == kline.volume &&
            index == kline.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, code, date, open, high, low, close, amount, volume, index);
    }

}
