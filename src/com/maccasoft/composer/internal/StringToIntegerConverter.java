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

import java.text.NumberFormat;

import org.eclipse.core.databinding.conversion.IConverter;

public class StringToIntegerConverter implements IConverter {

    static final NumberFormat nf = NumberFormat.getInstance();

    public StringToIntegerConverter() {

    }

    @Override
    public Object getFromType() {
        return String.class;
    }

    @Override
    public Object getToType() {
        return Integer.class;
    }

    @Override
    public Object convert(Object fromObject) {
        try {
            if (fromObject != null) {
                return nf.parse(fromObject.toString()).intValue();
            }
        } catch (Exception e) {
            // Do nothing
        }
        return new Double(0);
    }
}
