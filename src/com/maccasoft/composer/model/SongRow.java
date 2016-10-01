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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;

public class SongRow {

    public static final String PROP_NOTE = "note";
    public static final String PROP_INSTRUMENT = "instrument";
    public static final String PROP_FX1 = "fx1";
    public static final String PROP_FX2 = "fx2";

    String[] note = new String[8];
    String[] instrument = new String[8];
    String[] fx1 = new String[8];
    String[] fx2 = new String[8];

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public SongRow() {
        for (int i = 0; i < note.length; i++) {
            note[i] = "";
            instrument[i] = "";
            fx1[i] = "";
            fx2[i] = "";
        }
    }

    public SongRow(String[] note, String[] instrument, String[] fx1, String[] fx2) {
        this.note = note;
        this.instrument = instrument;
        this.fx1 = fx1;
        this.fx2 = fx2;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(propertyName, l);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(propertyName, l);
    }

    public String getNote(int index) {
        return note[index];
    }

    public void setNote(int index, String note) {
        changeSupport.fireIndexedPropertyChange(PROP_NOTE, index, this.note[index], this.note[index] = note.toUpperCase());
    }

    public String getInstrument(int index) {
        return instrument[index];
    }

    public void setInstrument(int index, String instrument) {
        changeSupport.fireIndexedPropertyChange(PROP_INSTRUMENT, index, this.instrument[index],
            this.instrument[index] = instrument.toUpperCase());
    }

    public String getFx1(int index) {
        return fx1[index];
    }

    public void setFx1(int index, String fx1) {
        changeSupport.fireIndexedPropertyChange(PROP_FX1, index, this.fx1[index], this.fx1[index] = fx1.toUpperCase());
    }

    public String getFx2(int index) {
        return fx2[index];
    }

    public void setFx2(int index, String fx2) {
        changeSupport.fireIndexedPropertyChange(PROP_FX2, index, this.fx2[index], this.fx2[index] = fx2.toUpperCase());
    }

    public boolean isChannelEmpty(int index) {
        return "".equals(note[index]) && "".equals(instrument[index]) && "".equals(fx1[index]) && "".equals(fx2[index]);
    }

    public boolean equalsTo(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SongRow)) {
            return false;
        }
        SongRow other = (SongRow) obj;
        if (!Arrays.equals(fx1, other.fx1)) {
            return false;
        }
        if (!Arrays.equals(fx2, other.fx2)) {
            return false;
        }
        if (!Arrays.equals(instrument, other.instrument)) {
            return false;
        }
        if (!Arrays.equals(note, other.note)) {
            return false;
        }
        return true;
    }
}
