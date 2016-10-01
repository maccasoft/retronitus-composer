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

public class SongRowBuilder {

    String[] note = new String[8];
    String[] instrument = new String[8];
    String[] fx1 = new String[8];
    String[] fx2 = new String[8];

    public SongRowBuilder() {
        for (int i = 0; i < note.length; i++) {
            note[i] = "";
            instrument[i] = "";
            fx1[i] = "";
            fx2[i] = "";
        }
    }

    public SongRowBuilder play(int channel, String note) {
        this.note[channel] = note;
        return this;
    }

    public SongRowBuilder play(int channel, String note, String instrument) {
        this.note[channel] = note;
        this.instrument[channel] = instrument;
        return this;
    }

    public SongRowBuilder play(int channel, String note, String instrument, String fx1, String fx2) {
        this.note[channel] = note;
        this.instrument[channel] = instrument;
        this.fx1[channel] = fx1;
        this.fx2[channel] = fx2;
        return this;
    }

    public SongRowBuilder fx(int channel, String fx1) {
        this.fx1[channel] = fx1;
        return this;
    }

    public SongRowBuilder fx(int channel, String fx1, String fx2) {
        this.fx1[channel] = fx1;
        this.fx2[channel] = fx2;
        return this;
    }

    public SongRow build() {
        return new SongRow(note, instrument, fx1, fx2);
    }
}
