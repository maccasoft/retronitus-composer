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
import com.maccasoft.composer.model.InstrumentBuilder;
import com.maccasoft.composer.model.Project;
import com.maccasoft.composer.model.ProjectBuilder;
import com.maccasoft.composer.model.SongBuilder;

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
}
