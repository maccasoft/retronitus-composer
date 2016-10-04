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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import com.maccasoft.composer.DatabindingTestCase;

public class ProjectTest extends DatabindingTestCase {

    public void testSaveAndReadEmptyProject() throws Exception {
        ProjectBuilder builder = new ProjectBuilder();
        Project project = builder.build();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        project.writeTo(new PrintStream(os));

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        Project subject = new Project(new BufferedReader(new InputStreamReader(is)));

        assertEquals(project.instruments.size(), subject.instruments.size());
        assertEquals(project.songs.size(), subject.songs.size());
    }

    public void testSaveAndReadInstruments() throws Exception {
        ProjectBuilder builder = new ProjectBuilder() //
            .add(new InstrumentBuilder("Piano 1") //
                .setModulation(0, 50) //
                .setVolume(95) //
                .setEnvelope(2, 2).repeat(1) //
                .setEnvelope(-260, 2).repeat(210) //
                .setEnvelope(0, 0) //
                .setVolume(0) //
                .jump(-1)) //
            .add(new InstrumentBuilder("Piano 2") //
                .setModulation(0, 50) //
                .setVolume(95) //
                .setEnvelope(2, 2).repeat(1) //
                .setEnvelope(-260, 2).repeat(210) //
                .setEnvelope(0, 0) //
                .setVolume(0) //
                .jump(-1));
        Project project = builder.build();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        project.writeTo(new PrintStream(os));

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        Project subject = new Project(new BufferedReader(new InputStreamReader(is)));

        assertEquals(project.instruments.size(), subject.instruments.size());
        assertTrue(project.instruments.get(0).equalsTo(subject.instruments.get(0)));
        assertTrue(project.instruments.get(1).equalsTo(subject.instruments.get(1)));
        assertEquals(project.songs.size(), subject.songs.size());
    }

    public void testSaveAndReadInstrumentsId() throws Exception {
        ProjectBuilder builder = new ProjectBuilder() //
            .add(new InstrumentBuilder("ins00") //
                .setModulation(0, 50) //
                .setVolume(95) //
                .setEnvelope(2, 2).repeat(1) //
                .setEnvelope(-260, 2).repeat(210) //
                .setEnvelope(0, 0) //
                .setVolume(0) //
                .jump(-1)) //
            .add(new InstrumentBuilder("ins01") //
                .setModulation(0, 50) //
                .setVolume(95) //
                .setEnvelope(2, 2).repeat(1) //
                .setEnvelope(-260, 2).repeat(210) //
                .setEnvelope(0, 0) //
                .setVolume(0) //
                .jump(-1)) //
            .add(new InstrumentBuilder("ins02") //
                .setModulation(0, 50) //
                .setVolume(95) //
                .setEnvelope(2, 2).repeat(1) //
                .setEnvelope(-260, 2).repeat(210) //
                .setEnvelope(0, 0) //
                .setVolume(0) //
                .jump(-1));

        Project project = builder.build();
        project.remove(project.getInstrument("01"));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        project.writeTo(new PrintStream(os));

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        Project subject = new Project(new BufferedReader(new InputStreamReader(is)));

        assertEquals(project.instruments.size(), subject.instruments.size());
        assertTrue(project.instruments.get(0).equalsTo(subject.instruments.get(0)));
        assertTrue(project.instruments.get(1).equalsTo(subject.instruments.get(1)));
        assertEquals("00", subject.getInstrumentId(subject.instruments.get(0)));
        assertEquals("02", subject.getInstrumentId(subject.instruments.get(1)));
        assertEquals(project.songs.size(), subject.songs.size());
    }

    public void testSaveAndReadSong() throws Exception {
        ProjectBuilder builder = new ProjectBuilder() //
            .add(new SongBuilder("Intro", 120) //
                .row().play(0, "C#4", "00", "", "") //
                .row().play(0, "B-4", "00", "D16", ""));
        Project project = builder.build();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        project.writeTo(new PrintStream(os));

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        Project subject = new Project(new BufferedReader(new InputStreamReader(is)));

        assertEquals(project.instruments.size(), subject.instruments.size());
        assertEquals(project.songs.size(), subject.songs.size());
        assertEquals(project.songs.get(0), subject.songs.get(0));
    }

