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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.NumberFormat;
import java.util.Arrays;

public class Command implements Cloneable {

    public static final String PROP_DISABLED = "disabled";
    public static final String PROP_REPEAT = "repeat";
    public static final String PROP_ACTION = "action";
    public static final String PROP_PROPERTY = "property";

    public static final String PROP_VALUE = "value";

    public static final String PROP_ENVELOPE_LENGTH = "envelopeLength";
    public static final String PROP_ENVELOPE_RESET = "envelopeReset";
    public static final String PROP_ENVELOPE_REGISTER = "envelopeRegister";

    public static final String PROP_FREQUENCY = "frequency";
    public static final String PROP_FREQUENCY_NOTE = "frequencyNote";
    public static final String PROP_FREQUENCY_REGISTER = "frequencyRegister";

    public static final String PROP_VOLUME = "volume";
    public static final String PROP_VOLUME_REGISTER = "volumeRegister";

    public static final String PROP_DUTY_CYCLE = "dutyCycle";
    public static final String PROP_MODULATION = "modulation";
    public static final String PROP_MODULATION_REGISTER = "modulationRegister";

    public static final int JUMP = 0;
    public static final int SET = 1;
    public static final int MODIFY = 2;

    public static final int FREQUENCY = 0;
    public static final int FREQUENCY_NOTE = 1;
    public static final int ENVELOPE = 2;
    public static final int VOLUME = 3;
    public static final int MODULATION = 4;
    public static final int FREQUENCY_REGISTER = 5;
    public static final int ENVELOPE_REGISTER = 6;
    public static final int VOLUME_REGISTER = 7;
    public static final int MODULATION_REGISTER = 8;

    static final NumberFormat nf = NumberFormat.getInstance();
    static {
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(2);
    }

    boolean disabled;

    int repeat;
    int action;
    int property;

    int value;

    double frequency;
    String frequencyNote = "";
    long frequencyRegister;

    double envelopeLength;
    double envelopeReset;
    long envelopeRegister;

    double volume;
    long volumeRegister;

    double dutyCycle;
    double modulation;
    long modulationRegister;

    int register;
    long[] registerValue = new long[4];

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public Command() {
    }

    public Command(int action) {
        this.action = action;
    }

    public Command(int action, int property) {
        this.action = action;
        this.property = property;
        updateRegisterFromProperty();
    }

    public Command(int repeat, int action, int property) {
        this.repeat = repeat;
        this.action = action;
        this.property = property;
        updateRegisterFromProperty();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(propertyName, l);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(propertyName, l);
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        changeSupport.firePropertyChange(PROP_DISABLED, this.disabled, this.disabled = disabled);
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        changeSupport.firePropertyChange(PROP_REPEAT, this.repeat, this.repeat = repeat);
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        changeSupport.firePropertyChange(PROP_ACTION, this.action, this.action = action);
    }

    public int getProperty() {
        return property;
    }

    public void setProperty(int property) {
        changeSupport.firePropertyChange(PROP_PROPERTY, this.property, this.property = property);
        updateRegisterFromProperty();
    }

    void updateRegisterFromProperty() {
        switch (property) {
            case FREQUENCY:
            case FREQUENCY_NOTE:
            case FREQUENCY_REGISTER:
                this.register = Util.FREQUENCY;
                break;
            case ENVELOPE:
            case ENVELOPE_REGISTER:
                this.register = Util.ENVELOPE;
                break;
            case VOLUME:
            case VOLUME_REGISTER:
                this.register = Util.VOLUME;
                break;
            case MODULATION:
            case MODULATION_REGISTER:
                this.register = Util.MODULATION;
                break;
        }
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        changeSupport.firePropertyChange(PROP_VALUE, this.value, this.value = value);
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        registerValue[Util.FREQUENCY] = Util.frequencyRegisterValue(frequency);
        changeSupport.firePropertyChange(PROP_FREQUENCY, this.frequency, this.frequency = frequency);
        changeSupport.firePropertyChange(PROP_FREQUENCY_NOTE, this.frequencyNote, this.frequencyNote = "");
        changeSupport.firePropertyChange(PROP_FREQUENCY_REGISTER, this.frequencyRegister, this.frequencyRegister =
            registerValue[Util.FREQUENCY]);
    }

