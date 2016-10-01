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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.maccasoft.composer.internal.ImageRegistry;
import com.maccasoft.composer.model.Command;
import com.maccasoft.composer.model.Instrument;
import com.maccasoft.composer.model.Music;
import com.maccasoft.composer.model.Project;
import com.maccasoft.composer.model.ProjectCompiler;

import jssc.SerialPortException;

public class Main {

    public static final String APP_TITLE = "Retronitus Composer";
    public static final String APP_VERSION = "0.1.0";

    Shell shell;
    MusicEditor editor;
    ProgressIndicator progressBar;
    Label portLabel;

    File projectFile;
    Project project;

    SerialPortList serialPortList;

    public Main(Shell shell) {
        this.shell = shell;

        shell.setLayout(new GridLayout(1, false));

        project = new Project();

        Menu menu = new Menu(shell, SWT.BAR);
        createFileMenu(menu);
        createToolsMenu(menu);
        createHelpMenu(menu);
        shell.setMenuBar(menu);

        editor = new MusicEditor(shell);
        editor.setProject(project);

        createStatusBar(shell);
        updateShellTitle();

        serialPortList = new SerialPortList();
        serialPortList.addObserver(new Observer() {

            @Override
            public void update(Observable o, Object arg) {
                portLabel.setText((String) arg);
                editor.setSerialPort((String) arg);
            }
        });
        portLabel.setText(serialPortList.getSelection());
        editor.setSerialPort(serialPortList.getSelection());
    }

