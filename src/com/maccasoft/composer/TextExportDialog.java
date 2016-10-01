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
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TextExportDialog extends Dialog {

    Button spin;
    Button clang;
    Text text;
    Label size;

    Font font;
    byte[] data;

    public TextExportDialog(Shell parentShell) {
        super(parentShell);
        setBlockOnOpen(true);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Song Data");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        applyDialogFont(composite);

        if ("win32".equals(SWT.getPlatform())) { //$NON-NLS-1$
            font = new Font(Display.getDefault(), "Courier New", 10, SWT.NONE); //$NON-NLS-1$
        }
        else {
            font = new Font(Display.getDefault(), "mono", 10, SWT.NONE); //$NON-NLS-1$
        }

        Composite container = new Composite(composite, SWT.NONE);
        container.setLayout(new GridLayout(2, false));
        spin = new Button(container, SWT.RADIO);
        spin.setText("Spin");
        spin.setSelection(true);
        spin.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateTextDump();
                text.setFocus();
            }
        });
        clang = new Button(container, SWT.RADIO);
        clang.setText("C Array");
        clang.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateTextDump();
                text.setFocus();
            }
        });

        text = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        text.setFont(font);
        text.setLayoutData(new GridData(convertWidthInCharsToPixels(80), convertHeightInCharsToPixels(25)));

        text.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                font.dispose();
            }
        });
        text.setFocus();

        size = new Label(composite, SWT.NONE);
        size.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

        updateTextDump();

        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    public void setSongData(byte[] data) {
        this.data = data;
    }

    void updateTextDump() {
        StringBuilder sb = new StringBuilder();
        try {
            if (spin.getSelection()) {
                displaySpin(sb);
            }
            else {
                displayC(sb);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        text.setText(sb.toString());
        size.setText(String.format("%d bytes", data.length));
    }

    void displaySpin(StringBuilder sb) {
        int c;
        Map<Integer, Integer> init = new HashMap<Integer, Integer>();
        List<Integer> instruments = new ArrayList<Integer>();
        List<Integer> patterns = new ArrayList<Integer>();

        int i = 0;

        sb.append("DAT\n\nmusic\n");
        sb.append(String.format("    byte    $%02X, $%02X ' flag\n\n", data[i], data[i + 1]));
        i += 2;

        c = 0;
        while (i < data.length) {
            int v = (data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8);
            sb.append(String.format("    byte    $%02X, $%02X", data[i], data[i + 1]));
            i += 2;
            if (v == 0) {
                sb.append(" ' end of instrument pointers\n\n");
                break;
            }
            else {
                instruments.add(v);
                sb.append(String.format(" ' instrument %d\n", c++));
            }
        }

        for (c = 0; c < 8; c++) {
            sb.append(String.format("    byte    $%02X, $%02X ' channel %d\n", data[i], data[i + 1], c));
            int v = (data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8);
            if (v != 0) {
                init.put(v, c);
            }
            i += 2;
        }

        if ((i % 4) != 0) {
            sb.append("    byte    ");
            sb.append(String.format("$%02X", data[i++]));
            while ((i % 4) != 0) {
                sb.append(String.format(", $%02X", data[i++]));
            }
            sb.append(" ' align to 4-bytes boundary\n");
        }

        sb.append("\n    ' pattern pointers\n");

        while (i < data.length) {
            sb.append(String.format("    byte    $%02X, $%02X, $%02X, $%02X", data[i], data[i + 1], data[i + 2], data[i + 3]));

            Integer ch = init.get(i - 2);
            if (ch != null) {
                sb.append(" ' channel " + ch);
            }
            sb.append("\n");

            if (data[i] == (byte) 0xFF && data[i + 1] == (byte) 0xFF && data[i + 2] == (byte) 0xFF && data[i + 3] == (byte) 0xFF) {
                break;
            }

            int v = (data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8);
            patterns.add(v & 0x7FFF);
            i += 4;
        }
        i += 4;

        sb.append("\n    ' instruments\n");

        int addr = i - 2;
        while (i < data.length && !patterns.contains(addr)) {
            sb.append(String.format("    byte    $%02X, $%02X, $%02X, $%02X", data[i], data[i + 1], data[i + 2], data[i + 3]));
            sb.append(String.format(", $%02X, $%02X, $%02X, $%02X", data[i + 4], data[i + 5], data[i + 6], data[i + 7]));
            if (instruments.contains(addr)) {
                sb.append(String.format(" ' instrument %d", instruments.indexOf(addr)));
            }
            sb.append("\n");
            i += 8;
            addr += 8;
        }

        sb.append("\n    ' patterns\n");

        StringBuilder pb = new StringBuilder();
        while (i < data.length) {
            if (patterns.contains(addr)) {
                if (pb.length() != 0) {
                    sb.append(pb);
                    sb.append("\n");
                }
                pb = new StringBuilder("    byte    ");
            }
            if (pb.length() > 12) {
                pb.append(", ");
            }
            pb.append(String.format("$%02X", data[i++]));
            addr++;
        }
        if (pb.length() != 0) {
            sb.append(pb);
            sb.append("\n");
        }

        text.setText(sb.toString());
    }

    void displayC(StringBuilder sb) {
        int c;
        Map<Integer, Integer> init = new HashMap<Integer, Integer>();
        List<Integer> instruments = new ArrayList<Integer>();
        List<Integer> patterns = new ArrayList<Integer>();

        int i = 0;

        sb.append("uint8_t music[] = {\n");
        sb.append(String.format("    0x%02X, 0x%02X, // flag\n\n", data[i], data[i + 1]));
        i += 2;

        c = 0;
        while (i < data.length) {
            int v = (data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8);
            sb.append(String.format("    0x%02X, 0x%02X,", data[i], data[i + 1]));
            i += 2;
            if (v == 0) {
                sb.append(" // end of instrument pointers\n\n");
                break;
            }
            else {
                instruments.add(v);
                sb.append(String.format(" // instrument %d\n", c++));
            }
        }

        for (c = 0; c < 8; c++) {
            sb.append(String.format("    0x%02X, 0x%02X, // channel %d\n", data[i], data[i + 1], c));
            int v = (data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8);
            if (v != 0) {
                init.put(v, c);
            }
            i += 2;
        }

        if ((i % 4) != 0) {
            sb.append("    ");
            sb.append(String.format("0x%02X,", data[i++]));
            while ((i % 4) != 0) {
                sb.append(String.format(" 0x%02X,", data[i++]));
            }
            sb.append(" // align to 4-bytes boundary\n");
        }

        sb.append("\n    // pattern pointers\n");

        while (i < data.length) {
            sb.append(String.format("    0x%02X, 0x%02X, 0x%02X, 0x%02X,", data[i], data[i + 1], data[i + 2], data[i + 3]));

            Integer ch = init.get(i - 2);
            if (ch != null) {
                sb.append(" // channel " + ch);
            }
            sb.append("\n");

            if (data[i] == (byte) 0xFF && data[i + 1] == (byte) 0xFF && data[i + 2] == (byte) 0xFF && data[i + 3] == (byte) 0xFF) {
                break;
            }

            int v = (data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8);
            patterns.add(v & 0x7FFF);
            i += 4;
        }
        i += 4;

        sb.append("\n    // instruments\n");

        int addr = i - 2;
        while (i < data.length && !patterns.contains(addr)) {
            sb.append(String.format("    0x%02X, 0x%02X, 0x%02X, 0x%02X,", data[i], data[i + 1], data[i + 2], data[i + 3]));
            sb.append(String.format(" 0x%02X, 0x%02X, 0x%02X, 0x%02X,", data[i + 4], data[i + 5], data[i + 6], data[i + 7]));
            if (instruments.contains(addr)) {
                sb.append(String.format(" // instrument %d", instruments.indexOf(addr)));
            }
            sb.append("\n");
            i += 8;
            addr += 8;
        }

        sb.append("\n    // patterns\n");

        StringBuilder pb = new StringBuilder();
        while (i < data.length) {
            if (patterns.contains(addr)) {
                if (pb.length() != 0) {
                    sb.append(pb);
                    sb.append("\n");
                }
                pb = new StringBuilder("    ");
            }
            if (pb.length() > 4) {
                pb.append(" ");
            }
            pb.append(String.format("0x%02X,", data[i++]));
            addr++;
        }
        if (pb.length() != 0) {
            sb.append(pb);
            sb.append("\n");
        }

        sb.append("};\n");

        text.setText(sb.toString());
    }
}
