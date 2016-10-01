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

import com.maccasoft.composer.model.Command;
import com.maccasoft.composer.model.Util;

import junit.framework.TestCase;

public class CommandTest extends TestCase {

    public void testSetFrequency() throws Exception {
        Command subject = new Command();
        subject.setFrequency(261.626);

        assertEquals("", subject.getFrequencyNote());
        assertEquals(261.626, subject.getFrequency());
        assertEquals("00DBD1CB", String.format("%08X", subject.getFrequencyRegister()));
        assertEquals(0x00DBD1CB, subject.registerValue[Util.FREQUENCY]);
    }

    public void testSetNote() throws Exception {
        Command subject = new Command();
        subject.setFrequencyNote("C-4");

        assertEquals("C-4", subject.getFrequencyNote());
        assertEquals(261.626, subject.getFrequency(), 0.01);
        assertEquals("00DBD1F7", String.format("%08X", subject.getFrequencyRegister()));
        assertEquals(0x00DBD1F7, subject.registerValue[Util.FREQUENCY]);
    }

    public void testSetFrequencyRegister() throws Exception {
        Command subject = new Command();
        subject.setFrequencyRegister(0x00DBD1F7);

        assertEquals("", subject.getFrequencyNote());
        assertEquals(261.626, subject.getFrequency(), 0.01);
        assertEquals("00DBD1F7", String.format("%08X", subject.getFrequencyRegister()));
        assertEquals(0x00DBD1F7, subject.registerValue[Util.FREQUENCY]);
    }

    public void testFrequencyToString() throws Exception {
        Command subject = new Command();
        subject.setAction(Command.SET);
        subject.setFrequencyNote("C-4");

        subject.setProperty(Command.FREQUENCY);
        assertEquals("SET_FREQUENCY(261.63)", subject.toString());
        assertEquals("<set-frequency value=\"261.63\" />", subject.toXmlString());

        subject.setProperty(Command.FREQUENCY_NOTE);
        assertEquals("SET_FREQUENCY_NOTE(\"C-4\")", subject.toString());
        assertEquals("<set-frequency note=\"C-4\" />", subject.toXmlString());

        subject.setProperty(Command.FREQUENCY_REGISTER);
        assertEquals("SET|FREQUENCY,          0x00DBD1F7", subject.toCString());
        assertEquals("<set-register frequency=\"00DBD1F7\" />", subject.toXmlString());
    }

    public void testSetVolume() throws Exception {
        Command subject = new Command();
        subject.setVolume(95.0);

        assertEquals(95.0, subject.getVolume());
        assertEquals("F0000000", String.format("%08X", subject.getVolumeRegister()));
        assertEquals(0xF0000000L, subject.registerValue[Util.VOLUME]);
    }

    public void testSetVolumeRegister() throws Exception {
        Command subject = new Command();
        subject.setVolumeRegister(0xF0000000L);

        assertEquals(93.75, subject.getVolume(), 0.01);
        assertEquals("F0000000", String.format("%08X", subject.getVolumeRegister()));
        assertEquals(0xF0000000L, subject.registerValue[Util.VOLUME]);
    }

    public void testVolumeToString() throws Exception {
        Command subject = new Command();
        subject.setAction(Command.SET);
        subject.setVolume(95.0);

        subject.setProperty(Command.VOLUME);
        assertEquals("SET_VOLUME(95)", subject.toString());
        assertEquals("<set-volume value=\"95\" />", subject.toXmlString());

        subject.setProperty(Command.VOLUME_REGISTER);
        assertEquals("SET|VOLUME, 0xF0000000", subject.toString());
        assertEquals("<set-register volume=\"F0000000\" />", subject.toXmlString());
    }