    public String getFrequencyNote() {
        return frequencyNote;
    }

    public void setFrequencyNote(String note) {
        registerValue[Util.FREQUENCY] = Util.noteRegisterValue(note);
        changeSupport.firePropertyChange(PROP_FREQUENCY, this.frequency, this.frequency = Util.noteFrequency(note));
        changeSupport.firePropertyChange(PROP_FREQUENCY_NOTE, this.frequencyNote, this.frequencyNote = note);
        changeSupport.firePropertyChange(PROP_FREQUENCY_REGISTER, this.frequencyRegister, this.frequencyRegister =
            registerValue[Util.FREQUENCY]);
    }

    public long getFrequencyRegister() {
        return frequencyRegister & 0xFFFFFFFFL;
    }

    public void setFrequencyRegister(long frequencyRegister) {
        registerValue[Util.FREQUENCY] = frequencyRegister;
        changeSupport.firePropertyChange(PROP_FREQUENCY, this.frequency,
            this.frequency = Util.frequencyFromRegisterValue(frequencyRegister));
        changeSupport.firePropertyChange(PROP_FREQUENCY_NOTE, this.frequencyNote, this.frequencyNote = "");
        changeSupport.firePropertyChange(PROP_FREQUENCY_REGISTER, this.frequencyRegister,
            this.frequencyRegister = registerValue[Util.FREQUENCY]);
    }

    public double getEnvelopeLength() {
        return envelopeLength;
    }

    public void setEnvelopeLength(double envelopeLength) {
        registerValue[Util.ENVELOPE] = Util.envelopeRegisterValue(envelopeLength, envelopeReset);
        changeSupport.firePropertyChange(PROP_ENVELOPE_LENGTH, this.envelopeLength,
            this.envelopeLength = envelopeLength);
        changeSupport.firePropertyChange(PROP_ENVELOPE_REGISTER, this.envelopeRegister,
            this.envelopeRegister = registerValue[Util.ENVELOPE]);
    }

    public double getEnvelopeReset() {
        return envelopeReset;
    }

    public void setEnvelopeReset(double envelopeReset) {
        registerValue[Util.ENVELOPE] = Util.envelopeRegisterValue(envelopeLength, envelopeReset);
        changeSupport.firePropertyChange(PROP_ENVELOPE_RESET, this.envelopeReset,
            this.envelopeReset = envelopeReset);
        changeSupport.firePropertyChange(PROP_ENVELOPE_REGISTER, this.envelopeRegister,
            this.envelopeRegister = registerValue[Util.ENVELOPE]);
    }

    public long getEnvelopeRegister() {
        return envelopeRegister & 0xFFFFFFFFL;
    }

    public void setEnvelopeRegister(long envelopeRegister) {
        registerValue[Util.ENVELOPE] = envelopeRegister;
        changeSupport.firePropertyChange(PROP_ENVELOPE_LENGTH, this.envelopeLength,
            this.envelopeLength = Util.envelopeLengthFromRegisterValue(envelopeRegister));
        changeSupport.firePropertyChange(PROP_ENVELOPE_RESET, this.envelopeReset,
            this.envelopeReset = Util.envelopeResetFromRegisterValue(envelopeRegister));
        changeSupport.firePropertyChange(PROP_ENVELOPE_REGISTER, this.envelopeRegister,
            this.envelopeRegister = envelopeRegister);
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        registerValue[Util.VOLUME] = Util.volumeRegisterValue(volume);
        changeSupport.firePropertyChange(PROP_VOLUME, this.volume, this.volume = volume);
        changeSupport.firePropertyChange(PROP_VOLUME_REGISTER, this.volumeRegister, this.volumeRegister =
            registerValue[Util.VOLUME]);
    }

    public long getVolumeRegister() {
        return volumeRegister & 0xFFFFFFFFL;
    }

