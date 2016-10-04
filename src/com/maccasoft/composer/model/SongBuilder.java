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

import java.util.ArrayList;
import java.util.List;

public class SongBuilder {

    String name;
    int bpm;
    List<SongRow> rows = new ArrayList<SongRow>();

    SongRow current;

    public SongBuilder() {
    }

    public SongBuilder(String name, int bpm) {
        this.name = name;
        this.bpm = bpm;
    }

    public String getName() {
        return name;
    }

    public SongBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public int getBpm() {
        return bpm;
    }

    public SongBuilder setBpm(int bpm) {
        this.bpm = bpm;
        return this;
    }

    public SongBuilder row() {
        rows.add(current = new SongRow());
        return this;
    }

    public SongBuilder row(int index) {
        while (rows.size() <= index) {
            rows.add(new SongRow());
        }
        current = rows.get(index);
        return this;
    }

    public SongBuilder play(int channel, String note, String instrument) {
        current.note[channel] = note;
        current.instrument[channel] = instrument;
        return this;
    }

    public SongBuilder play(int channel, String note, String instrument, String fx1, String fx2) {
        current.note[channel] = note;
        current.instrument[channel] = instrument;
        current.fx1[channel] = fx1;
        current.fx2[channel] = fx2;
        return this;
    }

    public Song build() {
        return new Song(name, bpm, rows);
    }
}
