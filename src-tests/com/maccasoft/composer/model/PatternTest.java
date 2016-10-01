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

import com.maccasoft.composer.model.Note;
import com.maccasoft.composer.model.Pattern;

import junit.framework.TestCase;

public class PatternTest extends TestCase {

    public void testNote() throws Exception {
        Pattern subject = new Pattern();
        subject.setBaseNote("C-4");
        subject.sequence.add(new Note("D-4"));

        String expected = "" +
            "uint8_t [] = {\n" +
            "    ((12 +2) << 3) | 0,\n" +
            "    0\n" +
            "};\n";
        assertEquals(expected, subject.toCString(""));
    }

    public void testNoteRepeat() throws Exception {
        Pattern subject = new Pattern();
        subject.setBaseNote("C-4");
        subject.sequence.add(new Note("D-4", 2));

        String expected = "" +
            "uint8_t [] = {\n" +
            "    ((12 +2) << 3) | 2,\n" +
            "    0\n" +
            "};\n";
        assertEquals(expected, subject.toCString(""));
    }

    public void testNoteRepeatMoreThanAllowed() throws Exception {
        Pattern subject = new Pattern();
        subject.setBaseNote("C-4");
        subject.sequence.add(new Note("D-4", 12));

        String expected = "" +
            "uint8_t [] = {\n" +
            "    ((12 +2) << 3) | 7,\n" +
            "    (25 << 3) | 5,\n" +
            "    0\n" +
            "};\n";
        assertEquals(expected, subject.toCString(""));
    }

    public void testRest() throws Exception {
        Pattern subject = new Pattern();
        subject.setBaseNote("C-4");
        subject.sequence.add(new Note(""));

        String expected = "" +
            "uint8_t [] = {\n" +
            "    (25 << 3) | 0,\n" +
            "    0\n" +
            "};\n";
        assertEquals(expected, subject.toCString(""));
    }

    public void testRestRepeat() throws Exception {
        Pattern subject = new Pattern();
        subject.setBaseNote("C-4");
        subject.sequence.add(new Note("", 2));

        String expected = "" +
            "uint8_t [] = {\n" +
            "    (25 << 3) | 2,\n" +
            "    0\n" +
            "};\n";
        assertEquals(expected, subject.toCString(""));
    }

    public void testRestRepeatMoreThanAllowed() throws Exception {
        Pattern subject = new Pattern();
        subject.setBaseNote("C-4");
        subject.sequence.add(new Note("", 12));

        String expected = "" +
            "uint8_t [] = {\n" +
            "    (25 << 3) | 7,\n" +
            "    (25 << 3) | 5,\n" +
            "    0\n" +
            "};\n";
        assertEquals(expected, subject.toCString(""));
    }
}
