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

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

@SuppressWarnings("rawtypes")
public class ListSelectionDialog extends Dialog {

    CheckboxTableViewer viewer;

    private String title;
    private String message;
    private List elements;
    private List selectedElements;

    public ListSelectionDialog(Shell parentShell, String title, String message, List elements) {
        super(parentShell);
        this.title = title;
        this.message = message;
        this.elements = elements;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(title);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        applyDialogFont(composite);

        Label label = new Label(composite, SWT.NONE);
        label.setText(message);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        viewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.widthHint = convertWidthInCharsToPixels(50);
        layoutData.heightHint = viewer.getTable().getItemHeight() * 20;
        viewer.getControl().setLayoutData(layoutData);
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new LabelProvider());
        viewer.setInput(elements);

        return composite;
    }

    @Override
    protected void okPressed() {
        selectedElements = Arrays.asList(viewer.getCheckedElements());
        super.okPressed();
    }

    public List getSelectedElements() {
        return selectedElements;
    }
}
