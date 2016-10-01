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

public class PatternBuilder {

    String baseNote;
    InstrumentBuilder instrument;
    List<Note> sequence = new ArrayList<Note>();

    public PatternBuilder(InstrumentBuilder instrument) {
        this.instrument = instrument;
    }

    public PatternBuilder(String baseNote, InstrumentBuilder instrument) {
        this.baseNote = baseNote;
        this.instrument = instrument;
    }

    public String getBaseNote() {
        return baseNote;
    }

    public PatternBuilder setBaseNote(String baseNote) {
        this.baseNote = baseNote;
        return this;
    }

    public InstrumentBuilder getInstrument() {
        return instrument;
    }

    public PatternBuilder setInstrument(InstrumentBuilder instrument) {
        this.instrument = instrument;
        return this;
    }

    public PatternBuilder play(String note) {
        sequence.add(new Note(note));
        return this;
    }

    public PatternBuilder play(String note, int wait) {
        sequence.add(new Note(note, wait));
        return this;
    }

    public List<Note> getSequence() {
        return sequence;
    }

    public Pattern build() {
        return new Pattern(baseNote, instrument.build(), sequence);
    }
}
