package com.tdx.zq.draw;

import com.tdx.zq.enums.LineDirectEnum;
import com.tdx.zq.model.KLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;


public class KLineDrawServiceTest {

    private KLineDrawService kLineDrawService = new KLineDrawService();

    @Test
    public void computeDirect() {

        assertThrows(IllegalArgumentException.class,  () -> kLineDrawService.computeDirect(null, null, null));

        KLine leftKLine1 = new KLine();
        leftKLine1.setLow(90);
        leftKLine1.setHigh(100);

        KLine rightKLine1 = new KLine();
        rightKLine1.setLow(100);
        rightKLine1.setHigh(110);

        assertEquals(LineDirectEnum.UP, kLineDrawService.computeDirect(leftKLine1, rightKLine1, LineDirectEnum.BALANCE));

        KLine leftKLine2 = new KLine();
        leftKLine2.setLow(100);
        leftKLine2.setHigh(110);

        KLine rightKLine2 = new KLine();
        rightKLine2.setLow(90);
        rightKLine2.setHigh(100);

        assertEquals(LineDirectEnum.DOWN, kLineDrawService.computeDirect(leftKLine2, rightKLine2, LineDirectEnum.BALANCE));

        KLine leftKLine3 = new KLine();
        leftKLine3.setLow(100);
        leftKLine3.setHigh(110);

        KLine rightKLine3 = new KLine();
        rightKLine3.setLow(90);
        rightKLine3.setHigh(110);

        assertEquals(LineDirectEnum.BALANCE, kLineDrawService.computeDirect(leftKLine3, rightKLine3, LineDirectEnum.BALANCE));
        assertEquals(LineDirectEnum.UP, kLineDrawService.computeDirect(leftKLine3, rightKLine3, LineDirectEnum.UP));
        assertEquals(LineDirectEnum.DOWN, kLineDrawService.computeDirect(leftKLine3, rightKLine3, LineDirectEnum.DOWN));

    }

    @Test
    public void isContain() {

        assertEquals(false, kLineDrawService.isContain(null, null, LineDirectEnum.BALANCE));

        KLine leftKLine1 = new KLine();
        leftKLine1.setLow(90);
        leftKLine1.setHigh(110);

        KLine rightKLine1 = new KLine();
        rightKLine1.setLow(100);
        rightKLine1.setHigh(105);

        assertEquals(true, kLineDrawService.isContain(leftKLine1, rightKLine1, LineDirectEnum.UP));

        KLine leftKLine2 = new KLine();
        leftKLine2.setLow(100);
        leftKLine2.setHigh(105);

        KLine rightKLine2 = new KLine();
        rightKLine2.setLow(90);
        rightKLine2.setHigh(110);

        assertEquals(true, kLineDrawService.isContain(leftKLine2, rightKLine2, LineDirectEnum.DOWN));

        KLine leftKLine3 = new KLine();
        leftKLine3.setLow(95);
        leftKLine3.setHigh(110);

        KLine rightKLine3 = new KLine();
        rightKLine3.setLow(100);
        rightKLine3.setHigh(120);

        assertEquals(false, kLineDrawService.isContain(leftKLine3, rightKLine3, LineDirectEnum.DOWN));

    }

    @Test
    public void kLineCombine() {

        assertThrows(IllegalArgumentException.class,  () -> kLineDrawService.kLineCombine(null, null, null));
        assertThrows(IllegalArgumentException.class,  () -> kLineDrawService.kLineCombine(null, null, LineDirectEnum.BALANCE));

        KLine leftKLine1 = new KLine();
        leftKLine1.setLow(90);
        leftKLine1.setHigh(110);
        leftKLine1.setDate(20100915);

        KLine rightKLine1 = new KLine();
        rightKLine1.setLow(100);
        rightKLine1.setHigh(105);
        rightKLine1.setDate(20100916);

        KLine expectKLine1 = new KLine();
        expectKLine1.setLow(100);
        expectKLine1.setHigh(110);
        expectKLine1.setDate(20100916);

        assertEquals(expectKLine1, kLineDrawService.kLineCombine(leftKLine1, rightKLine1, LineDirectEnum.UP));

        KLine leftKLine2 = new KLine();
        leftKLine2.setLow(90);
        leftKLine2.setHigh(110);
        leftKLine2.setDate(20100915);

        KLine rightKLine2 = new KLine();
        rightKLine2.setLow(100);
        rightKLine2.setHigh(105);
        rightKLine2.setDate(20100916);

        KLine expectKLine2 = new KLine();
        expectKLine2.setLow(90);
        expectKLine2.setHigh(105);
        expectKLine2.setDate(20100916);

        assertEquals(expectKLine2, kLineDrawService.kLineCombine(leftKLine2, rightKLine2, LineDirectEnum.DOWN));

    }
}