    public void testAddSongSetsDirty() throws Exception {
        Project subject = new Project();
        subject.add(new Song("Test", 120));
        assertTrue(subject.isDirty());
    }

    public void testAddInstrumentSetsDirty() throws Exception {
        Project subject = new Project();
        subject.add(new Instrument("Test"));
        assertTrue(subject.isDirty());
    }

    public void testEditSongSetsDirty() throws Exception {
        ProjectBuilder builder = new ProjectBuilder() //
            .add(new SongBuilder("Intro", 120) //
                .row().play(0, "C#4", "00", "", "") //
                .row().play(0, "B-4", "00", "D16", ""));
        Project subject = builder.build();

        subject.getSong(0).getRow(0).setNote(0, "E-4");
        assertTrue(subject.isDirty());
    }

    public void testGetInstrumentId() throws Exception {
        ProjectBuilder builder = new ProjectBuilder() //
            .add(new InstrumentBuilder("Ins00") //
                .jump(-1)) //
            .add(new InstrumentBuilder("Ins01") //
                .jump(-1)) //
            .add(new InstrumentBuilder("Ins03") //
                .jump(-1));
        Project subject = builder.build();

        assertEquals(3, subject.getInstruments().size());
        assertEquals("00", subject.getInstrumentId(subject.getInstruments().get(0)));
        assertEquals("01", subject.getInstrumentId(subject.getInstruments().get(1)));
        assertEquals("02", subject.getInstrumentId(subject.getInstruments().get(2)));
    }

    public void testAddInstrument() throws Exception {
        Instrument ins00 = new InstrumentBuilder("Ins00").jump(-1).build();
        Instrument ins01 = new InstrumentBuilder("Ins01").jump(-1).build();

        Project subject = new Project();

        subject.add(ins00);
        assertEquals(1, subject.getInstruments().size());
        assertEquals("00", subject.getInstrumentId(subject.getInstruments().get(0)));

        subject.add(ins01);
        assertEquals(2, subject.getInstruments().size());
        assertSame(ins00, subject.getInstruments().get(0));
        assertSame(ins01, subject.getInstruments().get(1));
        assertEquals("00", subject.getInstrumentId(subject.getInstruments().get(0)));
        assertEquals("01", subject.getInstrumentId(subject.getInstruments().get(1)));
    }

    public void testAddInstrumentFillsGap() throws Exception {
        Instrument ins00 = new InstrumentBuilder("Ins00").jump(-1).build();
        Instrument ins01 = new InstrumentBuilder("Ins01").jump(-1).build();
        Instrument ins02 = new InstrumentBuilder("Ins02").jump(-1).build();
        Instrument ins01b = new InstrumentBuilder("Ins01b").jump(-1).build();

        Project subject = new Project();
        subject.add(ins00);
        subject.add(ins01);
        subject.add(ins02);

        subject.remove(ins01);
        assertEquals(2, subject.getInstruments().size());
        assertSame(ins00, subject.getInstrument("00"));
        assertNull(subject.getInstrument("01"));
        assertSame(ins02, subject.getInstrument("02"));

        subject.add(ins01b);
        assertEquals(3, subject.getInstruments().size());
        assertSame(ins00, subject.getInstruments().get(0));
        assertSame(ins01b, subject.getInstruments().get(1));
        assertSame(ins02, subject.getInstruments().get(2));
        assertSame(ins00, subject.getInstrument("00"));
        assertSame(ins01b, subject.getInstrument("01"));
        assertSame(ins02, subject.getInstrument("02"));
    }

    public void testRemoveInstrument() throws Exception {
        Instrument ins00 = new InstrumentBuilder("Ins00").jump(-1).build();
        Instrument ins01 = new InstrumentBuilder("Ins01").jump(-1).build();
        Instrument ins02 = new InstrumentBuilder("Ins02").jump(-1).build();

        Project subject = new Project();
        subject.add(ins00);
        subject.add(ins01);
        subject.add(ins02);

        subject.remove(ins01);

        assertEquals(2, subject.getInstruments().size());
        assertEquals("00", subject.getInstrumentId(subject.getInstruments().get(0)));
        assertEquals("02", subject.getInstrumentId(subject.getInstruments().get(1)));
        assertNull(subject.getInstrument("01"));
    }
}
