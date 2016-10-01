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

package com.maccasoft.composer;

public class InstrumentChartCalculator {

    public static final int SAMPLE_RATE = 78000;
    public static final double SAMPLE_PERIOD = (1.0 / SAMPLE_RATE);
    public static final double VOL_MAX = Math.pow(2, 32);
    public static final double FREQUENCY_RATIO = Math.pow(2, 32) / SAMPLE_RATE;

    public static final int JUMP = 0x0;
    public static final int SET = 0x4;
    public static final int MODIFY = 0x8;

    public static final int FREQUENCY = 0x0;
    public static final int AMPLITUDE = 0x1;
    public static final int VOLUME = 0x2;
    public static final int MODULATION = 0x3;

    long c1_freq;
    long c1_ASD;
    long c1_vol;
    long c1_mod;

    long c1_volume;

    double[] frequency;
    double[] volume;
    double[] duty;
    double[] time;

    int[][] waveTable;
    int waveTablePointer;
    int repeatCounter;
    int[] repeatCmd;

    public InstrumentChartCalculator() {

    }

    public void setInitialValues(long c1_freq, long c1_ASD, long c1_volume, long c1_mod) {
        this.c1_freq = c1_freq;
        this.c1_ASD = c1_ASD;
        this.c1_volume = c1_volume;
        this.c1_mod = c1_mod;
    }

    public void setWaveTable(int[][] waveTable) {
        this.waveTable = waveTable;
    }

    public void play(int ms) {
        double t = 0.0;
        int length = (int) ((ms / 1000.0) / SAMPLE_PERIOD);

        length = ((length + 10) / 11) + 1;

        frequency = new double[length];
        volume = new double[length];
        duty = new double[length];
        time = new double[length];

        frequency[0] = c1_freq;
        volume[0] = c1_volume;
        time[0] = 0;

        waveTablePointer = 0;
        repeatCounter = 0;

        for (int i = 1; i < length; i++) {
            readCommand();
            for (int n = 0; n < 11; n++) {
                c1_volume = (c1_volume + c1_ASD) & 0xFFFFFFFFL; //                                  add   c1_volume, c1_ASD
                if ((c1_volume & 0xFC000000L) == 0) { //                                            test  c1_volume, volumeBits   wz
                    c1_volume = (c1_volume & ~0xFF800000L) | (c1_ASD & 0x1FFL) << 23; //    if_z    movi  c1_volume, c1_ASD
                }
                if (c1_volume > c1_vol) { //                                                        max   c1_volume, c1_vol
                    c1_volume = c1_vol;
                }

                t += SAMPLE_PERIOD;
            }
            frequency[i] = (c1_freq & 0xFFFFFFFFL) / FREQUENCY_RATIO;
            volume[i] = (c1_volume & 0xFFFFFFFFL) / VOL_MAX * 100.0;
            duty[i] = (c1_mod & 0x1FF) / 512.0 * 100.0;
            time[i] = t * 1000.0;
        }
    }

    void readCommand() {
        if (repeatCounter > 0) {
            repeatCounter--;
        }

        int[] ar = repeatCounter == 0 ? waveTable[waveTablePointer++] : repeatCmd;

        switch (ar[0] & 0xC) {
            case JUMP:
                waveTablePointer += ar[1] >> 3;
                break;
            case SET:
                switch (ar[0] & 0x3) {
                    case FREQUENCY:
                        c1_freq = ar[1] & 0xFFFFFFFFL;
                        break;
                    case AMPLITUDE:
                        c1_ASD = ar[1] & 0xFFFFFFFFL;
                        break;
                    case VOLUME:
                        c1_vol = ar[1] & 0xFFFFFFFFL;
                        break;
                    case MODULATION:
                        c1_mod = ar[1] & 0xFFFFFFFFL;
                        break;
                }
                break;
            case MODIFY:
                switch (ar[0] & 0x3) {
                    case FREQUENCY:
                        c1_freq = (c1_freq + (ar[1] & 0xFFFFFFFFL) & 0xFFFFFFFFL);
                        break;
                    case AMPLITUDE:
                        c1_ASD = (c1_ASD + (ar[1] & 0xFFFFFFFFL) & 0xFFFFFFFFL);
                        break;
                    case VOLUME:
                        c1_vol = (c1_vol + (ar[1] & 0xFFFFFFFFL) & 0xFFFFFFFFL);
                        break;
                    case MODULATION:
                        c1_mod = (c1_mod + (ar[1] & 0xFFFFFFFFL) & 0xFFFFFFFFL);
                        break;
                }
                break;
        }

        if (repeatCounter == 0) {
            repeatCounter = (int) ((ar[0] & 0xFFFFFFF0L) >> 4);
            if (repeatCounter != 0) {
                repeatCounter = (repeatCounter * 8) + 6;
            }
            repeatCmd = ar;
        }
    }

    public double[] getFrequency() {
        return frequency;
    }

    public double[] getVolume() {
        return volume;
    }

    public double[] getDuty() {
        return duty;
    }

    public double[] getTime() {
        return time;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < waveTable.length; i++) {
            sb.append(String.format("$%08X, $%08X\n", waveTable[i][0], waveTable[i][1]));
        }
        return sb.toString();
    }
}
