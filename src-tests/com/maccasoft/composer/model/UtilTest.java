/*
 * Copyright (c) 2016 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package com.maccasoft.composer.model;

import com.maccasoft.composer.model.Util;

import junit.framework.TestCase;

public class UtilTest extends TestCase {

    public void testNoteIndex() throws Exception {
        assertEquals(37, Util.noteIndex("C-4"));
        assertEquals(38, Util.noteIndex("C#4"));
    }

    public void testNoteFrequency() throws Exception {
        assertEquals(261.626, Util.noteFrequency("C-4"), 0.01);
        assertEquals(277.184, Util.noteFrequency("C#4"), 0.01);
    }

    public void testNoteRegisterValue() throws Exception {
        assertEquals(Util.noteValue[0] >> 6, Util.noteRegisterValue("C-4"));
        assertEquals(Util.noteValue[1] >> 6, Util.noteRegisterValue("C#4"));
    }

    public void testGetFrequencyRegisterValue() throws Exception {
        assertEquals(Util.noteValue[0] >> 6, Util.frequencyRegisterValue(261.626), 100);
        assertEquals(Util.noteValue[1] >> 6, Util.frequencyRegisterValue(277.184), 100);
    }

    public void testGetFrequencyFromValue() throws Exception {
        assertEquals(261.626, Util.frequencyFromRegisterValue(0x00DBD1CBL), 0.001);
    }

    public void testGetNegativeFrequencyFromValue() throws Exception {
        assertEquals(-52.0, Util.frequencyFromRegisterValue(0xFFD44F31L), 0.5);
        assertEquals(-81.0, Util.frequencyFromRegisterValue(0xFFBBF182L), 0.5);
    }

    public void testGetVolume() throws Exception {
        assertEquals("100000000", String.format("%08X", Util.volumeRegisterValue(100)));
        assertEquals("E4000000", String.format("%08X", Util.volumeRegisterValue(90)));
        assertEquals("80000000", String.format("%08X", Util.volumeRegisterValue(50)));
        assertEquals("00000000", String.format("%08X", Util.volumeRegisterValue(0)));
    }

    public void testGetVolumeFromValue() throws Exception {
        assertEquals(90.0, Util.volumeFromRegisterValue(0xE4000000L), 1.0);
        assertEquals(50.0, Util.volumeFromRegisterValue(0x80000000L), 0.01);
        assertEquals(0.0, Util.volumeFromRegisterValue(0x00000000L), 0.01);
    }

    public void testGetEnvelopeValue() throws Exception {
        assertEquals("00000000", String.format("%08X", Util.envelopeRegisterValue(0, 1)));
        assertEquals("00000008", String.format("%08X", Util.envelopeRegisterValue(0, 2)));
        assertEquals("00000100", String.format("%08X", Util.envelopeRegisterValue(0, 50)));
        assertEquals("000001C8", String.format("%08X", Util.envelopeRegisterValue(0, 90)));

        assertEquals("01A41A00", String.format("%08X", Util.envelopeRegisterValue(2, 1)));
        assertEquals("01A41A08", String.format("%08X", Util.envelopeRegisterValue(2, 2)));
        assertEquals("01A41B00", String.format("%08X", Util.envelopeRegisterValue(2, 50)));

        assertEquals("002A0200", String.format("%08X", Util.envelopeRegisterValue(20, 1)));
        assertEquals("002A0208", String.format("%08X", Util.envelopeRegisterValue(20, 2)));
        assertEquals("002A0300", String.format("%08X", Util.envelopeRegisterValue(20, 50)));
    }

    public void testGetEnvelopeDurationFromValue() throws Exception {
        assertEquals(20.0, Util.envelopeLengthFromRegisterValue(0x002A0200L), 0.01);
        assertEquals(20.0, Util.envelopeLengthFromRegisterValue(0x002A0208L), 0.01);
        assertEquals(20.0, Util.envelopeLengthFromRegisterValue(0x002A02F8L), 0.01);
    }

    public void testGetNegativeEnvelopeDurationFromValue() throws Exception {
        assertEquals(-1344.0, Util.envelopeLengthFromRegisterValue(0xFFFF6008L), 1.0);
    }

    public void testGetEnvelopeResetFromValue() throws Exception {
        assertEquals(0.0, Util.envelopeResetFromRegisterValue(0x002A0200L), 0.01);
        assertEquals(1.56, Util.envelopeResetFromRegisterValue(0x002A0208L), 0.01);
        assertEquals(50.0, Util.envelopeResetFromRegisterValue(0x002A0300L), 0.01);
    }

    public void testEnvelopeResetAndVolumeAreEquals() throws Exception {
        long v = Util.volumeRegisterValue(50);
        long r = Util.envelopeRegisterValue(0, 50);
        assertEquals(String.format("%08X", v), String.format("%08X", r << 23));

        v = Util.volumeRegisterValue(90);
        r = Util.envelopeRegisterValue(0, 90);
        assertEquals(String.format("%08X", v), String.format("%08X", r << 23));
    }

    public void testGetFixedModulationValue() throws Exception {
        assertEquals("00000180", String.format("%08X", Util.modulationRegisterValue(0, 75)));
        assertEquals("00000100", String.format("%08X", Util.modulationRegisterValue(0, 50)));
        assertEquals("00000080", String.format("%08X", Util.modulationRegisterValue(0, 25)));
        assertEquals("00000040", String.format("%08X", Util.modulationRegisterValue(0, 12.5)));
    }

    public void testGetVariableModulationValue() throws Exception {
        assertEquals("00086600", String.format("%08X", Util.modulationRegisterValue(10, 0)));
        assertEquals("002A0200", String.format("%08X", Util.modulationRegisterValue(50, 0)));
    }
}
