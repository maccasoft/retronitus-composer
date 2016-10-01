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

public class MusicBuilder {

    String name;
    int bpm;
    List<InstrumentBuilder> instruments = new ArrayList<InstrumentBuilder>();
    List<List<PatternBuilder>> channels = new ArrayList<List<PatternBuilder>>();

    public MusicBuilder(String name) {
        this.name = name;
        channels.add(new ArrayList<PatternBuilder>());
        channels.add(new ArrayList<PatternBuilder>());
        channels.add(new ArrayList<PatternBuilder>());
        channels.add(new ArrayList<PatternBuilder>());
        channels.add(new ArrayList<PatternBuilder>());
        channels.add(new ArrayList<PatternBuilder>());
        channels.add(new ArrayList<PatternBuilder>());
        channels.add(new ArrayList<PatternBuilder>());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBpm() {
        return bpm;
    }

    public MusicBuilder setBpm(int bpm) {
        this.bpm = bpm;
        return this;
    }

    public MusicBuilder addInstrument(InstrumentBuilder builder) {
        instruments.add(builder);
        return this;
    }

    public MusicBuilder addPattern(int channel, PatternBuilder builder) {
        channels.get(channel).add(builder);
        return this;
    }

    public Music build() {
        List<Instrument> ins = new ArrayList<Instrument>();
        for (InstrumentBuilder builder : this.instruments) {
            ins.add(builder.build());
        }

        Channel[] ch = new Channel[8];
        for (int i = 0; i < ch.length; i++) {
            List<PatternBuilder> list = channels.get(i);

            List<Pattern> patterns = new ArrayList<Pattern>();
            for (PatternBuilder builder : list) {
                Pattern pattern = builder.build();
                if (!ins.contains(pattern.getInstrument())) {
                    ins.add(pattern.getInstrument());
                }
                patterns.add(pattern);
            }

            if (patterns.size() != 0) {
                ch[i] = new Channel(patterns);
            }
        }
        Music music = new Music(name, bpm, ins, ch);
        return music;
    }
}
