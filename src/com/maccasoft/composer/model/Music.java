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

import java.util.ArrayList;
import java.util.List;

public class Music {

    String name;
    int bpm;
    List<Instrument> instruments = new ArrayList<Instrument>();
    Channel[] channels = new Channel[8];

    public Music(String name, int bpm) {
        this.name = name;
        this.bpm = bpm;
    }

    public Music(String name, int bpm, List<Instrument> instruments, Channel[] channels) {
        this.name = name;
        this.bpm = bpm;
        this.instruments = instruments;
        this.channels = channels;
    }

    public void add(Instrument element) {
        if (!instruments.contains(element)) {
            instruments.add(element);
        }
    }

    public int indexOf(Instrument element) {
        return instruments.indexOf(element);
    }

    public void setChannel(int index, Channel element) {
        this.channels[index] = element;
    }

    public String toCString() {
        int i;
        StringBuilder sb = new StringBuilder();

        i = 0;
        for (Instrument ins : instruments) {
            sb.append(String.format("static uint32_t ins%02d[] = {\n", i));
            for (Command cmd : ins.getCommands()) {
                sb.append("    ");
                sb.append(cmd.toCString());
                sb.append(",\n");
            }
            sb.append("};\n");
            i++;
        }

        for (i = 0; i < channels.length; i++) {
            if (channels[i] == null) {
                continue;
            }
            int seq = 0;
            for (Pattern pattern : channels[i].getPatterns()) {
                sb.append("\n");
                sb.append(pattern.toCString(String.format("ch%d_%d", i, seq)));
                seq++;
            }
        }

        for (i = 0; i < channels.length; i++) {
            if (channels[i] == null) {
                if (i == 7) {
                    sb.append("\n");
                    sb.append(String.format("static uint32_t ch%d[] = {\n", i));
                    sb.append(String.format("    SET_TEMPO(%d) | PATTERN(null_pattern),\n", bpm));
                    sb.append("    END\n};\n");
                }
                continue;
            }
            sb.append("\n");
            sb.append(String.format("static uint32_t ch%d[] = {\n", i));

            int seq = 0;
            for (Pattern pattern : channels[i].getPatterns()) {
                int note = Util.noteIndex(pattern.getBaseNote());
                if (i == 7 && seq == 0) {
                    sb.append(String.format("    SET_TEMPO(%d) | PATTERN(null_pattern),\n", bpm));
                }
                sb.append(String.format("    OCTAVE(%d) | NOTE(%d) | INSTR(%d) | PATTERN(ch%d_%d),\n",
                    10 - (((note - 1) / 12) + 1),
                    (note - 1) % 12,
                    instruments.indexOf(pattern.getInstrument()),
                    i, seq));
                seq++;
            }

            sb.append("    END\n};\n");
        }

        sb.append(String.format("\nuint32_t * %s[] = {\n", name));

        for (i = 0; i < instruments.size(); i++) {
            sb.append(String.format("    ins%02d,\n", i));
        }
        sb.append("    0,\n");

        for (i = 0; i < channels.length; i++) {
            if (i != 7 && channels[i] == null) {
                sb.append("    0,\n");
                continue;
            }
            sb.append(String.format("    ch%d,\n", i));
        }

        sb.append("};\n");

        return sb.toString();
    }

    public String toBinaryString() {
        ByteArrayWriter wr = toByteArray();
        return wr.toString();
    }

    public byte[] toArray() {
        ByteArrayWriter wr = toByteArray();
        return wr.toByteArray();
    }

    public ByteArrayWriter toByteArray() {
        int i;
        ByteArrayWriter sb = new ByteArrayWriter();

        sb.writeWord(0);

        // Instrument pointers
        int inspos = sb.getSize();
        for (i = 0; i < instruments.size(); i++) {
            sb.writeWord(0);
        }
        sb.writeWord(0);

        // channel data
        int chpos = sb.getSize();
        for (i = 0; i < channels.length; i++) {
            sb.writeWord(0);
        }

        while ((sb.getSize() % 4) != 0) {
            sb.writeByte(0);
        }

        // music patterns
        int patpos = sb.getSize();
        for (i = 0; i < channels.length; i++) {
            if (i == 7 || channels[i] != null) {
                sb.writeWord(chpos, sb.getSize() - 2);
            }
            chpos += 2;

            if (i == 7) {
                sb.writeLong(((1211401 * bpm) & 0xFFF00000L) | 0x8000);
            }

            if (channels[i] != null) {
                for (Pattern pattern : channels[i].getPatterns()) {
                    if (pattern.getBaseNote() == null) {
                        sb.writeLong(instruments.indexOf(pattern.getInstrument()) << 16);
                    }
                    else {
                        int note = Util.noteIndex(pattern.getBaseNote());
                        int o = 10 - (((note - 1) / 12) + 1);
                        int n = (note - 1) % 12;
                        sb.writeLong((o << 28) | (n << 24) | (instruments.indexOf(pattern.getInstrument()) << 16));
                    }
                }
            }

            if (i == 7 || channels[i] != null) {
                sb.writeLong(0);
            }
        }
        sb.writeLong(0xFFFFFFFFL);

        // instrument data
        i = 0;
        for (Instrument ins : instruments) {
            sb.writeWord(inspos, sb.getSize() - 2);
            inspos += 2;
            for (Command cmd : ins.getCommands()) {
                sb.writeLong(cmd.getCommand() & 0xFFFFFFFFL);
                sb.writeLong(cmd.getArgument() & 0xFFFFFFFFL);
            }
            i++;
        }

        // null byte for tempo pattern
        int null_pattern = sb.getSize() - 2;
        sb.writeByte(0);

        // music data
        for (i = 0; i < channels.length; i++) {
            if (i == 7) {
                sb.writeWord(patpos, null_pattern | 0x8000);
                patpos += 4;
            }

            if (channels[i] != null) {
                for (Pattern pattern : channels[i].getPatterns()) {
                    sb.writeWord(patpos, sb.getSize() - 2);
                    patpos += 4;
                    sb.append(pattern.toArray());
                }
            }

            if (i == 7 || channels[i] != null) {
                patpos += 4;
            }
        }

        return sb;
    }
}