    public void testSetModulation() throws Exception {
        Command subject = new Command();

        subject.setDutyCycle(50.0);
        subject.setModulation(0.0);
        assertEquals("00000100", String.format("%08X", subject.getModulationRegister()));
        assertEquals(0x00000100, subject.registerValue[Util.MODULATION]);

        subject.setDutyCycle(0.0);
        subject.setModulation(100.0);
        assertEquals("00540400", String.format("%08X", subject.getModulationRegister()));
        assertEquals(0x00540400, subject.registerValue[Util.MODULATION]);

        subject.setDutyCycle(50.0);
        subject.setModulation(100.0);
        assertEquals("00540500", String.format("%08X", subject.getModulationRegister()));
        assertEquals(0x00540500, subject.registerValue[Util.MODULATION]);
    }

    public void testSetModulationRegister() throws Exception {
        Command subject = new Command();

        subject.setModulationRegister(0x00000100);
        assertEquals(50.0, subject.getDutyCycle());
        assertEquals(0.0, subject.getModulation());

        subject.setModulationRegister(0x00540400);
        assertEquals(0.0, subject.getDutyCycle());
        assertEquals(100.0, subject.getModulation(), 0.1);

        subject.setModulationRegister(0x00540500);
        assertEquals(50.0, subject.getDutyCycle());
        assertEquals(100.0, subject.getModulation(), 0.1);
    }

    public void testModulationToString() throws Exception {
        Command subject = new Command();
        subject.setAction(Command.SET);
        subject.setDutyCycle(50.0);
        subject.setModulation(100.0);

        subject.setProperty(Command.MODULATION);
        assertEquals("SET_MODULATION(100, 50)", subject.toString());
        assertEquals("<set-modulation frequency=\"100\" fixed=\"50\" />", subject.toXmlString());

        subject.setProperty(Command.MODULATION_REGISTER);
        assertEquals("SET|MODULATION, 0x00540500", subject.toString());
        assertEquals("<set-register modulation=\"00540500\" />", subject.toXmlString());
    }

    public void testSetEnvelope() throws Exception {
        Command subject = new Command();

        subject.setEnvelopeLength(500.0);
        subject.setEnvelopeReset(0.0);
        assertEquals("0001AE00", String.format("%08X", subject.getEnvelopeRegister()));
        assertEquals(0x0001AE00, subject.registerValue[Util.ENVELOPE]);

        subject.setEnvelopeLength(0.0);
        subject.setEnvelopeReset(75.0);
        assertEquals("00000180", String.format("%08X", subject.getEnvelopeRegister()));
        assertEquals(0x00000180, subject.registerValue[Util.ENVELOPE]);

        subject.setEnvelopeLength(500.0);
        subject.setEnvelopeReset(75.0);
        assertEquals("0001AF80", String.format("%08X", subject.getEnvelopeRegister()));
        assertEquals(0x0001AF80, subject.registerValue[Util.ENVELOPE]);

        subject.setEnvelopeLength(-500.0);
        subject.setEnvelopeReset(2.0);
        assertEquals("FFFE5008", String.format("%08X", subject.getEnvelopeRegister()));
        assertEquals(0xFFFE5008, subject.registerValue[Util.ENVELOPE]);
    }

    public void testSetEnvelopeRegister() throws Exception {
        Command subject = new Command();

        subject.setEnvelopeRegister(0x0001AF80);
        assertEquals(500.0, subject.getEnvelopeLength(), 0.5);
        assertEquals(75.0, subject.getEnvelopeReset(), 0.1);

        subject.setEnvelopeRegister(0xFFFE5008);
        assertEquals(-497.8, subject.getEnvelopeLength(), 0.5);
        assertEquals(1.56, subject.getEnvelopeReset(), 0.01);
    }

    public void testEnvelopeToString() throws Exception {
        Command subject = new Command();
        subject.setAction(Command.SET);
        subject.setEnvelopeLength(500.0);
        subject.setEnvelopeReset(75.0);

        subject.setProperty(Command.ENVELOPE);
        assertEquals("SET_ENVELOPE(500, 75)", subject.toString());
        assertEquals("<set-envelope length=\"500\" reset=\"75\" />", subject.toXmlString());

        subject.setProperty(Command.ENVELOPE_REGISTER);
        assertEquals("SET|ENVELOPE, 0x0001AE00|0x180", subject.toString());
        assertEquals("<set-register envelope=\"0001AF80\" />", subject.toXmlString());
    }

