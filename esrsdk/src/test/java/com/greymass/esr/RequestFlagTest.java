package com.greymass.esr;

import com.eclipsesource.v8.V8;
import com.greymass.esr.models.RequestFlag;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class RequestFlagTest {
    @Test
    public void testSetters() {
        RequestFlag requestFlag = new RequestFlag((byte) 1);
        assertTrue("Default should be broadcast", requestFlag.isBroadcast());
        assertFalse("Default should not be background", requestFlag.isBackground());

        requestFlag.setBackground(true);
        assertTrue("After setting background=true, background should be true", requestFlag.isBackground());
        assertTrue("After setting background=true, broadcast should still be true", requestFlag.isBroadcast());

        requestFlag.setBroadcast(false);
        assertFalse("After setting broadcast=false, broadcast should be false", requestFlag.isBroadcast());

        requestFlag.setBackground(false);
        assertFalse("After setting background=false, background should be false", requestFlag.isBackground());
        assertFalse("After setting background=false, broadcast should still be false", requestFlag.isBroadcast());
    }
}