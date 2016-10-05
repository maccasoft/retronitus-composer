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

public class InvalidInstrumentException extends ProjectException {

    private static final long serialVersionUID = 6637876947199262781L;

    public InvalidInstrumentException(Project project, Song song, SongRow row, int channelIndex) {
        super(project, song, row, channelIndex);
    }

    @Override
    public String getMessage() {
        return String.format("Channel %s at row %02X contains an invalid instrument identificator",
            Project.channelLabels[channelIndex], song.getRows().indexOf(row));
    }
}
