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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import jssc.SerialPort;
import jssc.SerialPortException;

public class PianoKeyboard {

    Canvas canvas;
    int firstOctave;
    int octaves;

    int[][] waveTable;
    boolean needUpload;

    private int keyWidth;
    private int diesisKeyWidth;
    private Map<Rectangle, Integer> noteMap = new HashMap<Rectangle, Integer>();
    private Map<Rectangle, Integer> noteDiesisMap = new HashMap<Rectangle, Integer>();

    private SerialPort serialPort;

    final PaintListener paintListener = new PaintListener() {

        @Override
        public void paintControl(PaintEvent e) {
            GC gc = e.gc;
            Rectangle bounds = ((Canvas) e.widget).getBounds();
            int diesisHeight = (int) (bounds.height * 0.75);

            int x = 0;
            int n = 0;
            int i = 0;
            Map<Rectangle, Integer> newNoteMap = new HashMap<Rectangle, Integer>();
            Map<Rectangle, Integer> newNoteDiesisMap = new HashMap<Rectangle, Integer>();
            while (x < bounds.width) {
                Rectangle rect = new Rectangle(x, 0, keyWidth, bounds.height - 1);
                newNoteMap.put(rect, i++);
                if ((rect.x + rect.width + keyWidth) >= bounds.width) {
                    break;
                }
                if (n == 0 || n == 1 || n == 3 || n == 4 || n == 5) {
                    rect = new Rectangle(x + keyWidth - diesisKeyWidth / 2, 0, diesisKeyWidth, diesisHeight);
                    newNoteDiesisMap.put(rect, i++);
                }
                x += keyWidth;
                if (++n >= 7) {
                    n = 0;
                }
            }

            gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

            for (Entry<Rectangle, Integer> entry : newNoteMap.entrySet()) {
                gc.setBackground(Display.getDefault().getSystemColor(
                    currentNote == entry.getValue() ? SWT.COLOR_CYAN : SWT.COLOR_WHITE));
                gc.fillRectangle(entry.getKey());
                gc.drawRectangle(entry.getKey());
            }
            for (Entry<Rectangle, Integer> entry : newNoteDiesisMap.entrySet()) {
                gc.setBackground(Display.getDefault().getSystemColor(
                    currentNote == entry.getValue() ? SWT.COLOR_CYAN : SWT.COLOR_BLACK));
                gc.fillRectangle(entry.getKey());
            }

            noteMap = newNoteMap;
            noteDiesisMap = newNoteDiesisMap;
        }
    };

    boolean buttonDown;
    int currentNote = -1;

    final MouseListener mouseListener = new MouseListener() {

        @Override
        public void mouseUp(MouseEvent e) {
            buttonDown = false;
            currentNote = -1;
            canvas.redraw();

            try {
                if (serialPort.isOpened()) {
                    serialPort.writeString("0");
                }
            } catch (SerialPortException e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void mouseDown(MouseEvent e) {
            buttonDown = true;
            currentNote = getNoteIndex(e.x, e.y);
            canvas.redraw();

            if (currentNote == -1) {
                return;
            }

            playNote(currentNote);
        }

        @Override
        public void mouseDoubleClick(MouseEvent e) {
        }
    };

    final MouseMoveListener mouseMoveListener = new MouseMoveListener() {

        @Override
        public void mouseMove(MouseEvent e) {
            if (!buttonDown) {
                return;
            }
            int index = getNoteIndex(e.x, e.y);
            if (index != currentNote) {
                currentNote = index;
                canvas.redraw();

                if (currentNote == -1) {
                    return;
                }

                playNote(currentNote);
            }
        }
    };

    final MouseTrackListener mouseTrackListener = new MouseTrackListener() {

        @Override
        public void mouseEnter(MouseEvent e) {
        }

        @Override
        public void mouseExit(MouseEvent e) {
        }

        @Override
        public void mouseHover(MouseEvent e) {
        }
    };

    final KeyListener keyListener = new KeyListener() {

        char lastKeyPress;

        @Override
        public void keyPressed(KeyEvent e) {
            if (lastKeyPress == e.character) {
                return;
            }
            lastKeyPress = e.character;
            try {
                if (serialPort.isOpened()) {
                    uploadWaveTable();
                    serialPort.writeInt(e.character);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            lastKeyPress = 0;
            try {
                if (serialPort.isOpened()) {
                    serialPort.writeString("0");
                }
            } catch (SerialPortException e1) {
                e1.printStackTrace();
            }
        }
    };

    public PianoKeyboard(Composite parent) {
        GC gc = new GC(parent);
        FontMetrics fontMetrics = gc.getFontMetrics();
        keyWidth = Dialog.convertHorizontalDLUsToPixels(fontMetrics, 10);
        diesisKeyWidth = Dialog.convertHorizontalDLUsToPixels(fontMetrics, 6);
        gc.dispose();

        firstOctave = 0;
        octaves = 5;

        canvas = new Canvas(parent, SWT.NONE);
        canvas.setSize(octaves * keyWidth * 7, SWT.DEFAULT);
        canvas.addPaintListener(paintListener);
        canvas.addMouseListener(mouseListener);
        canvas.addMouseMoveListener(mouseMoveListener);
        canvas.addMouseTrackListener(mouseTrackListener);
        canvas.addKeyListener(keyListener);
    }

    public void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    int getNoteIndex(int x, int y) {
        for (Entry<Rectangle, Integer> entry : noteDiesisMap.entrySet()) {
            if (entry.getKey().contains(x, y)) {
                return entry.getValue();
            }
        }
        for (Entry<Rectangle, Integer> entry : noteMap.entrySet()) {
            if (entry.getKey().contains(x, y)) {
                return entry.getValue();
            }
        }
        return -1;
    }

    public void setLayoutData(Object data) {
        canvas.setLayoutData(data);
    }

    public int getKeyWidth() {
        return keyWidth;
    }

    void playNote(int note) {
        try {
            if (!serialPort.isOpened()) {
                return;
            }

            uploadWaveTable();

            String s = Util.noteFromIndex(note + 12 * firstOctave + 1);
            serialPort.writeString(s);
        } catch (SerialPortException e1) {
            e1.printStackTrace();
        }
    }

    void uploadWaveTable() throws SerialPortException {
        if (!needUpload) {
            return;
        }

        serialPort.writeString("U");

        long size = waveTable.length * 8;
        serialPort.writeByte((byte) (size));
        serialPort.writeByte((byte) (size >> 8));

        for (int i = 0; i < waveTable.length; i++) {
            serialPort.writeByte((byte) (waveTable[i][0]));
            serialPort.writeByte((byte) (waveTable[i][0] >> 8));
            serialPort.writeByte((byte) (waveTable[i][0] >> 16));
            serialPort.writeByte((byte) (waveTable[i][0] >> 24));

            serialPort.writeByte((byte) (waveTable[i][1]));
            serialPort.writeByte((byte) (waveTable[i][1] >> 8));
            serialPort.writeByte((byte) (waveTable[i][1] >> 16));
            serialPort.writeByte((byte) (waveTable[i][1] >> 24));
        }

        needUpload = false;
    }

    public void setWaveTable(int[][] waveTable) {
        this.waveTable = waveTable;
        this.needUpload = true;
    }
}
