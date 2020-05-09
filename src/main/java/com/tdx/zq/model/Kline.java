package com.tdx.zq.model;

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

    private int code;

    private int date;

    private int open;

    private int high;

    private int low;

    private int close;

    //成交额
    private float amount;

    //成交量
    private int volume;

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

    public Kline(String klineStr) {
        String[] strs = klineStr.split("\t");
        this.date = Integer.valueOf(strs[0].trim().replace("/", ""));
        this.open = Integer.valueOf(strs[1].trim().replace(".", ""));
        this.high = Integer.valueOf(strs[2].trim().replace(".", ""));
        this.low = Integer.valueOf(strs[3].trim().replace(".", ""));
        this.close = Integer.valueOf(strs[4].trim().replace(".", ""));
        this.volume = Integer.valueOf(strs[5].trim());
        this.amount = Float.valueOf(strs[6].trim());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kline kLine = (Kline) o;
        return date == kLine.date &&
                high == kLine.high &&
                low == kLine.low;
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, high, low);
    }
}
