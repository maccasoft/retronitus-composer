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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.databinding.observable.list.WritableList;

public class Song {

    public static final String PROP_NAME = "name";
    public static final String PROP_BPM = "bpm";

    String name;
    int bpm;
    List<SongRow> rows = new ArrayList<SongRow>();

    final WritableList observableRows = new WritableList(rows, SongRow.class);

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public Song() {
    }

    public Song(String name, int bpm) {
        this.name = name;
        this.bpm = bpm;
    }

    public Song(String name, int bpm, List<SongRow> rows) {
        this.name = name;
        this.bpm = bpm;
        this.observableRows.addAll(rows);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        changeSupport.firePropertyChange(PROP_NAME, this.name, this.name = name);
    }

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        changeSupport.firePropertyChange(PROP_BPM, this.bpm, this.bpm = bpm);
    }

    public List<SongRow> getRows() {
        return rows;
    }

    public void add(SongRow element) {
        observableRows.add(element);
    }

    public WritableList getObservableRows() {
        return observableRows;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + bpm;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((rows == null) ? 0 : rows.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Song)) {
            return false;
        }
        Song other = (Song) obj;
        if (bpm != other.bpm) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        if (rows == null) {
            if (other.rows != null) {
                return false;
            }
        }
        else if (!listEqualsTo(rows, other.rows)) {
            return false;
        }
        return true;
    }

    boolean listEqualsTo(List<SongRow> list, List<SongRow> o) {
        ListIterator<SongRow> e1 = list.listIterator();
        ListIterator<SongRow> e2 = o.listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            SongRow o1 = e1.next();
            SongRow o2 = e2.next();
            if (!(o1 == null ? o2 == null : o1.equalsTo(o2))) {
                return false;
            }
        }
        return !(e1.hasNext() || e2.hasNext());
    }

    @Override
    public String toString() {
        return name;
    }
}
