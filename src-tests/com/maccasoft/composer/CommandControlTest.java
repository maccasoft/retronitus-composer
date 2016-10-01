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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.widgets.Shell;

import com.maccasoft.composer.CommandControl;
import com.maccasoft.composer.model.Command;

public class CommandControlTest extends DatabindingTestCase {

    Shell shell;
    DataBindingContext ctx;

    @Override
    protected void setUp() throws Exception {
        shell = createShell();
        ctx = new DataBindingContext();
    }

    @Override
    protected void tearDown() throws Exception {
        ctx.dispose();
        shell.dispose();
    }

    public void testBindFrequency() throws Exception {
        CommandControl control = new CommandControl(shell);

        Command cmd = new Command(Command.SET, Command.FREQUENCY);
        cmd.setFrequency(100);

        control.bind(ctx, cmd);

        assertEquals(1, control.action.getSelectionIndex());
        assertEquals(0, control.property.getSelectionIndex());
        assertEquals("100", control.frequency.getText());
    }

    public void testUpdateFrequencyFromWidget() throws Exception {
        CommandControl control = new CommandControl(shell);

        Command cmd = new Command(Command.SET, Command.FREQUENCY);
        cmd.setFrequency(100);

        control.bind(ctx, cmd);

        control.frequency.setText("200");
        assertEquals(200.0, cmd.getFrequency());
    }

    public void testUpdateNoteFrequencyFromWidget() throws Exception {
        CommandControl control = new CommandControl(shell);

        Command cmd = new Command(Command.SET, Command.FREQUENCY);
        cmd.setFrequency(100);

        control.bind(ctx, cmd);

        control.frequencyNote.setText("C-4");
        assertEquals(261.626, cmd.getFrequency(), 0.01);
    }

    public void testUpdateFrequencyFromModel() throws Exception {
        CommandControl control = new CommandControl(shell);

        Command cmd = new Command(Command.SET, Command.FREQUENCY);
        cmd.setFrequency(100);

        control.bind(ctx, cmd);

        cmd.setFrequency(200);
        assertEquals("200", control.frequency.getText());
    }
}
