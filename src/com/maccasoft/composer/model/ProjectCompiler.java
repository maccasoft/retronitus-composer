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

import java.util.HashMap;
import java.util.Map;

public class ProjectCompiler {

    final Project project;

    Map<String, Instrument> instrumentsMap = new HashMap<String, Instrument>();

    public ProjectCompiler(Project project) {
        this.project = project;

        int id = 0;
        for (Instrument ins : project.getInstruments()) {
            instrumentsMap.put(String.format("%02X", id), ins);
            id++;
        }
    }

    public Music build(Song song) throws ProjectException {
        Music music = new Music(song.getName(), song.getBpm());

        for (SongRow row : song.getRows()) {
            for (int ch = 0; ch < 8; ch++) {
                Instrument ins = instrumentsMap.get(row.getInstrument(ch));
                if (ins != null) {
                    music.add(ins);
                }
            }
        }

        for (int ch = 0; ch < 8; ch++) {
            Channel channel = new Channel();

            int noteIndex = 0;
            Pattern pattern = new Pattern();

            for (SongRow row : song.getRows()) {
                String fx1 = row.getFx1(ch);
                String fx2 = row.getFx2(ch);

                Note note = new Note(row.getNote(ch));
                if (fx1.startsWith("TR")) {
                    note.setNote(fx1);
                }
                else if (fx2.startsWith("TR")) {
                    note.setNote(fx2);
                }

                Instrument ins = instrumentsMap.get(row.getInstrument(ch));
                if ((note.isNote() || note.isTrigger()) && ins == null) {
                    throw new InvalidInstrumentException(project, song, row, ch);
                }

                note.setWait(getWaitFrames(fx1, fx2));

                if (!"".equals(row.getInstrument(ch))) {
                    if (pattern.getInstrument() != null && pattern.getInstrument() != ins) {
                        channel.add(pattern);
                        pattern = new Pattern();
                    }
                    pattern.setInstrument(ins);
                }

                if (note.isNote() && pattern.getBaseNote() == null) {
                    pattern.setBaseNote(note.getNote());
                    noteIndex = Util.noteIndex(note.getNote());
                }

                if (note.isPause() && pattern.getNoteCount() > 0) {
                    Note prevNote = pattern.getNote(pattern.getNoteCount() - 1);
                    prevNote.setWait(prevNote.getWait() + note.getWait() + 1);
                }
                else {
                    if (note.isNote()) {
                        int delta = Util.noteIndex(note.getNote()) - noteIndex;
                        if (delta < -12 || delta > 12) {
                            channel.add(pattern);
                            pattern = new Pattern(note.getNote(), pattern.getInstrument());
                        }
                        noteIndex = Util.noteIndex(note.getNote());
                    }
                    pattern.add(note);
                }
            }
            if (pattern.getInstrument() != null) {
                channel.add(pattern);
            }

            if (channel.getPatterns().size() != 0) {
                music.setChannel(ch, channel);
            }
        }

        return music;
    }

    int getWaitFrames(String fx1, String fx2) {
        String s = "0";

        if (fx1.startsWith("W")) {
            s = fx1.substring(1);
        }
        else if (fx2.startsWith("W")) {
            s = fx2.substring(1);
        }

        return Integer.valueOf(s);
    }
}