    public void testGetFrequencyCommand() throws Exception {
        Command subject = new Command();
        subject.setAction(Command.SET);
        subject.setProperty(Command.FREQUENCY);
        subject.setFrequencyNote("C-4");

        assertEquals("00000004", String.format("%08X", subject.getCommand()));
        assertEquals("00DBD1F7", String.format("%08X", subject.getArgument()));
    }

    public void testGetEnvelopeCommand() throws Exception {
        Command subject = new Command();
        subject.setAction(Command.SET);
        subject.setProperty(Command.ENVELOPE);
        subject.setEnvelopeLength(500.0);
        subject.setEnvelopeReset(75.0);

        assertEquals("00000005", String.format("%08X", subject.getCommand()));
        assertEquals("0001AF80", String.format("%08X", subject.getArgument()));
    }

    public void testGetVolumeCommand() throws Exception {
        Command subject = new Command();
        subject.setAction(Command.SET);
        subject.setProperty(Command.VOLUME);
        subject.setVolume(25.0);

        assertEquals("00000006", String.format("%08X", subject.getCommand()));
        assertEquals("40000000", String.format("%08X", subject.getArgument()));
    }

    public void testGetModulationCommand() throws Exception {
        Command subject = new Command();
        subject.setAction(Command.SET);
        subject.setProperty(Command.MODULATION);
        subject.setDutyCycle(50.0);

        assertEquals("00000007", String.format("%08X", subject.getCommand()));
        assertEquals("00000100", String.format("%08X", subject.getArgument()));
    }

    public void testFrequencyXml() throws Exception {
        Command subject = new Command();
        subject.setAction(Command.SET);
        subject.setProperty(Command.FREQUENCY);
        subject.setFrequency(550);

        String line = subject.toXmlString();

        assertEquals(line, Command.fromXml(line).toXmlString());
    }

    public void testFrequencyNoteXml() throws Exception {
        Command subject = new Command();
        subject.setAction(Command.SET);
        subject.setProperty(Command.FREQUENCY_NOTE);
        subject.setFrequencyNote("C-4");

        String line = subject.toXmlString();

        assertEquals(line, Command.fromXml(line).toXmlString());
    }

    public void testEnvelopeXml() throws Exception {
        Command subject = new Command();
        subject.setAction(Command.SET);
        subject.setProperty(Command.ENVELOPE);
        subject.setEnvelopeLength(500.0);
        subject.setEnvelopeReset(75.0);

        String line = subject.toXmlString();

        assertEquals(line, Command.fromXml(line).toXmlString());
    }

    public void testVolumeXml() throws Exception {
        Command subject = new Command();
        subject.setAction(Command.SET);
        subject.setProperty(Command.VOLUME);
        subject.setVolume(25.0);

        String line = subject.toXmlString();

        assertEquals(line, Command.fromXml(line).toXmlString());
    }

    public void testModulationXml() throws Exception {
        Command subject = new Command();
        subject.setAction(Command.SET);
        subject.setProperty(Command.MODULATION);
        subject.setDutyCycle(50.0);

        String line = subject.toXmlString();

        assertEquals(line, Command.fromXml(line).toXmlString());
    }

    public void testBackwardJump() throws Exception {
        Command subject = new Command();
        subject.setAction(Command.JUMP);
        subject.setValue(-1);

        assertEquals("00000000", String.format("%08X", subject.getCommand()));
        assertEquals("FFFFFFF8", String.format("%08X", subject.getArgument()));
    }

    public void testForwardJump() throws Exception {
        Command subject = new Command();
        subject.setAction(Command.JUMP);
        subject.setValue(1);

        assertEquals("00000000", String.format("%08X", subject.getCommand()));
        assertEquals("00000008", String.format("%08X", subject.getArgument()));
    }
}
