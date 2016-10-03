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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Instrument {

    public static final String PROP_NAME = "name";
    public static final String PROP_COMMANDS = "commands";

    String name;
    List<Command> list = new ArrayList<Command>();
    List<Instrument> effects = new ArrayList<Instrument>();

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    public Instrument() {
    }

    public Instrument(String name) {
        this.name = name;
    }

    public Instrument(String name, List<Command> list) {
        this.name = name;
        this.list = list;
    }

    public Instrument(String name, List<Command> list, List<Instrument> effects) {
        assert (effects.size() <= 6);
        this.name = name;
        this.list = list;
        this.effects = effects;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        changeSupport.firePropertyChange(PROP_NAME, this.name, this.name = name);
    }

    public List<Command> getCommands() {
        return Collections.unmodifiableList(list);
    }

    public void setCommands(List<Command> list) {
        changeSupport.firePropertyChange(PROP_COMMANDS, this.list, this.list = list);
    }

    public List<Instrument> getEffects() {
        return effects;
    }

    public void setEffects(List<Instrument> effects) {
        this.effects = effects;
    }

    public void add(Command cmd) {
        list.add(cmd);
    }

    public boolean equalsTo(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Instrument)) {
            return false;
        }
        Instrument other = (Instrument) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        if (list == null) {
            if (other.list != null) {
                return false;
            }
        }
        else if (!list.equals(other.list)) {
            return false;
        }
        if (effects == null) {
            if (other.effects != null) {
                return false;
            }
        }
        else if (!effects.equals(other.effects)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }

    public String toCString() {
        StringBuilder sb = new StringBuilder();
        for (Command cmd : list) {
            sb.append(cmd.toCString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public String toXmlString() {
        StringBuilder sb = new StringBuilder();
        for (Command cmd : list) {
            sb.append(cmd.toXmlString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public long[] toArray() {
        long[] result = new long[list.size() * 2];

        int i = 0;
        for (Command cmd : list) {
            result[i++] = cmd.getCommand();
            result[i++] = cmd.getArgument();
        }

        return result;
    }

    public int getBinarySize() {
        return list.size() * 8;
    }
}