    public void setVolumeRegister(long volumeRegister) {
        registerValue[Util.VOLUME] = volumeRegister;
        changeSupport.firePropertyChange(PROP_VOLUME, this.volume, this.volume = Util.volumeFromRegisterValue(volumeRegister));
        changeSupport.firePropertyChange(PROP_VOLUME_REGISTER, this.volumeRegister, this.volumeRegister = volumeRegister);
    }

    public double getDutyCycle() {
        return dutyCycle;
    }

    public void setDutyCycle(double fixedModulation) {
        registerValue[Util.MODULATION] = Util.modulationRegisterValue(modulation, fixedModulation);
        changeSupport.firePropertyChange(PROP_DUTY_CYCLE, this.dutyCycle, this.dutyCycle = fixedModulation);
        changeSupport.firePropertyChange(PROP_MODULATION_REGISTER, this.modulationRegister, this.modulationRegister =
            registerValue[Util.MODULATION]);
    }

    public double getModulation() {
        return modulation;
    }

    public void setModulation(double frequencyModulation) {
        registerValue[Util.MODULATION] = Util.modulationRegisterValue(frequencyModulation, dutyCycle);
        changeSupport.firePropertyChange(PROP_MODULATION, this.modulation, this.modulation =
            frequencyModulation);
        changeSupport.firePropertyChange(PROP_MODULATION_REGISTER, this.modulationRegister, this.modulationRegister =
            registerValue[Util.MODULATION]);
    }

    public long getModulationRegister() {
        return modulationRegister;
    }

    public void setModulationRegister(long modulationRegister) {
        registerValue[Util.MODULATION] = modulationRegister;
        changeSupport.firePropertyChange(PROP_DUTY_CYCLE, this.dutyCycle,
            this.dutyCycle = (modulationRegister & 0x1FF) / 512.0 * 100.0);
        changeSupport.firePropertyChange(PROP_MODULATION, this.modulation,
            this.modulation = (modulationRegister & ~0x1FF) / Util.FREQUENCY_RATIO);
        changeSupport.firePropertyChange(PROP_MODULATION_REGISTER, this.modulationRegister,
            this.modulationRegister = registerValue[Util.MODULATION]);
    }

    public String toCString() {
        StringBuilder sb = new StringBuilder();

        if (action == JUMP) {
            sb.append("JUMP,");
            while (sb.length() < 24) {
                sb.append(" ");
            }
            sb.append(String.format("%d *STEPS", value));
            return sb.toString();
        }

        if (action == SET) {
            sb.append("SET|");
        }
        else if (action == MODIFY) {
            sb.append("MODIFY|");
        }

        switch (register) {
            case Util.FREQUENCY:
                sb.append("FREQUENCY");
                break;
            case Util.ENVELOPE:
                sb.append("ENVELOPE");
                break;
            case Util.VOLUME:
                sb.append("VOLUME");
                break;
            case Util.MODULATION:
                sb.append("MODULATION");
                break;
        }

        if (repeat != 0) {
            sb.append(String.format("|REP(%d)", repeat));
        }

        sb.append(", ");
        while (sb.length() < 24) {
            sb.append(" ");
        }

        switch (register) {
            case Util.FREQUENCY:
                sb.append(String.format("0x%08X", frequencyRegister & 0xFFFFFFFFL));
                break;
            case Util.ENVELOPE:
                sb.append(String.format("0x%08X|0x%03X", envelopeRegister & 0xFFFFFE00L, envelopeRegister & 0x1FFL));
                break;
            case Util.VOLUME:
                sb.append(String.format("0x%08X", volumeRegister & 0xFFFFFFFFL));
                break;
            case Util.MODULATION:
                sb.append(String.format("0x%08X|0x%03X", modulationRegister & 0xFFFFFE00L, modulationRegister & 0x1FFL));
                break;
        }

        return sb.toString();
    }

