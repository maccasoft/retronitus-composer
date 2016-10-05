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

public class ProjectException extends Exception {

    private static final long serialVersionUID = 5567711847444642046L;

    final Project project;
    final Song song;
    final SongRow row;
    final int channelIndex;

    public ProjectException(Project project, Song song, SongRow row, int channelIndex) {
        this.project = project;
        this.song = song;
        this.row = row;
        this.channelIndex = channelIndex;
    }

    public Project getProject() {
        return project;
    }

    public Song getSong() {
        return song;
    }

    public SongRow getRow() {
        return row;
    }

    public int getChannelIndex() {
        return channelIndex;
    }

    public int getRowIndex() {
        return song.getRows().indexOf(row);
    }
}
