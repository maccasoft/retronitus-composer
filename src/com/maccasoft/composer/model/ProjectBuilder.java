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

public class ProjectBuilder {

    List<InstrumentBuilder> instruments = new ArrayList<InstrumentBuilder>();
    List<SongBuilder> songs = new ArrayList<SongBuilder>();

    public ProjectBuilder() {
    }

    public ProjectBuilder add(InstrumentBuilder builder) {
        instruments.add(builder);
        return this;
    }

    public ProjectBuilder add(SongBuilder builder) {
        songs.add(builder);
        return this;
    }

    public Project build() {
        List<Instrument> ins = new ArrayList<Instrument>();
        for (InstrumentBuilder builder : instruments) {
            ins.add(builder.build());
        }
        List<Song> sng = new ArrayList<Song>();
        for (SongBuilder builder : songs) {
            sng.add(builder.build());
        }
        return new Project(ins, sng);
    }
}
