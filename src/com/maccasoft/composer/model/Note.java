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

public class Note {

    String note;
    int wait;

    public Note() {
        this.note = "";
        this.wait = 0;
    }

    public Note(String note) {
        this.note = note;
        this.wait = 0;
    }

    public Note(String note, int wait) {
        this.note = note;
        this.wait = wait;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getWait() {
        return wait;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }

    public boolean isNote() {
        return note.length() == 3 &&
            Character.isAlphabetic(note.charAt(0)) &&
            (note.charAt(1) == '-' || note.charAt(1) == '#') &&
            Character.isDigit(note.charAt(2));
    }

    public int getNoteIndex() {
        return Util.noteIndex(note);
    }

    public boolean isTrigger() {
        return note.length() == 3 &&
            note.charAt(0) == 'T' && note.charAt(1) == 'R' &&
            note.charAt(2) >= '1' && note.charAt(2) <= '6';
    }

    public int getTriggerIndex() {
        return note.charAt(2) - '1';
    }

    public boolean isPause() {
        return "".equals(note);
    }

    @Override
    public String toString() {
        return "Note [note=" + note + ", wait=" + wait + "]";
    }
}
