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

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import org.eclipse.core.internal.databinding.beans.BeanPropertyHelper;
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

@SuppressWarnings("rawtypes")
public class BeanPropertyTextEditingSupport extends EditingSupport {

    final int index;
    final String propertyName;

    final PropertyDescriptor descriptor;
    final Method readMethod;
    final Method writeMethod;
    TextCellEditor cellEditor;

    public BeanPropertyTextEditingSupport(ColumnViewer viewer, Class beanClass, String propertyName) {
        super(viewer);

        this.index = 0;
        this.propertyName = propertyName;

        descriptor = BeanPropertyHelper.getPropertyDescriptor(beanClass, propertyName);
        readMethod = descriptor.getReadMethod();
        writeMethod = descriptor.getWriteMethod();
    }

    public BeanPropertyTextEditingSupport(ColumnViewer viewer, Class beanClass, int index, String propertyName) {
        super(viewer);

        this.index = index;
        this.propertyName = propertyName;

        descriptor = BeanPropertyHelper.getPropertyDescriptor(beanClass, propertyName);
        readMethod = ((IndexedPropertyDescriptor) descriptor).getIndexedReadMethod();
        writeMethod = ((IndexedPropertyDescriptor) descriptor).getIndexedWriteMethod();
    }

    @Override
    protected boolean canEdit(Object element) {
        return true;
    }

    @Override
    protected Object getValue(Object element) {
        try {
            Object value;
            if (descriptor instanceof IndexedPropertyDescriptor) {
                value = readMethod.invoke(element, index);
            }
            else {
                value = readMethod.invoke(element);
            }
            if (value != null) {
                return value.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void setValue(Object element, Object value) {
        try {
            if (descriptor instanceof IndexedPropertyDescriptor) {
                writeMethod.invoke(element, index, value != null ? value.toString() : "");
            }
            else {
                writeMethod.invoke(element, value != null ? value.toString() : "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getViewer().update(element, null);
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
