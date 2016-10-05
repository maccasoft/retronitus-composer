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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;

public class Project {

    public static final String PROP_DIRTY = "dirty";

    public static final String[] channelLabels = {
        "Square 1",
        "Square 2",
        "Square 3",
        "Saw 1",
        "Saw 2",
        "Saw 3",
        "Triangle",
        "Noise",
    };

    List<Instrument> instruments = new ArrayList<Instrument>();
    List<Song> songs = new ArrayList<Song>();

    final WritableList observableInstruments = new WritableList(instruments, Instrument.class);
    final WritableList observableSongs = new WritableList(songs, SongRow.class);

    boolean dirty;
    private Map<Instrument, String> instrumentsMap = new HashMap<Instrument, String>();

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    static final NumberFormat nf = NumberFormat.getInstance(Locale.US);
    static {
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(2);
    }

    final IListChangeListener listChangeListener = new IListChangeListener() {

        @Override
        public void handleListChange(ListChangeEvent event) {
            event.diff.accept(new ListDiffVisitor() {

                @Override
                public void handleRemove(int index, Object element) {
                    if (element instanceof Song) {
                        Song song = (Song) element;
                        song.removePropertyChangeListener(propertyChangeListener);
                        song.getObservableRows().removeListChangeListener(listChangeListener);
                        for (SongRow row : song.getRows()) {
                            row.removePropertyChangeListener(propertyChangeListener);
                        }
                    }
                    else if (element instanceof SongRow) {
                        ((SongRow) element).removePropertyChangeListener(propertyChangeListener);
                    }
                    else if (element instanceof Instrument) {
                        ((Instrument) element).removePropertyChangeListener(propertyChangeListener);
                        instrumentsMap.remove(element);
                    }
                }

                @Override
                public void handleAdd(int index, Object element) {
                    if (element instanceof Song) {
                        Song song = (Song) element;
                        song.addPropertyChangeListener(propertyChangeListener);
                        song.getObservableRows().addListChangeListener(listChangeListener);
                        for (SongRow row : song.getRows()) {
                            row.addPropertyChangeListener(propertyChangeListener);
                        }
                    }
                    else if (element instanceof SongRow) {
                        ((SongRow) element).addPropertyChangeListener(propertyChangeListener);
                    }
                    else if (element instanceof Instrument) {
                        ((Instrument) element).addPropertyChangeListener(propertyChangeListener);
                        instrumentsMap.put((Instrument) element, String.format("%02X", index));
                    }
                }
            });
            setDirty(true);
        }
    };

