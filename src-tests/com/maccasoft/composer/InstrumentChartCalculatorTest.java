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

import com.maccasoft.composer.InstrumentChartCalculator;

import junit.framework.TestCase;

public class InstrumentChartCalculatorTest extends TestCase {

    public void testReadCommandSetFrequency() throws Exception {
        InstrumentChartCalculator ins = new InstrumentChartCalculator();
        ins.waveTable = new int[][] {
            {
                InstrumentChartCalculator.SET | InstrumentChartCalculator.FREQUENCY, 100
            },
        };

        ins.readCommand();

        assertEquals(100, ins.c1_freq);
        assertEquals(0, ins.c1_ASD);
        assertEquals(0, ins.c1_vol);
        assertEquals(0, ins.c1_mod);
    }

    public void testReadCommandModifyFrequency() throws Exception {
        InstrumentChartCalculator ins = new InstrumentChartCalculator();
        ins.c1_freq = 100;
        ins.waveTable = new int[][] {
            {
                InstrumentChartCalculator.MODIFY | InstrumentChartCalculator.FREQUENCY, 100
            },
        };

        ins.readCommand();

        assertEquals(200, ins.c1_freq);
        assertEquals(0, ins.c1_ASD);
        assertEquals(0, ins.c1_vol);
        assertEquals(0, ins.c1_mod);
    }

    public void testReadCommandSetVolume() throws Exception {
        InstrumentChartCalculator ins = new InstrumentChartCalculator();
        ins.waveTable = new int[][] {
            {
                InstrumentChartCalculator.MODIFY | InstrumentChartCalculator.VOLUME, 100
            },
        };

        ins.readCommand();

        assertEquals(0, ins.c1_freq);
        assertEquals(0, ins.c1_ASD);
        assertEquals(100, ins.c1_vol);
        assertEquals(0, ins.c1_mod);
    }

    public void testReadCommandModifyVolume() throws Exception {
        InstrumentChartCalculator ins = new InstrumentChartCalculator();
        ins.c1_vol = 100;
        ins.waveTable = new int[][] {
            {
                InstrumentChartCalculator.MODIFY | InstrumentChartCalculator.VOLUME, 100
            },
        };

        ins.readCommand();

        assertEquals(0, ins.c1_freq);
        assertEquals(0, ins.c1_ASD);
        assertEquals(200, ins.c1_vol);
        assertEquals(0, ins.c1_mod);
    }

    public void testReadCommandRepeat() throws Exception {
        InstrumentChartCalculator ins = new InstrumentChartCalculator();
        ins.waveTable = new int[][] {
            {
                (1 << 4) | InstrumentChartCalculator.MODIFY | InstrumentChartCalculator.FREQUENCY, 100
            },
            {
                InstrumentChartCalculator.SET | InstrumentChartCalculator.FREQUENCY, 0
            },
        };

        for (int i = 0; i < (1 * 8) + 6; i++) {
            ins.readCommand();
        }
        assertEquals(1400, ins.c1_freq);
        assertEquals(1, ins.waveTablePointer);

        ins.readCommand();
        assertEquals(0, ins.c1_freq);
        assertEquals(2, ins.waveTablePointer);
    }

    public void testReadCommandJump() throws Exception {
        InstrumentChartCalculator ins = new InstrumentChartCalculator();
        ins.c1_freq = 100;
        ins.waveTable = new int[][] {
            {
                InstrumentChartCalculator.MODIFY | InstrumentChartCalculator.FREQUENCY, 100
            },
            {
                InstrumentChartCalculator.JUMP, -1 << 3
            },
        };

        ins.readCommand();
        assertEquals(200, ins.c1_freq);
        assertEquals(1, ins.waveTablePointer);

        ins.readCommand();
        assertEquals(200, ins.c1_freq);
        assertEquals(1, ins.waveTablePointer);
    }

    public void testPlay() throws Exception {
        InstrumentChartCalculator ins = new InstrumentChartCalculator();
        ins.waveTable = new int[][] {
            {
                InstrumentChartCalculator.SET | InstrumentChartCalculator.FREQUENCY, 0x01A41A41
            },
            {
                InstrumentChartCalculator.SET | InstrumentChartCalculator.FREQUENCY, 0x00A80A80
            },
            {
                InstrumentChartCalculator.MODIFY | InstrumentChartCalculator.FREQUENCY, 0x00540540
            },
            {
                InstrumentChartCalculator.JUMP, -2 << 3
            },
        };

        ins.play(1);

        assertEquals(9, ins.frequency.length);
        assertEquals(9, ins.volume.length);
        assertEquals(9, ins.time.length);

        assertEquals(0.0, ins.frequency[0]);
        assertEquals(500.0, ins.frequency[1], 0.1);
        assertEquals(200.0, ins.frequency[2], 0.1);
        assertEquals(300.0, ins.frequency[3], 0.1);
        assertEquals(300.0, ins.frequency[4], 0.1);
        assertEquals(400.0, ins.frequency[5], 0.1);
        assertEquals(400.0, ins.frequency[6], 0.1);
        assertEquals(500.0, ins.frequency[7], 0.1);
        assertEquals(500.0, ins.frequency[8], 0.1);
    }
}
