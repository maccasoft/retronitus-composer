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

package com.maccasoft.composer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import com.maccasoft.composer.model.SongRow;

public class NoteKeyListener implements KeyListener {

    final GridTableViewer viewer;

    static final Map<Character, String> noteMap = new HashMap<Character, String>();
    static {
        noteMap.put('z', "C-");
        noteMap.put('s', "C#");
        noteMap.put('x', "D-");
        noteMap.put('d', "D#");
        noteMap.put('c', "E-");
        noteMap.put('v', "F-");
        noteMap.put('g', "F#");
        noteMap.put('b', "G-");
        noteMap.put('h', "G#");
        noteMap.put('n', "A-");
        noteMap.put('j', "A#");
        noteMap.put('m', "B-");
    }

    public NoteKeyListener(GridTableViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        IStructuredSelection selection = viewer.getStructuredSelection();
        if (selection.isEmpty()) {
            return;
        }

        SongRow model = (SongRow) selection.getFirstElement();

        final ViewerCell cell = viewer.getColumnViewerEditor().getFocusCell();
        int channel = cell.getColumnIndex() / 4;
        int columnIndex = cell.getColumnIndex() % 4;

        if (columnIndex == 0) {
            String s = noteMap.get(e.character);
            if (s != null) {
                model.setNote(channel, s + String.valueOf(getOctave()));
                if ("".equals(model.getInstrument(channel))) {
                    model.setInstrument(channel, getInstrument());
                }
                viewer.update(model, null);

                final ViewerCell nextCell = cell.getNeighbor(ViewerCell.BELOW, false);
                if (nextCell != null) {
                    final Event event1 = new Event();
                    event1.type = SWT.KeyDown;
                    event1.keyCode = SWT.ARROW_DOWN;
                    event1.widget = e.widget;
                    final Event event2 = new Event();
                    event2.type = SWT.KeyUp;
                    event2.keyCode = SWT.ARROW_DOWN;
                    event2.widget = e.widget;

                    final Display display = e.display;
                    display.asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            if (event1.widget.isDisposed()) {
                                return;
                            }
                            display.post(event1);
                            display.post(event2);
                        }
                    });
                }
                e.doit = false;
                return;
            }
        }
        if (e.character == SWT.DEL) {
            if ((e.stateMask & SWT.MOD2) != 0) {
                removeEntryAndShiftUp(channel, model);
                viewer.refresh();
                e.doit = false;
                return;
            }
            switch (columnIndex) {
                case 0:
                    model.setNote(channel, "");
                    break;
                case 1:
                    model.setInstrument(channel, "");
                    break;
                case 2:
                    model.setFx1(channel, "");
                    break;
                case 3:
                    model.setFx2(channel, "");
                    break;
            }
            viewer.update(model, null);
            e.doit = false;
            return;
        }
        if (e.keyCode == SWT.INSERT) {
            insertBlankAndShiftDown(channel, model);
            viewer.refresh();
            e.doit = false;
            return;
        }
        if (e.character >= 0x20 && e.character <= 0x7F) {
            e.doit = false;
            return;
        }
    }

    protected int getOctave() {
        return 4;
    }

    protected String getInstrument() {
        return "00";
    }

    class ChannelRowEntry {
        String note;
        String instrument;
        String fx1;
        String fx2;

        ChannelRowEntry() {
            note = "";
            instrument = "";
            fx1 = "";
            fx2 = "";
        }

        ChannelRowEntry(int channel, SongRow row) {
            this.note = row.getNote(channel);
            this.instrument = row.getInstrument(channel);
            this.fx1 = row.getFx1(channel);
            this.fx2 = row.getFx2(channel);
        }
    }

    void insertBlankAndShiftDown(int channel, Object selection) {
        List<ChannelRowEntry> list = new ArrayList<ChannelRowEntry>();

        WritableList input = (WritableList) viewer.getInput();
        for (Object o : input) {
            list.add(new ChannelRowEntry(channel, (SongRow) o));
        }

        int index = input.indexOf(selection);
        list.add(index, new ChannelRowEntry());

        Iterator<ChannelRowEntry> iterFrom = list.iterator();
        Iterator<?> iterTo = input.iterator();
        while (iterFrom.hasNext() && iterTo.hasNext()) {
            ChannelRowEntry entry = iterFrom.next();
            SongRow row = (SongRow) iterTo.next();
            row.setNote(channel, entry.note);
            row.setInstrument(channel, entry.instrument);
            row.setFx1(channel, entry.fx1);
            row.setFx2(channel, entry.fx2);
        }
    }

    void removeEntryAndShiftUp(int channel, Object selection) {
        List<ChannelRowEntry> list = new ArrayList<ChannelRowEntry>();

        WritableList input = (WritableList) viewer.getInput();
        for (Object o : input) {
            if (o != selection) {
                list.add(new ChannelRowEntry(channel, (SongRow) o));
            }
        }

        Iterator<ChannelRowEntry> iterFrom = list.iterator();
        Iterator<?> iterTo = input.iterator();
        while (iterFrom.hasNext() && iterTo.hasNext()) {
            ChannelRowEntry entry = iterFrom.next();
            SongRow row = (SongRow) iterTo.next();
            row.setNote(channel, entry.note);
            row.setInstrument(channel, entry.instrument);
            row.setFx1(channel, entry.fx1);
            row.setFx2(channel, entry.fx2);
        }
        while (iterTo.hasNext()) {
            SongRow row = (SongRow) iterTo.next();
            row.setNote(channel, "");
            row.setInstrument(channel, "");
            row.setFx1(channel, "");
            row.setFx2(channel, "");
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
