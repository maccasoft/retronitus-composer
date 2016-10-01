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

package com.maccasoft.composer.model;

import java.util.Arrays;

public class ByteArrayWriter {

    byte buf[];
    int count;

    public ByteArrayWriter() {
        buf = new byte[1024];
    }

    public void writeByte(int b) {
        ensureCapacity(count + 1);
        buf[count++] = (byte) b;
    }

    public void writeByte(int pos, int b) {
        ensureCapacity(pos + 1);
        buf[pos++] = (byte) b;
        if (count < pos) {
            count = pos;
        }
    }

    public void writeWord(int b) {
        ensureCapacity(count + 2);
        buf[count++] = (byte) b;
        buf[count++] = (byte) (b >> 8);
    }

    public void writeWord(int pos, int b) {
        ensureCapacity(pos + 2);
        buf[pos++] = (byte) b;
        buf[pos++] = (byte) (b >> 8);
        if (count < pos) {
            count = pos;
        }
    }

    public void writeLong(int b) {
        ensureCapacity(count + 4);
        buf[count++] = (byte) b;
        buf[count++] = (byte) (b >> 8);
        buf[count++] = (byte) (b >> 16);
        buf[count++] = (byte) (b >> 24);
    }

    public void writeLong(int pos, int b) {
        ensureCapacity(pos + 4);
        buf[pos++] = (byte) b;
        buf[pos++] = (byte) (b >> 8);
        buf[pos++] = (byte) (b >> 16);
        buf[pos++] = (byte) (b >> 24);
        if (count < pos) {
            count = pos;
        }
    }

    public void writeLong(long b) {
        ensureCapacity(count + 4);
        buf[count++] = (byte) b;
        buf[count++] = (byte) (b >> 8);
        buf[count++] = (byte) (b >> 16);
        buf[count++] = (byte) (b >> 24);
    }

    public void writeLong(int pos, long b) {
        ensureCapacity(pos + 4);
        buf[pos++] = (byte) b;
        buf[pos++] = (byte) (b >> 8);
        buf[pos++] = (byte) (b >> 16);
        buf[pos++] = (byte) (b >> 24);
        if (count < pos) {
            count = pos;
        }
    }

    private void ensureCapacity(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - buf.length > 0) {
            grow(minCapacity);
        }
    }

    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = buf.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        if (newCapacity < 0) {
            if (minCapacity < 0) {
                throw new OutOfMemoryError();
            }
            newCapacity = Integer.MAX_VALUE;
        }
        buf = Arrays.copyOf(buf, newCapacity);
    }

    public void append(byte[] array) {
        ensureCapacity(count + array.length);
        System.arraycopy(array, 0, buf, count, array.length);
        count += array.length;
    }

    public int getSize() {
        return count;
    }

    public byte toByteArray()[] {
        return Arrays.copyOf(buf, count);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        int i = 0;
        while (i < count) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(String.format("0x%02X,", buf[i++]));
        }

        return sb.toString();
    }
}