    public String toSpinString() {
        StringBuilder sb = new StringBuilder();
        sb.append("long    ");

        if (action == JUMP) {
            sb.append("JUMP, ");
            while (sb.length() < 40) {
                sb.append(" ");
            }
            sb.append(String.format("%d *STEPS", value));
            return sb.toString();
        }

        if (action == SET) {
            sb.append("SET|");
        }
        else if (action == MODIFY) {
            sb.append("MODIFY|");
        }

        switch (register) {
            case Util.FREQUENCY:
                sb.append("FREQUENCY");
                break;
            case Util.ENVELOPE:
                sb.append("ENVELOPE");
                break;
            case Util.VOLUME:
                sb.append("VOLUME");
                break;
            case Util.MODULATION:
                sb.append("MODULATION");
                break;
        }

        if (repeat != 0) {
            sb.append(String.format("|(%d *wait_ms)", repeat));
        }

        sb.append(", ");
        while (sb.length() < 40) {
            sb.append(" ");
        }

        switch (register) {
            case Util.FREQUENCY:
                sb.append(String.format("$%08X", frequencyRegister & 0xFFFFFFFFL));
                break;
            case Util.ENVELOPE:
                sb.append(String.format("$%08X|$%03X", envelopeRegister & 0xFFFFFE00L, envelopeRegister & 0x1FFL));
                break;
            case Util.VOLUME:
                sb.append(String.format("$%08X", volumeRegister & 0xFFFFFFFFL));
                break;
            case Util.MODULATION:
                sb.append(String.format("$%08X|$%03X", modulationRegister & 0xFFFFFE00L, modulationRegister & 0x1FFL));
                break;
        }

        return sb.toString();
    }

    public long getCommand() {
        if (action == JUMP) {
            return repeat << 4;
        }
        return (repeat << 4) | (action << 2) | register;
    }

