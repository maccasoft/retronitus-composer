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

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

import com.maccasoft.composer.InstrumentChartCalculator;

public class Util {

    static final NumberFormat nf = NumberFormat.getInstance();

    public static final double SAMPLE_RATE = 78000.0;
    public static final double FREQUENCY_RATIO = Math.pow(2.0, 32.0) / SAMPLE_RATE;
    public static final double NOTE_RATIO = Math.pow(2.0, 1.0 / 12.0);
    public static final double NOTE_BASE = 261.626;

    public static final int FREQUENCY = 0;
    public static final int ENVELOPE = 1;
    public static final int VOLUME = 2;
    public static final int MODULATION = 3;

    static final List<String> noteText = Arrays.asList(
        "C-", "C#", "D-", "D#", "E-", "F-", "F#", "G-", "G#", "A-", "A#", "B-",
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");

    static final long noteValue[] = {
        0x36F47DD2, // C-10: 16744.1
        0x3A390CDD, // C#10: 17739.8
        0x3DAF59A3, // D-10: 18794.6
        0x415A5A6D, // D#10: 19912.2
        0x453D319F, // E-10: 21096.3
        0x495B3042, // F-10: 22350.7
        0x4DB7DBDF, // F#10: 23679.8
        0x5256EDAC, // G-10: 25087.8
        0x573C5945, // G#10: 26579.6
        0x5C6C4CAB, // A-10: 28160.1
        0x61EB36FB, // A#10: 29834.6
        0x67BDCA8A, // B-10: 31608.7
    };

    public static int noteIndex(String note) {
        int n = 0;
        int o = -1;
        for (String s : noteText) {
            if (note.startsWith(s)) {
                o = Integer.parseInt(note.substring(s.length()));
                break;
            }
            n++;
        }
        if (n >= 12) {
            n -= 12;
        }
        assert (n < 12 && o != -1);
        return ((o - 1) * 12) + n + 1;
    }

    public static double noteFrequency(String note) {
        return noteRegisterValue(note) / FREQUENCY_RATIO;
    }

    public static long noteRegisterValue(String note) {
        int n = 0;
        int o = -1;
        for (String s : noteText) {
            if (note.startsWith(s)) {
                o = Integer.parseInt(note.substring(s.length()));
                break;
            }
            n++;
        }
        if (n >= 12) {
            n -= 12;
        }
        assert (n < 12 && o != -1);
        return noteValue[n] >> (10 - o);
    }

    public static long frequencyRegisterValue(double frequency) {
        return (long) (frequency * FREQUENCY_RATIO);
    }

    public static double frequencyFromRegisterValue(long value) {
        return (int) value / FREQUENCY_RATIO;
    }

    public static long volumeRegisterValue(double volume) {
        return (long) ((volume / 100.0) * 64) << 26;
    }

    public static double volumeFromRegisterValue(long value) {
        return (((value >> 26) & 0x3FL) / 64.0) * 100.0;
    }

    public static long modulationRegisterValue(double variable, double fixed) {
        long v = (long) (variable * FREQUENCY_RATIO);
        long f = (long) ((fixed / 100.0) * 0x200) & 0x1FF;
        return (v & ~0x1FF) | (f & 0x1FF);
    }

    public static long envelopeRegisterValue(double duration, double reset) {
        long d = duration == 0 ? 0 : (long) (FREQUENCY_RATIO / (duration / 1000.0));
        long r = (long) ((reset / 100.0) * 64);
        return (d & ~0x1FF) | ((r << 3) & 0x1FF);
    }

    public static double envelopeLengthFromRegisterValue(long value) {
        return (InstrumentChartCalculator.FREQUENCY_RATIO / (int) (value & 0xFFFFFE00L)) * 1000.0;
    }

    public static double envelopeResetFromRegisterValue(long value) {
        return (((value & 0x1FF) / 8.0) / 64.0) * 100.0;
    }
}
