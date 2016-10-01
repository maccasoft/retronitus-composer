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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.databinding.observable.list.WritableList;

public class Project {

    List<Instrument> instruments = new ArrayList<Instrument>();
    List<Song> songs = new ArrayList<Song>();

    final WritableList observableInstruments = new WritableList(instruments, Instrument.class);
    final WritableList observableSongs = new WritableList(songs, SongRow.class);

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    static final NumberFormat nf = NumberFormat.getInstance(Locale.US);
    static {
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(2);
    }

    public Project() {
    }

    public Project(BufferedReader is) throws Exception {
        readFrom(is);
    }

    public Project(List<Instrument> instruments, List<Song> songs) {
        this.instruments = instruments;
        this.songs = songs;
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
        observableInstruments.add(element);
    }

    public int getInstrumentsSize() {
        return instruments.size();
    }

    public Instrument getInstrument(int index) {
        return instruments.get(index);
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
                s = line.indexOf("name=\"") + 6;
                e = line.indexOf('"', s);
                instrument.setName(line.substring(s, e));
                readFrom(is, instrument);
                instruments.add(instrument);
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

        int id = 0;
        for (Instrument ins : instruments) {
            os.println(String.format("  <instrument id=\"%02X\" name=\"%s\">", id, ins.getName()));
            for (Command cmd : ins.getCommands()) {
                os.print("    " + cmd.toXmlString());
                os.println();
            }
            os.print("  </instrument>\n");
            id++;
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
    }
}
