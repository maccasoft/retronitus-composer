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

public class Pattern {

    String baseNote;
    Instrument instrument;
    List<Note> sequence = new ArrayList<Note>();

    public Pattern() {
    }

    public Pattern(String baseNote, Instrument instrument) {
        this.baseNote = baseNote;
        this.instrument = instrument;
    }

    public Pattern(String baseNote, Instrument instrument, List<Note> sequence) {
        this.baseNote = baseNote;
        this.instrument = instrument;
        this.sequence = sequence;
    }

    public String getBaseNote() {
        return baseNote;
    }

    public void setBaseNote(String baseNote) {
        this.baseNote = baseNote;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public void add(Note note) {
        sequence.add(note);
    }

    public int getNoteCount() {
        return sequence.size();
    }

    public Note getNote(int index) {
        return sequence.get(index);
    }

    public List<Note> getSequence() {
        return sequence;
    }

    public void setSequence(List<Note> sequence) {
        this.sequence = sequence;
    }

    public String toCString(String name) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("uint8_t %s[] = {\n", name));

        int note = Util.noteIndex(baseNote);
        for (Note s : sequence) {
            if (s.isPause()) {
                int rw = s.getWait();
                do {
                    int cw = rw <= 7 ? rw : 7;
                    sb.append(String.format("    (25 << 3) | %d,\n", cw));
                    rw -= cw;
                } while (rw > 0);
            }
            else if (s.isNote()) {
                int delta = s.getNoteIndex() - note;

                int rw = s.getWait();
                int cw = rw <= 7 ? rw : 7;
                sb.append(String.format("    ((12 %s%d) << 3) | %d,", delta >= 0 ? "+" : "", delta, cw));
                if (Math.abs(delta) > 12) {
                    sb.append(" // WARNING delta exceeds!");
                }
                sb.append("\n");
                rw -= cw;
                while (rw > 0) {
                    cw = rw <= 7 ? rw : 7;
                    sb.append(String.format("    (25 << 3) | %d,\n", cw));
                    rw -= cw;
                }
                note += delta;
            }
            else if (s.isTrigger()) {
                sb.append(String.format("    ((%d) << 3) | %d,\n", 26 + s.getTriggerIndex(), 1));
            }
        }
        sb.append("    0\n};\n");

        return sb.toString();
    }

    public byte[] toArray() {
        ByteArrayWriter sb = new ByteArrayWriter();

        int note = baseNote != null ? Util.noteIndex(baseNote) : 0;
        for (Note s : sequence) {
            int rw = s.getWait();

            if (s.isNote()) {
                int delta = s.getNoteIndex() - note;
                note += delta;

                int cw = rw <= 7 ? rw : 7;
                sb.writeByte(((12 + delta) << 3) | cw);
                rw -= cw;
            }
            else if (s.isTrigger()) {
                int fx = 26 + s.getTriggerIndex();

                int cw = rw <= 7 ? rw : 7;
                sb.writeByte((fx << 3) | cw);
                rw -= cw;
            }
            else {
                int fx = 25;

                int i = 0;
                for (Instrument ins : instrument.getEffects()) {
                    if (s.equals(ins.getName())) {
                        fx = 26 + i;
                        break;
                    }
                    i++;
                }

                int cw = rw <= 7 ? rw : 7;
                sb.writeByte((fx << 3) | cw);
                rw -= cw;
            }

            while (rw > 0) {
                int cw = rw <= 7 ? rw : 7;
                sb.writeByte((25 << 3) | cw);
                rw -= cw;
            }
        }
        sb.writeByte(0);

        return sb.toByteArray();
    }
}
