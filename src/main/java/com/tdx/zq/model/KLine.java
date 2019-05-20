package com.tdx.zq.model;

import com.tdx.zq.enums.LineDirectEnum;
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
public class KLine {

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

    //趋势
    private LineDirectEnum direct = LineDirectEnum.BALANCE;


    public KLine(byte[] bytes, int code) {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KLine kLine = (KLine) o;
        return date == kLine.date &&
                high == kLine.high &&
                low == kLine.low;
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, high, low);
    }
}
