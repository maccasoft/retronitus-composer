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
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

@SuppressWarnings("rawtypes")
public class BeanPropertyCellLabelProvider extends CellLabelProvider {

    final int index;
    final String propertyName;

    final PropertyDescriptor descriptor;

    public BeanPropertyCellLabelProvider(Class beanClass, String propertyName) {
        this.index = 0;
        this.propertyName = propertyName;

        descriptor = BeanPropertyHelper.getPropertyDescriptor(beanClass, propertyName);
    }

    public BeanPropertyCellLabelProvider(Class beanClass, int index, String propertyName) {
        this.index = index;
        this.propertyName = propertyName;

        descriptor = BeanPropertyHelper.getPropertyDescriptor(beanClass, propertyName);
    }

    @Override
    public void update(ViewerCell cell) {
        Object value = getValue(cell.getElement());
        cell.setText(value != null ? value.toString() : "");
    }

    protected Object getValue(Object element) {
        if (descriptor instanceof IndexedPropertyDescriptor) {
            Method method = ((IndexedPropertyDescriptor) descriptor).getIndexedReadMethod();
            try {
                return method.invoke(element, index);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Method method = descriptor.getReadMethod();
            try {
                return method.invoke(element);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int getIndex() {
        return index;
    }
}
