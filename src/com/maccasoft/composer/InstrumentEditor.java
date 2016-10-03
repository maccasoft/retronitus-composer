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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiffVisitor;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.maccasoft.composer.internal.ImageRegistry;
import com.maccasoft.composer.model.Command;
import com.maccasoft.composer.model.Instrument;

import jssc.SerialPort;

public class InstrumentEditor extends Window {

    Text name;
    ScrolledComposite sc;
    Composite instrumentContainer;
    InstrumentChart chart;
    PianoKeyboard keyBoard;

    final List<Command> list = new ArrayList<Command>();
    final WritableList observableList = new WritableList(list, Command.class);

    Instrument instrument;
    DataBindingContext context;
    private FontMetrics fontMetrics;
    private SerialPort serialPort;

    final IListChangeListener listChangedListener = new IListChangeListener() {

        @Override
        public void handleListChange(ListChangeEvent event) {
            event.diff.accept(new ListDiffVisitor() {

                @Override
                public void handleAdd(int index, Object element) {
                    Control[] childs = instrumentContainer.getChildren();
                    Control control = createInstrumentControls((Command) element);
                    if (index < childs.length) {
                        control.moveAbove(childs[index]);
                    }
                    ((Command) element).addPropertyChangeListener(propertyChangeListener);
                }

                @Override
                public void handleRemove(int index, Object element) {
                    Control[] childs = instrumentContainer.getChildren();
                    childs[index].dispose();
                    ((Command) element).removePropertyChangeListener(propertyChangeListener);
                }
            });
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    if (sc.isDisposed()) {
                        return;
                    }
                    Rectangle r = sc.getClientArea();
                    sc.setMinSize(sc.getContent().computeSize(r.width, SWT.DEFAULT));
                    sc.layout(true, true);
                    Display.getDefault().timerExec(250, updateRunnable);
                }
            });
        }
    };

    final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    Display.getDefault().timerExec(250, updateRunnable);
                }
            });
        }
    };

    final Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            if (chart.getControl().isDisposed()) {
                return;
            }

            int count = 0;
            for (Command cmd : list) {
                if (!cmd.isDisabled()) {
                    count++;
                }
            }

            int[][] waveTable = new int[count][2];

            count = 0;
            for (Command cmd : list) {
                if (!cmd.isDisabled()) {
                    waveTable[count][0] = (int) cmd.getCommand();
                    waveTable[count][1] = (int) cmd.getArgument();
                    count++;
                }
            }

            chart.setWaveTable(waveTable);
            keyBoard.setWaveTable(waveTable);
        }
    };

    public InstrumentEditor(Shell parentShell, Instrument instrument) {
        super(parentShell);
        this.instrument = instrument;
        setBlockOnOpen(true);
        setReturnCode(CANCEL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);

        FillLayout layout = new FillLayout();
        layout.marginWidth = layout.marginHeight = 5;
        newShell.setLayout(layout);
        newShell.setText("Instrument Editor");
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, false));

        GC gc = new GC(container);
        fontMetrics = gc.getFontMetrics();
        gc.dispose();

        context = new DataBindingContext();

        createToolBar(container);
        createControls(container);
        createButtonBar(container);

        try {
            for (Command o : instrument.getCommands()) {
                observableList.add(o.clone());
            }
        } catch (Exception e) {
            // Do nothing, clone is supported
        }
        observableList.addListChangeListener(listChangedListener);

        for (Command element : list) {
            createInstrumentControls(element);
            element.addPropertyChangeListener(propertyChangeListener);
        }

        container.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                context.dispose();
                observableList.removeListChangeListener(listChangedListener);
                for (Command element : list) {
                    createInstrumentControls(element);
                    element.removePropertyChangeListener(propertyChangeListener);
                }
            }
        });

        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                Rectangle r = sc.getClientArea();
                sc.setMinSize(sc.getContent().computeSize(r.width, SWT.DEFAULT));
                sc.layout(true, true);
                Display.getDefault().timerExec(250, updateRunnable);
            }
        });

        return container;
    }

    void createToolBar(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);
        container.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        name = new Text(container, SWT.BORDER);
        name.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        ((GridData) name.getLayoutData()).widthHint = Dialog.convertWidthInCharsToPixels(fontMetrics, 30);
        name.setText(instrument.getName());

        ToolBar toolBar = new ToolBar(container, SWT.FLAT);
        toolBar.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        ToolItem toolItem = new ToolItem(toolBar, SWT.PUSH);
        toolItem.setImage(ImageRegistry.getImageFromResources("document-import.png"));
        toolItem.setToolTipText("Import from file");
        toolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    handleImportFromFile();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        toolItem = new ToolItem(toolBar, SWT.PUSH);
        toolItem.setImage(ImageRegistry.getImageFromResources("document-export.png"));
        toolItem.setToolTipText("Export to file");
        toolItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    handleExportToFile();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    void createControls(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Commands");
        FillLayout fillLayout = new FillLayout();
        fillLayout.marginWidth = fillLayout.marginHeight = 0;
        group.setLayout(fillLayout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

        sc = new ScrolledComposite(group, SWT.V_SCROLL);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);

        instrumentContainer = new Composite(sc, SWT.NONE);
        instrumentContainer.setLayout(new RowLayout(SWT.VERTICAL));

        sc.setContent(instrumentContainer);
        sc.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(ControlEvent e) {
                Rectangle r = sc.getClientArea();
                sc.setMinSize(sc.getContent().computeSize(r.width, SWT.DEFAULT));
            }
        });

        chart = new InstrumentChart(parent);
        chart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        keyBoard = new PianoKeyboard(parent);
        GridData gridData = new GridData(SWT.CENTER, SWT.FILL, false, false, 2, 1);
        gridData.heightHint = 80;
        gridData.widthHint = (keyBoard.getKeyWidth() * 7) * 8 + keyBoard.getKeyWidth();
        keyBoard.setLayoutData(gridData);
        keyBoard.setSerialPort(serialPort);
    }

    protected Control createButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.numColumns = 0;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        layout.marginTop = Dialog.convertVerticalDLUsToPixels(fontMetrics, IDialogConstants.VERTICAL_MARGIN);
        layout.horizontalSpacing = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.HORIZONTAL_SPACING);
        layout.verticalSpacing = Dialog.convertVerticalDLUsToPixels(fontMetrics, IDialogConstants.VERTICAL_SPACING);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        Button button = createButton(composite, -1, "Get Spin Data");
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                StringBuilder sb = new StringBuilder();
                sb.append("DAT\n\n");

                sb.append(name.getText().replace(' ', '_'));
                sb.append("\n");

                for (Command cmd : list) {
                    if (!cmd.isDisabled()) {
                        sb.append("    " + cmd.toSpinString() + "\n");
                    }
                }

                TextDialog dlg = new TextDialog(getShell());
                dlg.setString(sb.toString());
                dlg.open();
            }
        });

        button = createButton(composite, -1, "Get C Data");
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("uint32_t %s[] = {\n", name.getText().replace(' ', '_')));

                for (Command cmd : list) {
                    if (!cmd.isDisabled()) {
                        sb.append("    " + cmd.toCString() + ",\n");
                    }
                }

                sb.append("};\n");

                TextDialog dlg = new TextDialog(getShell());
                dlg.setString(sb.toString());
                dlg.open();
            }
        });

        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        layout.numColumns++;

        button = createButton(composite, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL);
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                okPressed();
                setReturnCode(OK);
                close();
            }
        });
        getShell().setDefaultButton(button);

        button = createButton(composite, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL);
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                close();
            }
        });

        return composite;
    }

    protected Button createButton(Composite parent, int id, String label) {
        ((GridLayout) parent.getLayout()).numColumns++;

        Button button = new Button(parent, SWT.PUSH);
        button.setText(label);
        button.setFont(JFaceResources.getDialogFont());
        button.setData(new Integer(id));

        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics, IDialogConstants.BUTTON_WIDTH);
        Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint = Math.max(widthHint, minSize.x);
        button.setLayoutData(data);

        return button;
    }

    Control createInstrumentControls(final Command element) {
        Composite container = new Composite(instrumentContainer, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);

        CommandControl control = new CommandControl(container);
        control.bind(context, element);

        ToolBar toolBar = new ToolBar(container, SWT.FLAT);

        ToolItem button = new ToolItem(toolBar, SWT.PUSH);
        button.setImage(ImageRegistry.getImageFromResources("arrow-090.png"));
        button.setToolTipText("Move up");
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = observableList.indexOf(element);
                if (index > 0) {
                    observableList.remove(index);
                    observableList.add(index - 1, element);
                    instrumentContainer.layout(true);
                }
            }
        });

        button = new ToolItem(toolBar, SWT.PUSH);
        button.setImage(ImageRegistry.getImageFromResources("arrow-270.png"));
        button.setToolTipText("Move down");
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = observableList.indexOf(element);
                if (index < (observableList.size() - 1)) {
                    observableList.remove(index);
                    observableList.add(index + 1, element);
                    instrumentContainer.layout(true);
                }
            }
        });

        button = new ToolItem(toolBar, SWT.PUSH);
        button.setImage(ImageRegistry.getImageFromResources("plus.png"));
        button.setToolTipText("Add new command");
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (observableList.size() == 1) {
                    observableList.add(0, new Command(Command.JUMP, 0));
                }
                else {
                    int index = observableList.indexOf(element);
                    observableList.add(index + 1, new Command(Command.JUMP, 0));
                }
                instrumentContainer.layout();
            }
        });

        button = new ToolItem(toolBar, SWT.PUSH);
        button.setImage(ImageRegistry.getImageFromResources("minus.png"));
        button.setToolTipText("Delete");
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                observableList.remove(element);
                if (observableList.size() == 0) {
                    observableList.add(new Command(Command.JUMP, -1));
                }
                instrumentContainer.layout(true);
            }
        });

        return container;
    }

    protected void okPressed() {
        instrument.setName(name.getText());
        instrument.setCommands(list);
    }

    public void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    private void handleExportToFile() throws Exception {
        String name = this.name.getText();
        if ("".equals(name)) {
            name = "*.xml";
        }
        else {
            name += ".xml";
        }

        FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
        dlg.setFileName(name);
        dlg.setText("Save File");

        String fileName = dlg.open();
        if (fileName != null) {
            Writer os = new OutputStreamWriter(new FileOutputStream(fileName));
            try {
                for (Command cmd : list) {
                    os.write(cmd.toXmlString());
                    os.write("\n");
                }
            } finally {
                os.close();
            }
        }
    }

    private void handleImportFromFile() {
        FileDialog dlg = new FileDialog(getShell(), SWT.OPEN);
        String[] filterNames = new String[] {
            "Instrument Files (*.xml)"
        };
        String[] filterExtensions = new String[] {
            "*.xml"
        };
        dlg.setFilterNames(filterNames);
        dlg.setFilterExtensions(filterExtensions);
        dlg.setText("Open File");

        final String fileName = dlg.open();
        if (fileName != null) {
            BusyIndicator.showWhile(Display.getDefault(), new Runnable() {

                @Override
                public void run() {
                    try {
                        String line;
                        File fileToOpen = new File(fileName);
                        List<Command> list = new ArrayList<Command>();

                        BufferedReader is = new BufferedReader(new FileReader(fileToOpen));
                        try {
                            while ((line = is.readLine()) != null) {
                                if (line.contains("</instrument")) {
                                    break;
                                }
                                else if (line.contains("<jump ") || line.contains("<set-") || line.contains("<modify-")) {
                                    list.add(Command.fromXml(line));
                                }
                            }
                        } finally {
                            is.close();
                        }

                        if (list.size() != 0) {
                            observableList.clear();
                            observableList.addAll(list);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
