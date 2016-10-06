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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridViewerColumn;
import org.eclipse.nebula.jface.gridviewer.GridViewerEditor;
import org.eclipse.nebula.jface.gridviewer.internal.CellSelection;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.maccasoft.composer.internal.ImageRegistry;
import com.maccasoft.composer.internal.TextEditingSupport;
import com.maccasoft.composer.model.Instrument;
import com.maccasoft.composer.model.InvalidInstrumentException;
import com.maccasoft.composer.model.Music;
import com.maccasoft.composer.model.Project;
import com.maccasoft.composer.model.ProjectCompiler;
import com.maccasoft.composer.model.ProjectException;
import com.maccasoft.composer.model.Song;
import com.maccasoft.composer.model.SongRow;

import jssc.SerialPort;
import jssc.SerialPortException;

public class MusicEditor {

    Shell shell;
    ComboViewer songsCombo;
    ToolItem edit;
    ToolItem play;
    ToolItem stop;
    ToolItem delete;
    Spinner bpm;
    Spinner rows;
    Spinner octave;
    InstrumentToolBar instrumentToolBar;

    GridTableViewer viewer;

    Project project;
    Song currentSong;

    private SerialPort serialPort;
    private FontMetrics fontMetrics;

    final IListChangeListener listChangeListener = new IListChangeListener() {

        @Override
        public void handleListChange(ListChangeEvent event) {
            event.diff.accept(new ListDiffVisitor() {

                @Override
                public void handleRemove(int index, Object element) {
                    ((SongRow) element).removePropertyChangeListener(propertyChangeListener);
                }

                @Override
                public void handleAdd(int index, Object element) {
                    ((SongRow) element).addPropertyChangeListener(propertyChangeListener);
                }
            });
            Display.getDefault().timerExec(250, rowBackgroundUpdateRunnable);
        }
    };

