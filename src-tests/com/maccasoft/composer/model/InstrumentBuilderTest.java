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

import com.maccasoft.composer.model.Instrument;
import com.maccasoft.composer.model.InstrumentBuilder;

import junit.framework.TestCase;

public class InstrumentBuilderTest extends TestCase {

    public void testInstrumentBuilderCString() throws Exception {
        Instrument subject = new InstrumentBuilder().setFrequency(500).modifyFrequency(100).repeat(10).build();
        assertEquals("" +
            "SET|FREQUENCY,          0x01A41A41\n" +
            "MODIFY|FREQUENCY|REP(10), 0x00540540\n", subject.toCString());
    }
}