    public long getArgument() {
        if (action == JUMP) {
            return (value << 3) & 0xFFFFFFFFL;
        }
        return registerValue[register];
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + action;
        result = prime * result + (disabled ? 1231 : 1237);
        long temp;
        temp = Double.doubleToLongBits(dutyCycle);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(envelopeLength);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (int) (envelopeRegister ^ (envelopeRegister >>> 32));
        temp = Double.doubleToLongBits(envelopeReset);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(frequency);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((frequencyNote == null) ? 0 : frequencyNote.hashCode());
        result = prime * result + (int) (frequencyRegister ^ (frequencyRegister >>> 32));
        temp = Double.doubleToLongBits(modulation);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (int) (modulationRegister ^ (modulationRegister >>> 32));
        result = prime * result + property;
        result = prime * result + register;
        result = prime * result + Arrays.hashCode(registerValue);
        result = prime * result + repeat;
        result = prime * result + value;
        temp = Double.doubleToLongBits(volume);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (int) (volumeRegister ^ (volumeRegister >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Command)) {
            return false;
        }
        Command other = (Command) obj;
        if (action != other.action) {
            return false;
        }
        if (disabled != other.disabled) {
            return false;
        }
        if (Double.doubleToLongBits(dutyCycle) != Double.doubleToLongBits(other.dutyCycle)) {
            return false;
        }
        if (Double.doubleToLongBits(envelopeLength) != Double.doubleToLongBits(other.envelopeLength)) {
            return false;
        }
        if (envelopeRegister != other.envelopeRegister) {
            return false;
        }
        if (Double.doubleToLongBits(envelopeReset) != Double.doubleToLongBits(other.envelopeReset)) {
            return false;
        }
        if (Double.doubleToLongBits(frequency) != Double.doubleToLongBits(other.frequency)) {
            return false;
        }
        if (frequencyNote == null) {
            if (other.frequencyNote != null) {
                return false;
            }
        }
        else if (!frequencyNote.equals(other.frequencyNote)) {
            return false;
        }
        if (frequencyRegister != other.frequencyRegister) {
            return false;
        }
        if (Double.doubleToLongBits(modulation) != Double.doubleToLongBits(other.modulation)) {
            return false;
        }
        if (modulationRegister != other.modulationRegister) {
            return false;
        }
        if (property != other.property) {
            return false;
        }
        if (register != other.register) {
            return false;
        }
        if (!Arrays.equals(registerValue, other.registerValue)) {
            return false;
        }
        if (repeat != other.repeat) {
            return false;
        }
        if (value != other.value) {
            return false;
        }
        if (Double.doubleToLongBits(volume) != Double.doubleToLongBits(other.volume)) {
            return false;
        }
        if (volumeRegister != other.volumeRegister) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (action == JUMP) {
            return String.format("JUMP(%d)", value);
        }

        StringBuilder sb = new StringBuilder();

        if (repeat != 0) {
            sb.append(String.format("REP(%d) | ", repeat));
        }

        if (action == SET) {
            sb.append("SET");
        }
        else if (action == MODIFY) {
            sb.append("SET");
        }

        switch (property) {
            case FREQUENCY:
                sb.append(String.format("_FREQUENCY(%s)", nf.format(frequency)));
                break;
            case FREQUENCY_NOTE:
                sb.append(String.format("_FREQUENCY_NOTE(\"%s\")", frequencyNote));
                break;
            case ENVELOPE:
                sb.append(String.format("_ENVELOPE(%s, %s)", nf.format(envelopeLength), nf.format(envelopeReset)));
                break;
            case VOLUME:
                sb.append(String.format("_VOLUME(%s)", nf.format(volume)));
                break;
            case MODULATION:
                sb.append(String.format("_MODULATION(%s, %s)", nf.format(modulation), nf.format(dutyCycle)));
                break;
            case FREQUENCY_REGISTER:
                sb.append(String.format("|FREQUENCY, 0x%08X", frequencyRegister & 0xFFFFFFFFL));
                break;
            case ENVELOPE_REGISTER:
                sb.append(String.format("|ENVELOPE, 0x%08X|0x%03X", envelopeRegister & 0xFFFFFE00L, envelopeRegister & 0x1FFL));
                break;
            case VOLUME_REGISTER:
                sb.append(String.format("|VOLUME, 0x%08X", volumeRegister & 0xFFFFFFFFL));
                break;
            case MODULATION_REGISTER:
                sb.append(String.format("|MODULATION, 0x%08X", modulationRegister & 0xFFFFFFFFL));
                break;
        }

        return sb.toString();
    }

    public String toXmlString() {
        if (action == JUMP) {
            return String.format("<jump pos=\"%d\" />", value);
        }

        StringBuilder sb = new StringBuilder();

        if (action == SET) {
            sb.append("<set-");
        }
        else if (action == MODIFY) {
            sb.append("<modify-");
        }

        switch (property) {
            case FREQUENCY:
                sb.append(String.format("frequency value=\"%s\"", nf.format(frequency)));
                break;
            case FREQUENCY_NOTE:
                sb.append(String.format("frequency note=\"%s\"", frequencyNote));
                break;
            case ENVELOPE:
                sb.append(String.format("envelope length=\"%s\" reset=\"%s\"", nf.format(envelopeLength), nf.format(
                    envelopeReset)));
                break;
            case VOLUME:
                sb.append(String.format("volume value=\"%s\"", nf.format(volume)));
                break;
            case MODULATION:
                sb.append(String.format("modulation frequency=\"%s\" fixed=\"%s\"", nf.format(modulation), nf.format(dutyCycle)));
                break;
            case FREQUENCY_REGISTER:
                sb.append(String.format("register frequency=\"%08X\"", frequencyRegister & 0xFFFFFFFFL));
                break;
            case ENVELOPE_REGISTER:
                sb.append(String.format("register envelope=\"%08X\"", envelopeRegister & 0xFFFFFFFFL));
                break;
            case VOLUME_REGISTER:
                sb.append(String.format("register volume=\"%08X\"", volumeRegister & 0xFFFFFFFFL));
                break;
            case MODULATION_REGISTER:
                sb.append(String.format("register modulation=\"%08X\"", modulationRegister & 0xFFFFFFFFL));
                break;
        }

        if (repeat != 0) {
            sb.append(String.format(" repeat=\"%d\"", repeat));
        }

        sb.append(" />");

        return sb.toString();
    }

