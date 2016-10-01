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

public class InstrumentBuilder {

    String name;
    List<Command> list = new ArrayList<Command>();
    List<Instrument> effectsList = new ArrayList<Instrument>();

    private Instrument instrument;

    public InstrumentBuilder() {

    }

    public InstrumentBuilder(String name) {
        this.name = name;
    }

    public String getName() {
        return name == null ? toString() : name;
    }

    public InstrumentBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public InstrumentBuilder jump(int steps) {
        Command cmd = new Command(Command.JUMP);
        cmd.setValue(steps);
        list.add(cmd);
        return this;
    }

    public InstrumentBuilder setFrequency(double frequency) {
        Command cmd = new Command(Command.SET, Command.FREQUENCY);
        cmd.setFrequency(frequency);
        list.add(cmd);
        return this;
    }

    public InstrumentBuilder modifyFrequency(double frequency) {
        Command cmd = new Command(Command.MODIFY, Command.FREQUENCY);
        cmd.setFrequency(frequency);
        list.add(cmd);
        return this;
    }

    public InstrumentBuilder setNote(String note) {
        Command cmd = new Command(Command.SET, Command.FREQUENCY_NOTE);
        cmd.setFrequencyNote(note);
        list.add(cmd);
        return this;
    }

    public InstrumentBuilder modifyNote(String note) {
        Command cmd = new Command(Command.MODIFY, Command.FREQUENCY_NOTE);
        cmd.setFrequencyNote(note);
        list.add(cmd);
        return this;
    }

    public InstrumentBuilder setEnvelope(double length, double reset) {
        Command cmd = new Command(Command.SET, Command.ENVELOPE);
        cmd.setEnvelopeLength(length);
        cmd.setEnvelopeReset(reset);
        list.add(cmd);
        return this;
    }

    public InstrumentBuilder modifyEnvelope(double length, double reset) {
        Command cmd = new Command(Command.MODIFY, Command.ENVELOPE);
        cmd.setEnvelopeLength(length);
        cmd.setEnvelopeReset(reset);
        list.add(cmd);
        return this;
    }

    public InstrumentBuilder setVolume(double volume) {
        Command cmd = new Command(Command.SET, Command.VOLUME);
        cmd.setVolume(volume);
        list.add(cmd);
        return this;
    }

    public InstrumentBuilder modifyVolume(double volume) {
        Command cmd = new Command(Command.MODIFY, Command.VOLUME);
        cmd.setVolume(volume);
        list.add(cmd);
        return this;
    }

    public InstrumentBuilder setModulation(double frequency, double fixed) {
        Command cmd = new Command(Command.SET, Command.MODULATION);
        cmd.setModulation(frequency);
        cmd.setDutyCycle(fixed);
        list.add(cmd);
        return this;
    }

    public InstrumentBuilder modifyModulation(double frequency, double fixed) {
        Command cmd = new Command(Command.MODIFY, Command.MODULATION);
        cmd.setModulation(frequency);
        cmd.setDutyCycle(fixed);
        list.add(cmd);
        return this;
    }

    public InstrumentBuilder setRegister(int register, long value) {
        switch (register) {
            case Util.FREQUENCY: {
                Command cmd = new Command(Command.SET, Command.FREQUENCY_REGISTER);
                cmd.setFrequencyRegister(value);
                list.add(cmd);
                break;
            }
            case Util.ENVELOPE: {
                Command cmd = new Command(Command.SET, Command.ENVELOPE_REGISTER);
                cmd.setEnvelopeRegister(value);
                list.add(cmd);
                break;
            }
            case Util.VOLUME: {
                Command cmd = new Command(Command.SET, Command.VOLUME_REGISTER);
                cmd.setVolumeRegister(value);
                list.add(cmd);
                break;
            }
            case Util.MODULATION: {
                Command cmd = new Command(Command.SET, Command.MODULATION_REGISTER);
                cmd.setModulationRegister(value);
                list.add(cmd);
                break;
            }
        }
        return this;
    }

    public InstrumentBuilder modifyRegister(int register, long value) {
        switch (register) {
            case Util.FREQUENCY: {
                Command cmd = new Command(Command.MODIFY, Command.FREQUENCY_REGISTER);
                cmd.setFrequencyRegister(value);
                list.add(cmd);
                break;
            }
            case Util.ENVELOPE: {
                Command cmd = new Command(Command.MODIFY, Command.ENVELOPE_REGISTER);
                cmd.setEnvelopeRegister(value);
                list.add(cmd);
                break;
            }
            case Util.VOLUME: {
                Command cmd = new Command(Command.MODIFY, Command.VOLUME_REGISTER);
                cmd.setVolumeRegister(value);
                list.add(cmd);
                break;
            }
            case Util.MODULATION: {
                Command cmd = new Command(Command.MODIFY, Command.MODULATION_REGISTER);
                cmd.setModulationRegister(value);
                list.add(cmd);
                break;
            }
        }
        return this;
    }

    public InstrumentBuilder repeat(int repeat) {
        list.get(list.size() - 1).setRepeat(repeat);
        return this;
    }

    public InstrumentBuilder effect(Instrument effect) {
        assert (effectsList.size() < 6);
        effectsList.add(effect);
        return this;
    }

    public Instrument build() {
        if (instrument == null) {
            instrument = new Instrument(name, list);
        }
        return instrument;
    }

    @Override
    public String toString() {
        return name == null ? super.toString() : name;
    }
}
