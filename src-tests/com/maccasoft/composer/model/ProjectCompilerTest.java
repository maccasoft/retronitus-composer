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

import com.maccasoft.composer.DatabindingTestCase;
import com.maccasoft.composer.model.InstrumentBuilder;
import com.maccasoft.composer.model.Music;
import com.maccasoft.composer.model.Pattern;
import com.maccasoft.composer.model.Project;
import com.maccasoft.composer.model.ProjectBuilder;
import com.maccasoft.composer.model.ProjectCompiler;
import com.maccasoft.composer.model.SongBuilder;

public class ProjectCompilerTest extends DatabindingTestCase {

    public void testCompileEmptyProject() throws Exception {
        Project project = new ProjectBuilder() //
            .add(new SongBuilder()) //
            .build();

        Music music = new ProjectCompiler(project).build(project.getSong(0));

        assertEquals(0, music.instruments.size());
        assertNull(music.channels[0]);
        assertNull(music.channels[1]);
        assertNull(music.channels[2]);
        assertNull(music.channels[3]);
        assertNull(music.channels[4]);
        assertNull(music.channels[5]);
        assertNull(music.channels[6]);
        assertNull(music.channels[7]);
    }

    public void testCompileSongInstruments() throws Exception {
        Project project = new ProjectBuilder() //
            .add(new InstrumentBuilder("ins00") // 00
                .setModulation(0, 50) //
                .setVolume(95) //
                .setEnvelope(2, 2).repeat(1) //
                .jump(-1)) //
            .add(new InstrumentBuilder("ins01") // 01
                .setModulation(0, 50) //
                .setVolume(95) //
                .setEnvelope(2, 2).repeat(1) //
                .setEnvelope(-260, 2).repeat(210) //
                .setEnvelope(0, 0) //
                .setVolume(0) //
                .jump(-1)) //
            .add(new SongBuilder() //
                .row().play(0, "C-4", "01", "", "")) //
            .build();

        Music music = new ProjectCompiler(project).build(project.getSong(0));

        assertEquals(1, music.instruments.size());
        assertEquals(project.instruments.get(1), music.instruments.get(0));
        assertEquals(1, music.channels[0].patterns.size());
        assertNull(music.channels[1]);
        assertNull(music.channels[2]);
        assertNull(music.channels[3]);
        assertNull(music.channels[4]);
        assertNull(music.channels[5]);
        assertNull(music.channels[6]);
        assertNull(music.channels[7]);
    }

    public void testCompilePatternWithTwoInstruments() throws Exception {
        Project project = new ProjectBuilder() //
            .add(new InstrumentBuilder("ins00") // 00
                .setModulation(0, 50) //
                .setVolume(95) //
                .setEnvelope(2, 2).repeat(1) //
                .jump(-1)) //
            .add(new InstrumentBuilder("ins01") // 01
                .setModulation(0, 50) //
                .setVolume(95) //
                .setEnvelope(2, 2).repeat(1) //
                .setEnvelope(-260, 2).repeat(210) //
                .setEnvelope(0, 0) //
                .setVolume(0) //
                .jump(-1)) //
            .add(new SongBuilder() //
                .row().play(0, "C-4", "01", "", "") //
                .row().play(0, "C-4", "00", "", "")) //
            .build();

        Music music = new ProjectCompiler(project).build(project.getSong(0));

        assertEquals(2, music.instruments.size());
        assertEquals(project.instruments.get(1), music.instruments.get(0));
        assertEquals(project.instruments.get(0), music.instruments.get(1));
        assertEquals(2, music.channels[0].patterns.size());
        assertNull(music.channels[1]);
        assertNull(music.channels[2]);
        assertNull(music.channels[3]);
        assertNull(music.channels[4]);
        assertNull(music.channels[5]);
        assertNull(music.channels[6]);
        assertNull(music.channels[7]);
    }

    public void testCompileNotePattern() throws Exception {
        Project project = new ProjectBuilder() //
            .add(new InstrumentBuilder("ins00") // 00
                .setModulation(0, 50) //
                .setVolume(95) //
                .setEnvelope(2, 2).repeat(1) //
                .jump(-1)) //
            .add(new SongBuilder() //
                .row().play(0, "C-4", "00", "", "") //
                .row().play(0, "D-4", "00", "", "")) //
            .build();

        Music music = new ProjectCompiler(project).build(project.getSong(0));
        Pattern pattern = music.channels[0].patterns.get(0);

        assertEquals(2, pattern.sequence.size());
        assertEquals(project.instruments.get(0), pattern.instrument);
        assertEquals("C-4", pattern.baseNote);
        assertEquals("C-4", pattern.sequence.get(0).getNote());
        assertEquals("D-4", pattern.sequence.get(1).getNote());
    }

