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

import com.maccasoft.composer.model.InstrumentBuilder;
import com.maccasoft.composer.model.MusicBuilder;
import com.maccasoft.composer.model.PatternBuilder;

import junit.framework.TestCase;

public class MusicTest extends TestCase {

    public void testEmptyMusic() throws Exception {
        MusicBuilder builder = new MusicBuilder("test");

        String expected = "0x00, 0x00, " + // Flag
        /* 00 */ "0x00, 0x00, " + // Instrument pointers (+ null terminator)
        /* 02 */ "0x00, 0x00, " + // Ch.0
        /* 04 */ "0x00, 0x00, " + // Ch.1
        /* 06 */ "0x00, 0x00, " + // Ch.2
        /* 08 */ "0x00, 0x00, " + // Ch.3
        /* 0A */ "0x00, 0x00, " + // Ch.4
        /* 0C */ "0x00, 0x00, " + // Ch.5
        /* 0E */ "0x00, 0x00, " + // Ch.6
        /* 10 */ "0x12, 0x00, " + // Ch.7
        /* 12 */ "0x1E, 0x80, 0x00, 0x00, " +
        /* 16 */ "0x00, 0x00, 0x00, 0x00, " +
        /* 1A */ "0xFF, 0xFF, 0xFF, 0xFF, " + // End of pattern sequences
        /* 1E */ "0x00,";
        assertEquals(expected, builder.build().toBinaryString());
    }

    public void testInstrument() throws Exception {
        MusicBuilder builder = new MusicBuilder("test") //
            .addInstrument(new InstrumentBuilder() // 0
                .setModulation(0, 50) //
                .setVolume(95) //
                .setEnvelope(2, 2).repeat(1) //
                .setEnvelope(-260, 2).repeat(210) //
                .setEnvelope(0, 0) //
                .setVolume(0) //
                .jump(-1)); //

        String expected = "0x00, 0x00, " + // Flag
        /* 00 */ "0x22, 0x00, " + // Instrument pointers (+ null terminator)
        /* 02 */ "0x00, 0x00, " +
        /* 04 */ "0x00, 0x00, " + // Ch.0
        /* 06 */ "0x00, 0x00, " + // Ch.1
        /* 08 */ "0x00, 0x00, " + // Ch.2
        /* 0A */ "0x00, 0x00, " + // Ch.3
        /* 0C */ "0x00, 0x00, " + // Ch.4
        /* 0E */ "0x00, 0x00, " + // Ch.5
        /* 10 */ "0x00, 0x00, " + // Ch.6
        /* 12 */ "0x16, 0x00, " + // Ch.7
        /* 14 */ "0x00, 0x00, " + // align to 4-bytes boundary
        /* 16 */ "0x5A, 0x80, 0x00, 0x00, " +
        /* 1A */ "0x00, 0x00, 0x00, 0x00, " +
        /* 1E */ "0xFF, 0xFF, 0xFF, 0xFF, " + // End of pattern sequences
        /* 22 */ "0x07, 0x00, 0x00, 0x00, " +
        /* 26 */ "0x00, 0x01, 0x00, 0x00, " +
        /* 2A */ "0x06, 0x00, 0x00, 0x00, " +
        /* 2E */ "0x00, 0x00, 0x00, 0xF0, " +
        /* 32 */ "0x15, 0x00, 0x00, 0x00, " +
        /* 36 */ "0x08, 0x1A, 0xA4, 0x01, " +
        /* 3A */ "0x25, 0x0D, 0x00, 0x00, " +
        /* 3E */ "0x08, 0xC4, 0xFC, 0xFF, " +
        /* 42 */ "0x05, 0x00, 0x00, 0x00, " +
        /* 46 */ "0x00, 0x00, 0x00, 0x00, " +
        /* 4A */ "0x06, 0x00, 0x00, 0x00, " +
        /* 4E */ "0x00, 0x00, 0x00, 0x00, " +
        /* 52 */ "0x00, 0x00, 0x00, 0x00, " +
        /* 56 */ "0xF8, 0xFF, 0xFF, 0xFF, " +
        /* 5A */ "0x00,";
        assertEquals(expected, builder.build().toBinaryString());
    }

