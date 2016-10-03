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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

public class InstrumentTest extends TestCase {

    public void testEqualsTo() throws Exception {
        Instrument subject1 = new InstrumentBuilder() // 0
            .setModulation(0, 50) //
            .setVolume(95) //
            .setEnvelope(2, 2).repeat(1) //
            .setEnvelope(-260, 2).repeat(210) //
            .setEnvelope(0, 0) //
            .setVolume(0) //
            .jump(-1) //
            .build();

        Instrument subject2 = new InstrumentBuilder() // 0
            .setModulation(0, 50) //
            .setVolume(95) //
            .setEnvelope(2, 2).repeat(1) //
            .setEnvelope(-260, 2).repeat(210) //
            .setEnvelope(0, 0) //
            .setVolume(0) //
            .jump(-1) //
            .build();

        assertNotSame(subject1, subject2);
        assertFalse(subject1.equals(subject2));
        assertTrue(subject1.equalsTo(subject2));
    }

    public void testCommandChangePropertyNotification() throws Exception {
        Instrument subject1 = new InstrumentBuilder() // 0
            .setModulation(0, 50) //
            .setVolume(95) //
            .setEnvelope(2, 2).repeat(1) //
            .setEnvelope(-260, 2).repeat(210) //
            .setEnvelope(0, 0) //
            .setVolume(0) //
            .jump(-1) //
            .build();

        Instrument subject2 = new InstrumentBuilder() // 0
            .setModulation(0, 50) //
            .setVolume(95) //
            .setEnvelope(2, 2).repeat(1) //
            .setEnvelope(-260, 2).repeat(210) //
            .setEnvelope(0, 0) //
            .setVolume(0) //
            .jump(-1) //
            .build();

        Instrument subject3 = new InstrumentBuilder() // 0
            .setModulation(0, 50) //
            .setVolume(95) //
            .setEnvelope(2, 2).repeat(1) //
            .setEnvelope(-270, 2).repeat(210) //
            .setEnvelope(0, 0) //
            .setVolume(0) //
            .jump(-1) //
            .build();

        final AtomicBoolean notified = new AtomicBoolean(false);
        subject1.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                notified.set(true);
            }
        });

        subject1.setCommands(subject2.getCommands());
        assertFalse(notified.get());

        subject1.setCommands(subject3.getCommands());
        assertTrue(notified.get());
    }
}