    void createFileMenu(Menu parent) {
        Menu menu = new Menu(parent.getParent(), SWT.DROP_DOWN);

        MenuItem item = new MenuItem(parent, SWT.CASCADE);
        item.setText("File");
        item.setMenu(menu);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("New");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                projectFile = null;
                project = new Project();
                editor.setProject(project);
                updateShellTitle();
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Open...");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleFileOpen();
                    updateShellTitle();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Save\tCTRL+S");
        item.setAccelerator(SWT.MOD1 + 'S');
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleFileSave();
                    updateShellTitle();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Save As...");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                try {
                    handleFileSaveAs();
                    updateShellTitle();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Import intrument");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                handleImportInstrument();
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Exit");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                shell.dispose();
            }
        });
    }

    void createToolsMenu(Menu parent) {
        Menu menu = new Menu(parent.getParent(), SWT.DROP_DOWN);

        MenuItem item = new MenuItem(parent, SWT.CASCADE);
        item.setText("&Tools");
        item.setMenu(menu);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("View compiled song");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                ProjectCompiler compiler = new ProjectCompiler(project);
                Music music = compiler.build(editor.getCurrentSong());

                TextExportDialog dlg = new TextExportDialog(shell);
                dlg.setSongData(music.toArray());
                dlg.open();
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Upload player");
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                handlePlayerUpload();
            }
        });

        new MenuItem(menu, SWT.SEPARATOR);

        createPortMenu(menu);
    }

    void createPortMenu(Menu parent) {
        final Menu menu = new Menu(parent.getParent(), SWT.DROP_DOWN);
        menu.addMenuListener(new MenuListener() {

            @Override
            public void menuShown(MenuEvent e) {
                serialPortList.fillMenu(menu);
            }

            @Override
            public void menuHidden(MenuEvent e) {
            }
        });

        MenuItem item = new MenuItem(parent, SWT.CASCADE);
        item.setText("&Port");
        item.setMenu(menu);
    }

    void createHelpMenu(Menu parent) {
        Menu menu = new Menu(parent.getParent(), SWT.DROP_DOWN);

        MenuItem item = new MenuItem(parent, SWT.CASCADE);
        item.setText("&Help");
        item.setMenu(menu);

        item = new MenuItem(menu, SWT.PUSH);
        item.setText("About " + APP_TITLE);
        item.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event e) {
                AboutDialog dlg = new AboutDialog(shell);
                dlg.open();
            }
        });
    }

    void createStatusBar(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        layoutData.heightHint = 27;
        container.setLayoutData(layoutData);

        Label messageLabel = new Label(container, SWT.NONE);
        layoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        messageLabel.setLayoutData(layoutData);

        Label label = new Label(container, SWT.SEPARATOR);
        layoutData = new GridData(SWT.FILL, SWT.CENTER, false, true);
        label.setLayoutData(layoutData);

        portLabel = new Label(container, SWT.NONE);
        layoutData = new GridData(SWT.FILL, SWT.CENTER, false, true);
        layoutData.widthHint = 128;
        portLabel.setLayoutData(layoutData);

        label = new Label(container, SWT.SEPARATOR);
        layoutData = new GridData(SWT.FILL, SWT.CENTER, false, true);
        label.setLayoutData(layoutData);

        progressBar = new ProgressIndicator(container, SWT.HORIZONTAL);
        layoutData = new GridData(SWT.FILL, SWT.CENTER, false, true);
        layoutData.widthHint = 128;
        progressBar.setLayoutData(layoutData);

        container.setLayout(new GridLayout(container.getChildren().length, false));
    }

    private void handleFileOpen() {
        FileDialog dlg = new FileDialog(shell, SWT.OPEN);
        String[] filterNames = new String[] {
            "Music Projects (*.xml)"
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
                        File fileToOpen = new File(fileName);
                        BufferedReader is = new BufferedReader(new FileReader(fileToOpen));
                        try {
                            Project projectToOpen = new Project(is);
                            editor.setProject(projectToOpen);
                            project = projectToOpen;
                            projectFile = fileToOpen;
                        } finally {
                            is.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void handleFileSave() throws Exception {
        if (projectFile == null) {
            File newFile = getFileToSaveTo(null);
            if (newFile == null) {
                return;
            }
            projectFile = newFile;
        }

        PrintStream os = new PrintStream(new FileOutputStream(projectFile));
        try {
            project.writeTo(os);
        } finally {
            os.close();
        }
    }

    private void handleFileSaveAs() throws Exception {
        File newFile = getFileToSaveTo(projectFile);
        if (newFile == null) {
            return;
        }
        projectFile = newFile;

        PrintStream os = new PrintStream(new FileOutputStream(projectFile));
        try {
            project.writeTo(os);
        } finally {
            os.close();
        }
    }

    private File getFileToSaveTo(File currentFile) {
        FileDialog dlg = new FileDialog(shell, SWT.SAVE);
        if (currentFile != null) {
            dlg.setFilterPath(currentFile.getParent());
        }
        dlg.setFileName(currentFile != null ? currentFile.getName() : "*.xml");
        dlg.setText("Save File");

        String fileName = dlg.open();
        if (fileName == null) {
            return null;
        }
        return new File(fileName);
    }

    void updateShellTitle() {
        String title = projectFile != null ? projectFile.getName() : "Untitled";
        shell.setText(String.format("%s - %s", title, APP_TITLE));
    }

    void handlePlayerUpload() {
        File file = new File("Player/Player.binary");
        if (!file.exists()) {
            file = new File("Player.binary");
        }
        if (!file.exists()) {
            MessageDialog.openError(shell, APP_TITLE, "Player binary file not found!");
            return;
        }

        final File binaryFile = file;

        ProgressMonitorDialog dlg = new ProgressMonitorDialog(shell);
        try {
            dlg.setOpenOnRun(true);
            dlg.run(true, false, new IRunnableWithProgress() {

                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    PropellerLoader loader = new PropellerLoader(editor.getSerialPort(), true) {

                        SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, 1);

                        @Override
                        protected void bufferUpload(int type, byte[] binaryImage) throws SerialPortException, IOException {
                            StringBuilder sb = new StringBuilder("Loading binary image to ");
                            switch (type) {
                                case DOWNLOAD_EEPROM:
                                case DOWNLOAD_RUN_EEPROM:
                                    sb.append("EEPROM via ");
                                    // fall through
                                case DOWNLOAD_RUN_BINARY:
                                    sb.append("hub memory");
                                    break;
                            }
                            monitor.beginTask(sb.toString(), IProgressMonitor.UNKNOWN);
                            super.bufferUpload(type, binaryImage);
                        }

                        @Override
                        protected void notifyProgress(int sent, int total) {
                            subProgressMonitor.subTask(String.format("%d bytes remaining             \r", total - sent));
                        }

                        @Override
                        protected void verifyRam() throws SerialPortException, IOException {
                            subProgressMonitor.subTask("Verifying RAM ... ");
                            super.verifyRam();
                        }

                        @Override
                        protected void eepromWrite() throws SerialPortException, IOException {
                            subProgressMonitor.subTask("Programming EEPROM ... ");
                            super.eepromWrite();
                        }

                        @Override
                        protected void eepromVerify() throws SerialPortException, IOException {
                            subProgressMonitor.subTask("Verifying EEPROM ... ");
                            super.eepromVerify();
                        }
                    };

                    try {
                        loader.upload(binaryFile, PropellerLoader.DOWNLOAD_RUN_EEPROM);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private void handleImportInstrument() {
        FileDialog dlg = new FileDialog(shell, SWT.OPEN);
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

                        String defaultName = fileToOpen.getName();
                        if (defaultName.toLowerCase().endsWith(".xml")) {
                            defaultName = defaultName.substring(0, defaultName.length() - 4);
                        }

                        BufferedReader is = new BufferedReader(new FileReader(fileToOpen));
                        try {
                            Instrument instrument = new Instrument(defaultName);
                            while ((line = is.readLine()) != null) {
                                if (line.contains("<instrument")) {
                                    if (instrument.getCommands().size() != 0) {
                                        project.add(instrument);
                                    }
                                    int s = line.indexOf("name=\"") + 6;
                                    int e = line.indexOf('"', s);
                                    instrument = new Instrument(line.substring(s, e));
                                }
                                else if (line.contains("</instrument")) {
                                    if (instrument.getCommands().size() != 0) {
                                        project.add(instrument);
                                    }
                                    instrument = new Instrument(defaultName);
                                }
                                else if (line.contains("<jump ") || line.contains("<set-") || line.contains("<modify-")) {
                                    instrument.add(Command.fromXml(line));
                                }
                            }
                            if (instrument.getCommands().size() != 0) {
                                project.add(instrument);
                            }
                        } finally {
                            is.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    static {
        System.setProperty("SWT_GTK3", "0");
    }

    public static void main(String[] args) {
        final Display display = new Display();

        Realm.runWithDefault(DisplayRealm.getRealm(display), new Runnable() {

            @Override
            public void run() {
                try {
                    final Shell shell = new Shell(display);
                    shell.setText(APP_TITLE);

                    Image[] images = new Image[] {
                        ImageRegistry.getImageFromResources("app64.png"),
                        ImageRegistry.getImageFromResources("app48.png"),
                        ImageRegistry.getImageFromResources("app32.png"),
                        ImageRegistry.getImageFromResources("app16.png"),
                    };
                    shell.setImages(images);

                    Rectangle screen = display.getClientArea();

                    Rectangle rect = new Rectangle(0, 0, (int) (screen.width * 0.85), (int) (screen.height * 0.85));
                    rect.x = (screen.width - rect.width) / 2;
                    rect.y = (screen.height - rect.height) / 2;
                    if (rect.y < 0) {
                        rect.height += rect.y * 2;
                        rect.y = 0;
                    }

                    shell.setLocation(rect.x, rect.y);
                    shell.setSize(rect.width, rect.height);

                    new Main(shell);

                    shell.open();

                    while (display.getShells().length != 0) {
                        if (!display.readAndDispatch()) {
                            display.sleep();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        });

        display.dispose();
    }
}
