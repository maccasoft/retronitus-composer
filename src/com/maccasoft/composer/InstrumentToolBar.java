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
import java.util.List;

import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.maccasoft.composer.internal.ImageRegistry;
import com.maccasoft.composer.model.Command;
import com.maccasoft.composer.model.Instrument;
import com.maccasoft.composer.model.InstrumentBuilder;
import com.maccasoft.composer.model.Project;
import com.maccasoft.composer.model.Song;
import com.maccasoft.composer.model.SongRow;

import jssc.SerialPort;

public class InstrumentToolBar {

    Shell shell;
    ComboViewer viewer;
    ToolItem duplicate;
    ToolItem edit;
    ToolItem delete;

    Project project;
    SerialPort serialPort;

    public InstrumentToolBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(3, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        composite.setLayout(gridLayout);

        shell = parent.getShell();

        GC gc = new GC(parent);
        FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();

        Label label = new Label(composite, SWT.NONE);
        label.setText("Instrument");

        viewer = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.NO_FOCUS);
        viewer.getCombo().setVisibleItemCount(20);
        viewer.getCombo().setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 30), SWT.DEFAULT));
        viewer.setContentProvider(new ObservableListContentProvider());
        viewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                return String.format("%s - %s", project.getInstrumentId((Instrument) element), element.toString());
            }
        });

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                duplicate.setEnabled(!selection.isEmpty());
                edit.setEnabled(!selection.isEmpty());
                delete.setEnabled(!selection.isEmpty());
            }
        });

        ToolBar toolBar = new ToolBar(composite, SWT.FLAT | SWT.NO_FOCUS);
        ToolItem toolItem = new ToolItem(toolBar, SWT.PUSH);
        toolItem.setImage(ImageRegistry.getImageFromResources("application_add.png"));
        toolItem.setToolTipText("New instrument");
        toolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Instrument instrument = new InstrumentBuilder("New instrument") //
                    .setModulation(0, 50) //
                    .setVolume(95) //
                    .setEnvelope(2, 2).repeat(1) //
                    .jump(-1).build();

                InstrumentEditor editor = new InstrumentEditor(shell, instrument);
                editor.setSerialPort(serialPort);
                if (editor.open() == InstrumentEditor.OK) {
                    project.add(instrument);
                    viewer.setSelection(new StructuredSelection(instrument));
                }
            }
        });

        duplicate = new ToolItem(toolBar, SWT.PUSH);
        duplicate.setImage(ImageRegistry.getImageFromResources("application_double.png"));
        duplicate.setToolTipText("Duplicate instrument");
        duplicate.setEnabled(false);
        duplicate.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = viewer.getStructuredSelection();
                if (selection.isEmpty()) {
                    return;
                }
                Instrument selectedInstrument = (Instrument) selection.getFirstElement();

                Instrument instrument = new Instrument(selectedInstrument.getName() + " (1)");
                List<Command> list = new ArrayList<Command>();
                try {
                    for (Command cmd : selectedInstrument.getCommands()) {
                        list.add(cmd.clone());
                    }
                    instrument.setCommands(list);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                InstrumentEditor editor = new InstrumentEditor(shell, instrument);
                editor.setSerialPort(serialPort);
                if (editor.open() == InstrumentEditor.OK) {
                    project.add(instrument);
                    viewer.setSelection(new StructuredSelection(instrument));
                }
            }
        });

        edit = new ToolItem(toolBar, SWT.PUSH);
        edit.setImage(ImageRegistry.getImageFromResources("application_edit.png"));
        edit.setToolTipText("Edit instrument");
        edit.setEnabled(false);
        edit.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = viewer.getStructuredSelection();
                if (selection.isEmpty()) {
                    return;
                }
                Instrument selectedInstrument = (Instrument) selection.getFirstElement();

                InstrumentEditor editor = new InstrumentEditor(shell, selectedInstrument);
                editor.setSerialPort(serialPort);
                if (editor.open() == InstrumentEditor.OK) {
                    viewer.refresh();
                    viewer.setSelection(new StructuredSelection(selectedInstrument));
                }
            }
        });

        new ToolItem(toolBar, SWT.SEPARATOR);

        delete = new ToolItem(toolBar, SWT.PUSH);
        delete.setImage(ImageRegistry.getImageFromResources("application_delete.png"));
        delete.setToolTipText("Delete instrument");
        delete.setEnabled(false);
        delete.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = viewer.getStructuredSelection();
                if (selection.isEmpty()) {
                    return;
                }

                Instrument selectedInstrument = (Instrument) selection.getFirstElement();
                if (isInstrumentUsed(selectedInstrument)) {
                    if (!MessageDialog.openConfirm(shell, Main.APP_TITLE,
                        "The instrument is used in a song.  You really want to delete?")) {
                        return;
                    }
                }
                project.remove(selectedInstrument);
            }
        });
    }

    boolean isInstrumentUsed(Instrument ins) {
        String insId = String.format("%02X", project.getInstruments().indexOf(ins));
        for (Song song : project.getSongs()) {
            for (SongRow row : song.getRows()) {
                for (int i = 0; i < 8; i++) {
                    if (insId.equals(row.getInstrument(i))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void addSelectionChangedListener(ISelectionChangedListener l) {
        viewer.addSelectionChangedListener(l);
    }

    public void removeSelectionChangedListener(ISelectionChangedListener l) {
        viewer.removeSelectionChangedListener(l);
    }

    public void setProject(Project project) {
        this.project = project;
        this.viewer.setInput(project.getObservableInstruments());
    }

    public void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public ISelection getSelection() {
        return viewer.getSelection();
    }

    public IStructuredSelection getStructuredSelection() {
        return viewer.getStructuredSelection();
    }
}
