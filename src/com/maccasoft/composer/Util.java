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

import java.text.NumberFormat;

public class Util {

    static final NumberFormat nf = NumberFormat.getInstance();
    static {
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(2);
    }

    static final String[] noteText = new String[] {
        "C-", "C#", "D-", "D#", "E-", "F-", "F#", "G-", "G#", "A-", "A#", "B-"
    };

    public String format(double v) {
        return nf.format(v);
    }

    public static final boolean equals(final Object left, final Object right) {
        return left == null ? right == null : right != null && left.equals(right);
    }

    public static String noteFromIndex(int index) {
        int n = (index - 1) % 12;
        int o = (index - 1) / 12;
        return String.format("%s%d", noteText[n], o + 1);
    }
}