    public void testPattern() throws Exception {
        InstrumentBuilder ins = new InstrumentBuilder().jump(-1);
        MusicBuilder builder = new MusicBuilder("test") //
            .addInstrument(ins) //
            .addPattern(0, new PatternBuilder("C#4", ins) //
                .play("C#4", 1) //
                .play("B-4", 16));

        String expected = "0x00, 0x00, " + // Flag
        /* 00 */ "0x2A, 0x00, " + // Instrument pointers (+ null terminator)
        /* 02 */ "0x00, 0x00, " +
        /* 04 */ "0x16, 0x00, " + // Ch.0
        /* 06 */ "0x00, 0x00, " + // Ch.1
        /* 08 */ "0x00, 0x00, " + // Ch.2
        /* 0A */ "0x00, 0x00, " + // Ch.3
        /* 0C */ "0x00, 0x00, " + // Ch.4
        /* 0E */ "0x00, 0x00, " + // Ch.5
        /* 10 */ "0x00, 0x00, " + // Ch.6
        /* 12 */ "0x1E, 0x00, " + // Ch.7
        /* 14 */ "0x00, 0x00, " + // align to 4-bytes boundary
        /* 16 */ "0x33, 0x00, 0x00, 0x61, " + // Patterns
        /* 1A */ "0x00, 0x00, 0x00, 0x00, " +
        /* 1E */ "0x32, 0x80, 0x00, 0x00, " +
        /* 22 */ "0x00, 0x00, 0x00, 0x00, " +
        /* 26 */ "0xFF, 0xFF, 0xFF, 0xFF, " + // End of pattern sequences
        /* 2A */ "0x00, 0x00, 0x00, 0x00, " + // Instrument 0
        /* 2E */ "0xF8, 0xFF, 0xFF, 0xFF, " +
        /* 32 */ "0x00, " + // Dummy tempo pattern
        /* 33 */ "0x61, 0xB7, 0xCF, 0xCA, 0x00,";

        assertEquals(expected, builder.build().toBinaryString());
    }

    public void testTriggersOnlyPattern() throws Exception {
        InstrumentBuilder ins = new InstrumentBuilder().jump(-1);
        MusicBuilder builder = new MusicBuilder("test") //
            .addInstrument(ins) //
            .addPattern(0, new PatternBuilder(ins) //
                .play("TR1", 1) //
                .play("TR2", 16));

        String expected = "0x00, 0x00, " + // Flag
        /* 00 */ "0x2A, 0x00, " + // Instrument pointers (+ null terminator)
        /* 02 */ "0x00, 0x00, " +
        /* 04 */ "0x16, 0x00, " + // Ch.0
        /* 06 */ "0x00, 0x00, " + // Ch.1
        /* 08 */ "0x00, 0x00, " + // Ch.2
        /* 0A */ "0x00, 0x00, " + // Ch.3
        /* 0C */ "0x00, 0x00, " + // Ch.4
        /* 0E */ "0x00, 0x00, " + // Ch.5
        /* 10 */ "0x00, 0x00, " + // Ch.6
        /* 12 */ "0x1E, 0x00, " + // Ch.7
        /* 14 */ "0x00, 0x00, " + // align to 4-bytes boundary
        /* 16 */ "0x33, 0x00, 0x00, 0x00, " + // Patterns
        /* 1A */ "0x00, 0x00, 0x00, 0x00, " +
        /* 1E */ "0x32, 0x80, 0x00, 0x00, " +
        /* 22 */ "0x00, 0x00, 0x00, 0x00, " +
        /* 26 */ "0xFF, 0xFF, 0xFF, 0xFF, " + // End of pattern sequences
        /* 2A */ "0x00, 0x00, 0x00, 0x00, " + // Instrument 0
        /* 2E */ "0xF8, 0xFF, 0xFF, 0xFF, " +
        /* 32 */ "0x00, " + // Dummy tempo pattern
        /* 33 */ "0xD1, 0xDF, 0xCF, 0xCA, 0x00,";

        assertEquals(expected, builder.build().toBinaryString());
    }
}
