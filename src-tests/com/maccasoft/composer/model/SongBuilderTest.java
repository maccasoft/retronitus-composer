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

import com.maccasoft.composer.DatabindingTestCase;
import com.maccasoft.composer.model.Song;
import com.maccasoft.composer.model.SongBuilder;

public class SongBuilderTest extends DatabindingTestCase {

    public void testBuilder() throws Exception {
        Song song = new SongBuilder("Intro", 600) //
            .row().play(0, "C#4", "00", "", "").play(1, "C#3", "00", "", "") //
            .row().play(0, "B-4", "00", "", "").play(1, "B-3", "00", "", "") //
            .row().play(0, "G-4", "00", "", "").play(1, "G-3", "00", "", "") //
            .build();

        assertEquals("Intro", song.name);
        assertEquals(3, song.rows.size());
        assertEquals("C#4", song.rows.get(0).note[0]);
    }
}
