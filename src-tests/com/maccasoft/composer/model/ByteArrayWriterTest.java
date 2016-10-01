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

import com.maccasoft.composer.model.ByteArrayWriter;

import junit.framework.TestCase;

public class ByteArrayWriterTest extends TestCase {

    public void testWriteByte() throws Exception {
        ByteArrayWriter subject = new ByteArrayWriter();
        subject.writeByte(0x01);
        subject.writeByte(0x02);

        assertEquals("0x01, 0x02,", subject.toString());
    }

    public void testWriteWord() throws Exception {
        ByteArrayWriter subject = new ByteArrayWriter();
        subject.writeWord(0x0102);

        assertEquals("0x02, 0x01,", subject.toString());
    }

    public void testWriteLong() throws Exception {
        ByteArrayWriter subject = new ByteArrayWriter();
        subject.writeLong(0x01020304);

        assertEquals("0x04, 0x03, 0x02, 0x01,", subject.toString());
    }

    public void testOverwriteByte() throws Exception {
        ByteArrayWriter subject = new ByteArrayWriter();
        subject.writeByte(0x01);
        subject.writeByte(0x02);

        assertEquals("0x01, 0x02,", subject.toString());

        subject.writeByte(0, 0x03);

        assertEquals("0x03, 0x02,", subject.toString());
    }

    public void testWriteByteIndex() throws Exception {
        ByteArrayWriter subject = new ByteArrayWriter();
        subject.writeByte(0, 0x01);
        subject.writeByte(1, 0x02);

        assertEquals("0x01, 0x02,", subject.toString());
    }
}