    final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {

        }
    };

    final IListChangeListener instrumentsListChangeListener = new IListChangeListener() {

        @Override
        public void handleListChange(ListChangeEvent event) {
            viewer.refresh();
        }
    };

    final Runnable rowBackgroundUpdateRunnable = new Runnable() {

        @Override
        public void run() {
            Grid grid = (Grid) viewer.getControl();
            if (grid.isDisposed()) {
                return;
            }

            Color bg = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

            GridItem[] gridItems = grid.getItems();
            for (int i = 0; i < gridItems.length; i++) {
                gridItems[i].setBackground((i & 1) == 0 ? null : bg);
            }
        }
    };

    public MusicEditor(Composite parent) {
        shell = parent.getShell();

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GC gc = new GC(container);
        fontMetrics = gc.getFontMetrics();
        gc.dispose();

        createHeader(container);
        createMusicViewer(container);

        container.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                try {
                    if (serialPort.isOpened()) {
                        serialPort.closePort();
                    }
                } catch (SerialPortException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    void updateViewFromProject() {
        songsCombo.setInput(project.getObservableSongs());
        if (project.getSongSize() != 0) {
            songsCombo.setSelection(new StructuredSelection(project.getSong(0)));
            rows.setEnabled(true);
        }
        else {
            rows.setEnabled(false);
        }
        viewer.getControl().setFocus();
    }

    void updateSongView() {
        edit.setEnabled(currentSong != null);
        play.setEnabled(currentSong != null);
        stop.setEnabled(currentSong != null);
        delete.setEnabled(currentSong != null && project.getSongs().size() > 1);
        bpm.setSelection(currentSong.getBpm());
        rows.setSelection(currentSong.getObservableRows().size());
        viewer.setInput(currentSong.getObservableRows());
    }

    void createHeader(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(12, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label label = new Label(composite, SWT.NONE);
        label.setText("Song");

        songsCombo = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.NO_FOCUS);
        songsCombo.getCombo().setVisibleItemCount(20);
        songsCombo.getCombo().setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 30), SWT.DEFAULT));
        songsCombo.setContentProvider(new ObservableListContentProvider());
        songsCombo.setLabelProvider(new LabelProvider());

        ToolBar toolBar = new ToolBar(composite, SWT.FLAT | SWT.NO_FOCUS);

        edit = new ToolItem(toolBar, SWT.PUSH);
        edit.setImage(ImageRegistry.getImageFromResources("application_edit.png"));
        edit.setToolTipText("Rename");
        edit.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IInputValidator validator = new IInputValidator() {

                    @Override
                    public String isValid(String newText) {
                        if (newText.length() == 0) {
                            return "";
                        }
                        for (Song song : project.getSongs()) {
                            if (song != currentSong && newText.equalsIgnoreCase(song.getName())) {
                                return "A song with the same title already exists";
                            }
                        }
                        return null;
                    }
                };
                InputDialog dlg = new InputDialog(shell, "Rename Song", "Title:", currentSong.getName(), validator);
                if (dlg.open() == InputDialog.OK) {
                    currentSong.setName(dlg.getValue());
                    songsCombo.refresh();
                }
            }
        });

        new ToolItem(toolBar, SWT.SEPARATOR);

        play = new ToolItem(toolBar, SWT.PUSH);
        play.setImage(ImageRegistry.getImageFromResources("control_play_blue.png"));
        play.setToolTipText("Play");
        play.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Runnable playThread = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            ProjectCompiler compiler = new ProjectCompiler(project);
                            Music music = compiler.build(currentSong);
                            byte[] data = music.toArray();
                            try {
                                serialPort.writeInt('P');
                                serialPort.writeInt(data.length & 0xFF);
                                serialPort.writeInt((data.length >> 8) & 0xFF);
                                serialPort.writeBytes(data);
                            } catch (SerialPortException e) {
                                e.printStackTrace();
                            }
                        } catch (final ProjectException ex) {
                            Display.getDefault().asyncExec(new Runnable() {

                                @Override
                                public void run() {
                                    if (shell.isDisposed()) {
                                        return;
                                    }
                                    MessageDialog.openError(shell, Main.APP_TITLE, "An error occurred while compiling song:\r\n\r\n"
                                        + ex.getMessage());
                                    focusOnErrorCell(ex);
                                }
                            });
                        }
                    }
                };
                new Thread(playThread).start();
            }
        });

        stop = new ToolItem(toolBar, SWT.PUSH);
        stop.setImage(ImageRegistry.getImageFromResources("control_stop_blue.png"));
        stop.setToolTipText("Stop");
        stop.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    serialPort.writeInt('0');
                } catch (SerialPortException ex) {
                    ex.printStackTrace();
                }
            }
        });

        new ToolItem(toolBar, SWT.SEPARATOR);

        delete = new ToolItem(toolBar, SWT.PUSH);
        delete.setImage(ImageRegistry.getImageFromResources("application_delete.png"));
        delete.setToolTipText("Delete song");
        delete.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = songsCombo.getStructuredSelection();
                if (selection.isEmpty()) {
                    return;
                }
                if (!MessageDialog.openConfirm(shell, Main.APP_TITLE, "You really want to delete this song?")) {
                    return;
                }
                int index = project.getSongs().indexOf(selection.getFirstElement());
                if (index > 0) {
                    songsCombo.setSelection(new StructuredSelection(project.getSong(index - 1)));
                }
                else {
                    songsCombo.setSelection(new StructuredSelection(project.getSong(index + 1)));
                }
                project.getObservableSongs().remove(selection.getFirstElement());
                delete.setEnabled(currentSong != null && project.getSongs().size() > 1);
            }
        });

        label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        label = new Label(composite, SWT.NONE);
        label.setText("BPM");

        bpm = new Spinner(composite, SWT.BORDER | SWT.NO_FOCUS);
        bpm.setValues(120, 0, 9999, 0, 1, 1);
        bpm.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 5), SWT.DEFAULT));
        bpm.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (currentSong != null) {
                    currentSong.setBpm(bpm.getSelection());
                }
            }
        });

        label = new Label(composite, SWT.NONE);
        label.setText("Rows");

        rows = new Spinner(composite, SWT.BORDER | SWT.NO_FOCUS);
        rows.setValues(0, 0, 9999, 0, 1, 1);
        rows.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 5), SWT.DEFAULT));
        rows.setEnabled(false);
        rows.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (currentSong == null) {
                    return;
                }
                int totalRows = rows.getSelection();
                while (currentSong.getObservableRows().size() > totalRows) {
                    currentSong.getObservableRows().remove(currentSong.getObservableRows().size() - 1);
                }
                while (currentSong.getObservableRows().size() < totalRows) {
                    currentSong.getObservableRows().add(new SongRow());
                }
            }
        });

        label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        label = new Label(composite, SWT.NONE);
        label.setText("Octave");

        octave = new Spinner(composite, SWT.BORDER | SWT.NO_FOCUS);
        octave.setValues(3, 1, 9, 0, 1, 1);
        octave.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 3), SWT.DEFAULT));
        octave.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.getControl().setFocus();
            }
        });

        instrumentToolBar = new InstrumentToolBar(composite);

        songsCombo.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                currentSong = (Song) selection.getFirstElement();
                updateSongView();
            }
        });
    }

    void createMusicViewer(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Pattern");
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        group.setLayout(new GridLayout(1, false));

        final Font font;
        if ("win32".equals(SWT.getPlatform())) {
            font = new Font(Display.getDefault(), "Courier New", 10, SWT.NONE);
        }
        else {
            font = new Font(Display.getDefault(), "mono", 10, SWT.NONE);
        }

        GC gc = new GC(group);
        gc.setFont(font);
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        Grid table = new Grid(group, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(false);
        table.setRowHeaderVisible(true);
        table.setItemHeaderWidth(Dialog.convertWidthInCharsToPixels(fontMetrics, 6));
        table.setCellSelectionEnabled(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        table.setFont(font);

        table.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                font.dispose();
            }
        });

        viewer = new GridTableViewer(table);

        ObservableListContentProvider contentProvider = new ObservableListContentProvider() {

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
                if (oldInput != null) {
                    ((IObservableList) oldInput).removeListChangeListener(listChangeListener);
                }
                if (newInput != null) {
                    ((IObservableList) newInput).addListChangeListener(listChangeListener);
                }
                Display.getDefault().timerExec(0, rowBackgroundUpdateRunnable);
                super.inputChanged(viewer, oldInput, newInput);
            }
        };
        viewer.setContentProvider(contentProvider);

        viewer.setRowHeaderLabelProvider(new CellLabelProvider() {

            @Override
            public void update(ViewerCell cell) {
                int index = ((IObservableList) viewer.getInput()).indexOf(cell.getElement());
                cell.setText(String.format("%02X", index));
            }
        });

        for (int ch = 0; ch < Project.channelLabels.length; ch++) {
            final int channel = ch;

            GridColumnGroup columnGroup = new GridColumnGroup(table, SWT.NONE);
            columnGroup.setText(Project.channelLabels[ch]);

            GridColumn column = new GridColumn(columnGroup, SWT.CENTER);
            column.setText("Note");
            column.setWidth(Dialog.convertWidthInCharsToPixels(fontMetrics, 6));
            GridViewerColumn viewerColumn = new GridViewerColumn(viewer, column);
            viewerColumn.setLabelProvider(new CellLabelProvider() {

                @Override
                public void update(ViewerCell cell) {
                    SongRow element = (SongRow) cell.getElement();
                    cell.setText(element.getNote(channel));
                }
            });
            viewerColumn.setEditingSupport(new TextEditingSupport(viewer) {

                @Override
                protected Object getValue(Object element) {
                    return ((SongRow) element).getNote(channel);
                }

                @Override
                protected void setValue(Object element, Object value) {
                    ((SongRow) element).setNote(channel, value.toString());
                    viewer.update(element, null);
                }
            });

            column = new GridColumn(columnGroup, SWT.CENTER);
            column.setText("Ins.");
            column.setWidth(Dialog.convertWidthInCharsToPixels(fontMetrics, 6));
            viewerColumn = new GridViewerColumn(viewer, column);
            viewerColumn.setLabelProvider(new CellLabelProvider() {

                @Override
                public void update(ViewerCell cell) {
                    SongRow element = (SongRow) cell.getElement();
                    String value = element.getInstrument(channel);
                    cell.setText(value);
                    if (project.getInstrument(value) == null) {
                        cell.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
                    }
                    else {
                        cell.setForeground(null);
                    }
                }
            });
            viewerColumn.setEditingSupport(new TextEditingSupport(viewer) {

                @Override
                protected Object getValue(Object element) {
                    return ((SongRow) element).getInstrument(channel);
                }

                @Override
                protected void setValue(Object element, Object value) {
                    ((SongRow) element).setInstrument(channel, value.toString());
                    viewer.update(element, null);
                }
            });

            column = new GridColumn(columnGroup, SWT.CENTER);
            column.setText("Fx1");
            column.setWidth(Dialog.convertWidthInCharsToPixels(fontMetrics, 6));
            viewerColumn = new GridViewerColumn(viewer, column);
            viewerColumn.setLabelProvider(new CellLabelProvider() {

                @Override
                public void update(ViewerCell cell) {
                    SongRow element = (SongRow) cell.getElement();
                    String value = element.getFx1(channel);
                    cell.setText(value);
                    if (value.startsWith("TR") && project.getInstrument(element.getInstrument(channel)) == null) {
                        cell.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
                    }
                    else {
                        cell.setForeground(null);
                    }
                }
            });
            viewerColumn.setEditingSupport(new TextEditingSupport(viewer) {

                @Override
                protected Object getValue(Object element) {
                    return ((SongRow) element).getFx1(channel);
                }

                @Override
                protected void setValue(Object element, Object value) {
                    ((SongRow) element).setFx1(channel, value.toString());
                    viewer.update(element, null);
                }
            });

            column = new GridColumn(columnGroup, SWT.CENTER);
            column.setText("Fx2");
            column.setWidth(Dialog.convertWidthInCharsToPixels(fontMetrics, 6));
            viewerColumn = new GridViewerColumn(viewer, column);
            viewerColumn.setLabelProvider(new CellLabelProvider() {

                @Override
                public void update(ViewerCell cell) {
                    SongRow element = (SongRow) cell.getElement();
                    String value = element.getFx2(channel);
                    cell.setText(value);
                    if (value.startsWith("TR") && project.getInstrument(element.getInstrument(channel)) == null) {
                        cell.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
                    }
                    else {
                        cell.setForeground(null);
                    }
                }
            });
            viewerColumn.setEditingSupport(new TextEditingSupport(viewer) {

                @Override
                protected Object getValue(Object element) {
                    return ((SongRow) element).getFx2(channel);
                }

                @Override
                protected void setValue(Object element, Object value) {
                    ((SongRow) element).setFx2(channel, value.toString());
                    viewer.update(element, null);
                }
            });
        }

        table.addKeyListener(new NoteKeyListener(viewer) {

            @Override
            protected int getOctave() {
                return octave.getSelection();
            }

            @Override
            protected String getInstrument() {
                IStructuredSelection selection = instrumentToolBar.getStructuredSelection();
                String id = project.getInstrumentId((Instrument) selection.getFirstElement());
                return id != null ? id : super.getInstrument();
            }
        });

        ColumnViewerEditorActivationStrategy activationStrategy = new ColumnViewerEditorActivationStrategy(viewer) {

            @Override
            protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
                return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
                    || event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
                    || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.character == SWT.CR)
                    || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
            }
        };
        activationStrategy.setEnableEditorActivationWithKeyboard(true);

        GridViewerEditor.create(viewer,
            activationStrategy,
            ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.KEYBOARD_ACTIVATION
                | GridViewerEditor.SELECTION_FOLLOWS_EDITOR);
    }

    public void setProject(Project project) {
        if (this.project != null) {
            this.project.getObservableInstruments().removeListChangeListener(instrumentsListChangeListener);
        }
        this.project = project;
        this.project.getObservableInstruments().addListChangeListener(instrumentsListChangeListener);
        instrumentToolBar.setProject(project);
        updateViewFromProject();
    }

    public Project getProject() {
        return project;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(String portName) {
        try {
            if (serialPort != null && serialPort.isOpened()) {
                serialPort.closePort();
            }
            serialPort = new SerialPort(portName);
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE,
                false, false);
            instrumentToolBar.setSerialPort(serialPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addSongSelectionChangedListener(ISelectionChangedListener l) {
        songsCombo.addSelectionChangedListener(l);
    }

    public void removeSongSelectionChangedListener(ISelectionChangedListener l) {
        songsCombo.removeSelectionChangedListener(l);
    }

    public void focusOnErrorCell(ProjectException e) {
        int columnIndex = e.getChannelIndex() * 4;
        if (e instanceof InvalidInstrumentException) {
            columnIndex += 1;
        }
        viewer.setSelection(new StructuredSelection(e.getRow()), true);
        CellSelection selection = new CellSelection(
            Collections.singletonList(e.getRow()),
            Collections.singletonList(Collections.singletonList(columnIndex)),
            e.getRow(),
            null);
        viewer.setSelection(selection, true);
    }
}