    final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            setDirty(true);
        }
    };

    public Project() {
        observableSongs.addListChangeListener(listChangeListener);
        observableInstruments.addListChangeListener(listChangeListener);
    }

    public Project(BufferedReader is) throws Exception {
        readFrom(is);
        for (Instrument element : instruments) {
            element.addPropertyChangeListener(propertyChangeListener);
        }
        for (Song song : songs) {
            song.addPropertyChangeListener(propertyChangeListener);
            for (SongRow element : song.getRows()) {
                element.addPropertyChangeListener(propertyChangeListener);
            }
        }
        observableSongs.addListChangeListener(listChangeListener);
        observableInstruments.addListChangeListener(listChangeListener);
    }

    public Project(List<Instrument> instruments, List<Song> songs) {
        this.instruments.addAll(instruments);
        this.songs.addAll(songs);

        int index = 0;
        for (Instrument element : instruments) {
            element.addPropertyChangeListener(propertyChangeListener);
            instrumentsMap.put(element, String.format("%02X", index++));
        }
        for (Song song : songs) {
            song.addPropertyChangeListener(propertyChangeListener);
            for (SongRow element : song.getRows()) {
                element.addPropertyChangeListener(propertyChangeListener);
            }
        }

        observableSongs.addListChangeListener(listChangeListener);
        observableInstruments.addListChangeListener(listChangeListener);
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

    public void add(Instrument element) {
        int index = 0;
        while (index < instruments.size()) {
            String key = String.format("%02X", index);
            if (!instrumentsMap.containsValue(key)) {
                break;
            }
            index++;
        }
        observableInstruments.add(index, element);
    }

    public void remove(Instrument element) {
        observableInstruments.remove(element);
    }

    public String getInstrumentId(Instrument ins) {
        return instrumentsMap.get(ins);
    }

    public Instrument getInstrument(String id) {
        for (Map.Entry<Instrument, String> entry : instrumentsMap.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(id)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void add(Song element) {
        observableSongs.add(element);
    }

    public int getSongSize() {
        return songs.size();
    }

    public Song getSong(int index) {
        return songs.get(index);
    }

    public List<Instrument> getInstruments() {
        return instruments;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        changeSupport.firePropertyChange(PROP_DIRTY, this.dirty, this.dirty = dirty);
    }

    public WritableList getObservableInstruments() {
        return observableInstruments;
    }

    public WritableList getObservableSongs() {
        return observableSongs;
    }

    void readFrom(BufferedReader is) throws Exception {
        int s, e;
        String line;

        while ((line = is.readLine()) != null) {
            if (line.contains("<instrument")) {
                Instrument instrument = new Instrument();
                s = line.indexOf("id=\"") + 4;
                e = line.indexOf('"', s);
                String id = line.substring(s, e);
                s = line.indexOf("name=\"") + 6;
                e = line.indexOf('"', s);
                instrument.setName(line.substring(s, e));
                readFrom(is, instrument);
                instruments.add(instrument);
                instrumentsMap.put(instrument, id);
            }
            else if (line.contains("<song")) {
                Song song = new Song();

                s = line.indexOf("name=\"") + 6;
                e = line.indexOf('"', s);
                song.setName(line.substring(s, e));

                s = line.indexOf("bpm=\"") + 5;
                e = line.indexOf('"', s);
                song.setBpm(Integer.valueOf(line.substring(s, e)));

                readFrom(is, song);

                songs.add(song);
            }
        }
    }

    void readFrom(BufferedReader is, Instrument instrument) throws Exception {
        String line;

        while ((line = is.readLine()) != null) {
            if (line.contains("</instrument")) {
                break;
            }

            Command cmd = Command.fromXml(line);
            instrument.add(cmd);
        }
    }

    void readFrom(BufferedReader is, Song song) throws Exception {
        int s, e;
        String line;

        while ((line = is.readLine()) != null) {
            if (line.contains("</song")) {
                break;
            }
            if (line.contains("<row")) {
                SongRow row = new SongRow();
                while ((line = is.readLine()) != null) {
                    if (line.contains("</row")) {
                        break;
                    }
                    if (line.contains("<channel ")) {
                        s = line.indexOf("id=\"") + 4;
                        e = line.indexOf('"', s);
                        int id = Integer.valueOf(line.substring(s, e));
                        s = line.indexOf("note=\"") + 6;
                        e = line.indexOf('"', s);
                        row.setNote(id, line.substring(s, e));
                        s = line.indexOf("instrument=\"") + 12;
                        e = line.indexOf('"', s);
                        row.setInstrument(id, line.substring(s, e));
                        if ((s = line.indexOf("fx1=\"")) != -1) {
                            s += 5;
                            e = line.indexOf('"', s);
                            row.setFx1(id, line.substring(s, e));
                        }
                        if ((s = line.indexOf("fx2=\"")) != -1) {
                            s += 5;
                            e = line.indexOf('"', s);
                            row.setFx2(id, line.substring(s, e));
                        }
                    }
                }
                song.add(row);
            }
        }
    }

    public void writeTo(PrintStream os) throws IOException {
        os.println("<music>");

        for (Instrument ins : instruments) {
            os.println(String.format("  <instrument id=\"%s\" name=\"%s\">", instrumentsMap.get(ins), ins.getName()));
            for (Command cmd : ins.getCommands()) {
                os.print("    " + cmd.toXmlString());
                os.println();
            }
            os.print("  </instrument>\n");
        }

        for (Song song : songs) {
            os.print(String.format("  <song name=\"%s\" bpm=\"%d\">\n", song.getName(), song.getBpm()));
            int rowId = 0;
            for (SongRow row : song.getRows()) {
                os.print(String.format("    <row id=\"%02X\">\n", rowId++));
                for (int i = 0; i < 8; i++) {
                    if (row.isChannelEmpty(i)) {
                        continue;
                    }
                    os.print("      ");
                    os.print(String.format("<channel id=\"%d\" note=\"%s\" instrument=\"%s\"", i,
                        row.getNote(i), row.getInstrument(i)));
                    if (!"".equals(row.getFx1(i))) {
                        os.printf(String.format(" fx1=\"%s\"", row.getFx1(i)));
                    }
                    if (!"".equals(row.getFx2(i))) {
                        os.printf(String.format(" fx2=\"%s\"", row.getFx2(i)));
                    }
                    os.println(" />");
                }
                os.println("    </row>");
            }
            os.println("  </song>");
        }
        os.println("</music>");

        setDirty(false);
    }
}