    public static Command fromXml(String line) throws Exception {
        int s, e;
        Command cmd = new Command();

        if (line.contains("<jump ")) {
            cmd.setAction(JUMP);
            s = line.indexOf("pos=\"") + 5;
            e = line.indexOf('"', s);
            cmd.setValue(Integer.parseInt(line.substring(s, e)));
            return cmd;
        }
        else if (line.contains("<set-")) {
            cmd.setAction(SET);
        }
        else if (line.contains("<modify-")) {
            cmd.setAction(MODIFY);
        }
        else {
            throw new Exception("Invalid format: \"" + line + "\"");
        }

        if (line.contains("-register")) {
            s = line.indexOf("\"") + 1;
            e = line.indexOf('"', s);
            if (line.contains("frequency=")) {
                cmd.setProperty(FREQUENCY_REGISTER);
                cmd.setFrequencyRegister(Long.parseLong(line.substring(s, e), 16));
            }
            else if (line.contains("envelope=")) {
                cmd.setProperty(ENVELOPE_REGISTER);
                cmd.setEnvelopeRegister(Long.parseLong(line.substring(s, e), 16));
            }
            else if (line.contains("volume=")) {
                cmd.setProperty(VOLUME_REGISTER);
                cmd.setVolumeRegister(Long.parseLong(line.substring(s, e), 16));
            }
            else if (line.contains("modulation=")) {
                cmd.setProperty(MODULATION_REGISTER);
                cmd.setModulationRegister(Long.parseLong(line.substring(s, e), 16));
            }
            else {
                throw new Exception("Invalid register name: \"" + line + "\"");
            }
        }
        else if (line.contains("-frequency")) {
            if ((s = line.indexOf("value=\"")) != -1) {
                s += 7;
                e = line.indexOf('"', s);
                cmd.setProperty(FREQUENCY);
                cmd.setFrequency(nf.parse(line.substring(s, e)).doubleValue());
            }
            else if ((s = line.indexOf("note=\"")) != -1) {
                s += 6;
                e = line.indexOf('"', s);
                cmd.setProperty(FREQUENCY_NOTE);
                cmd.setFrequencyNote(line.substring(s, e));
            }
            else {
                throw new Exception("Invalid parameter: \"" + line + "\"");
            }
        }
        else if (line.contains("-envelope")) {
            cmd.setProperty(ENVELOPE);
            s = line.indexOf("length=\"") + 8;
            e = line.indexOf('"', s);
            cmd.setEnvelopeLength(nf.parse(line.substring(s, e)).doubleValue());
            s = line.indexOf("reset=\"") + 7;
            e = line.indexOf('"', s);
            cmd.setEnvelopeReset(nf.parse(line.substring(s, e)).doubleValue());
        }
        else if (line.contains("-volume")) {
            cmd.setProperty(VOLUME);
            s = line.indexOf("value=\"") + 7;
            e = line.indexOf('"', s);
            cmd.setVolume(nf.parse(line.substring(s, e)).doubleValue());
        }
        else if (line.contains("-modulation")) {
            cmd.setProperty(MODULATION);
            s = line.indexOf("frequency=\"") + 11;
            e = line.indexOf('"', s);
            cmd.setModulation(nf.parse(line.substring(s, e)).doubleValue());
            s = line.indexOf("fixed=\"") + 7;
            e = line.indexOf('"', s);
            cmd.setDutyCycle(nf.parse(line.substring(s, e)).doubleValue());
        }
        else {
            throw new Exception("Invalid property name: \"" + line + "\"");
        }

        if ((s = line.indexOf("repeat=\"")) != -1) {
            s += 8;
            e = line.indexOf('"', s);
            cmd.setRepeat(Integer.parseInt(line.substring(s, e)));
        }

        return cmd;
    }

    @Override
    public Command clone() throws CloneNotSupportedException {
        return (Command) super.clone();
    }
}
