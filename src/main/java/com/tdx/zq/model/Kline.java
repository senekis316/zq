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

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public int getHigh() {
        return high;
    }

    public void setHigh(int high) {
        this.high = high;
    }

    public int getLow() {
        return low;
    }

    public void setLow(int low) {
        this.low = low;
    }

    public int getClose() {
        return close;
    }

    public void setClose(int close) {
        this.close = close;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

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
