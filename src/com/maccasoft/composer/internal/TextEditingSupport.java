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

package com.maccasoft.composer.internal;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;

public abstract class TextEditingSupport extends EditingSupport {

    TextCellEditor cellEditor;

    public TextEditingSupport(ColumnViewer viewer) {
        super(viewer);
    }

    @Override
    protected boolean canEdit(Object element) {
        return true;
    }

    @Override
    protected CellEditor getCellEditor(Object element) {
        if (cellEditor == null) {
            cellEditor = new TextCellEditor((Composite) getViewer().getControl(), SWT.CENTER) {

                @Override
                protected void keyReleaseOccured(KeyEvent e) {
                    if (e.keyCode == SWT.ARROW_UP) {
                        fireApplyEditorValue();
                        deactivate();

                        ViewerCell cell = getViewer().getColumnViewerEditor().getFocusCell();
                        if (cell != null) {
                            final ViewerCell nextCell = cell.getNeighbor(ViewerCell.ABOVE, false);
                            if (nextCell != null) {
                                getViewer().getControl().getDisplay().asyncExec(new Runnable() {

                                    @Override
                                    public void run() {
                                        GridTableViewer viewer = (GridTableViewer) getViewer();
                                        Grid grid = (Grid) viewer.getControl();
                                        if (!grid.isDisposed()) {
                                            viewer.editElement(nextCell.getElement(), nextCell.getColumnIndex());
                                        }
                                    }
                                });
                            }
                        }
                        e.doit = false;
                    }
                    else if (e.keyCode == SWT.ARROW_DOWN) {
                        fireApplyEditorValue();
                        deactivate();

                        ViewerCell cell = getViewer().getColumnViewerEditor().getFocusCell();
                        if (cell != null) {
                            final ViewerCell nextCell = cell.getNeighbor(ViewerCell.BELOW, false);
                            if (nextCell != null) {
                                getViewer().getControl().getDisplay().asyncExec(new Runnable() {

                                    @Override
                                    public void run() {
                                        GridTableViewer viewer = (GridTableViewer) getViewer();
                                        Grid grid = (Grid) viewer.getControl();
                                        if (!grid.isDisposed()) {
                                            viewer.editElement(nextCell.getElement(), nextCell.getColumnIndex());
                                        }
                                    }
                                });
                            }
                        }
                        e.doit = false;
                    }
                    super.keyReleaseOccured(e);
                }
            };
        }
        return cellEditor;
    }
}