    public void testCompileNoteDelayEffect() throws Exception {
        Project project = new ProjectBuilder() //
            .add(new InstrumentBuilder("ins00") // 00
                .setModulation(0, 50) //
                .setVolume(95) //
                .setEnvelope(2, 2).repeat(1) //
                .jump(-1)) //
            .add(new SongBuilder() //
                .row().play(0, "C-4", "00", "", "") //
                .row().play(0, "D-4", "00", "W16", "")) //
            .build();

        Music music = new ProjectCompiler(project).build(project.getSong(0));
        Pattern pattern = music.channels[0].patterns.get(0);

        assertEquals(2, pattern.sequence.size());
        assertEquals(project.instruments.get(0), pattern.instrument);
        assertEquals("C-4", pattern.baseNote);
        assertEquals("C-4", pattern.sequence.get(0).getNote());
        assertEquals(0, pattern.sequence.get(0).getWait());
        assertEquals("D-4", pattern.sequence.get(1).getNote());
        assertEquals(16, pattern.sequence.get(1).getWait());
    }

    public void testCompileTriggerEffect() throws Exception {
        Project project = new ProjectBuilder() //
            .add(new InstrumentBuilder("ins00") // 00
                .setModulation(0, 50) //
                .setVolume(95) //
                .setEnvelope(2, 2).repeat(1) //
                .jump(-1)) //
            .add(new SongBuilder() //
                .row().play(0, "", "00", "TR1", "") //
                .row().play(0, "", "00", "TR2", "")) //
            .build();

        Music music = new ProjectCompiler(project).build(project.getSong(0));
        Pattern pattern = music.channels[0].patterns.get(0);

        assertEquals(2, pattern.sequence.size());
        assertEquals(project.instruments.get(0), pattern.instrument);
        assertNull(pattern.baseNote);
        assertEquals("TR1", pattern.sequence.get(0).getNote());
        assertEquals("TR2", pattern.sequence.get(1).getNote());
    }

    public void testCompileMixedEffects() throws Exception {
        Project project = new ProjectBuilder() //
            .add(new InstrumentBuilder("ins00") // 00
                .setModulation(0, 50) //
                .setVolume(95) //
                .setEnvelope(2, 2).repeat(1) //
                .jump(-1)) //
            .add(new SongBuilder() //
                .row().play(0, "C-4", "00", "TR1", "W12") //
                .row().play(0, "D-4", "00", "", "")) //
            .build();

        Music music = new ProjectCompiler(project).build(project.getSong(0));
        Pattern pattern = music.channels[0].patterns.get(0);

        assertEquals(2, pattern.sequence.size());
        assertEquals(project.instruments.get(0), pattern.instrument);
        assertEquals("D-4", pattern.baseNote);
        assertEquals("TR1", pattern.sequence.get(0).getNote());
        assertEquals(12, pattern.sequence.get(0).getWait());
        assertEquals("D-4", pattern.sequence.get(1).getNote());
    }

    public void testCompactPauses() throws Exception {
        Project project = new ProjectBuilder() //
            .add(new InstrumentBuilder("ins00") // 00
                .setModulation(0, 50) //
                .setVolume(95) //
                .setEnvelope(2, 2).repeat(1) //
                .jump(-1)) //
            .add(new SongBuilder() //
                .row().play(0, "C-4", "00", "", "") //
                .row().play(0, "", "00", "", "") //
                .row().play(0, "", "00", "", "") //
                .row().play(0, "D-4", "00", "", "") //
                .row().play(0, "", "00", "", "") //
                .row().play(0, "", "00", "", "") //
                .row().play(0, "", "00", "", "")) //
            .build();

        Music music = new ProjectCompiler(project).build(project.getSong(0));
        Pattern pattern = music.channels[0].patterns.get(0);

        assertEquals(2, pattern.sequence.size());
        assertEquals(2, pattern.sequence.get(0).getWait());
        assertEquals(3, pattern.sequence.get(1).getWait());
    }
}
