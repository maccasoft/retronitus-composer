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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.maccasoft.composer.internal.HexStringToLongConverter;
import com.maccasoft.composer.internal.NumberToHexStringConverter;
import com.maccasoft.composer.internal.NumberToStringConverter;
import com.maccasoft.composer.internal.StringToDoubleConverter;
import com.maccasoft.composer.model.Command;

public class CommandControl {

    Composite container;

    Button disable;
    Spinner repeat;
    Combo action;

    Composite stackContainer;
    StackLayout stackLayout;

    Combo property;
    Composite propertiesStackContainer;
    StackLayout propertiesStackLayout;

    Text frequency;
    Text frequencyNote;
    Text frequencyRegister;
    Text duration;
    Text reset;
    Text envelopeRegister;
    Text volume;
    Text volumeRegister;
    Text variable;
    Text fixed;
    Text modulationRegister;
    Spinner steps;

    private FontMetrics fontMetrics;

    final FocusAdapter textFocusListener = new FocusAdapter() {

        @Override
        public void focusGained(FocusEvent e) {
            ((Text) e.widget).selectAll();
        }

        @Override
        public void focusLost(FocusEvent e) {
            ((Text) e.widget).clearSelection();
        }
    };

    public CommandControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(6, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);

        GC gc = new GC(container);
        fontMetrics = gc.getFontMetrics();
        gc.dispose();

        disable = new Button(container, SWT.CHECK);
        disable.setToolTipText("Check to disable entry");
        disable.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateEnablement();
            }
        });

        repeat = new Spinner(container, SWT.BORDER);
        repeat.setValues(0, 0, 99999, 0, 1, 1);
        repeat.setToolTipText("Repeat count (ms)");
        repeat.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 5), SWT.DEFAULT));

        Label label = new Label(container, SWT.NONE);
        label.setText("x");

        action = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        action.setItems(new String[] {
            "JUMP",
            "SET",
            "MODIFY"
        });
        action.setToolTipText("Command");
        action.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateVisibleStack();
            }
        });

        createStack(container);
    }

    void updateEnablement() {
        boolean enabled = !disable.getSelection();
        repeat.setEnabled(enabled);
        action.setEnabled(enabled);
        property.setEnabled(enabled);
        frequency.setEnabled(enabled);
        frequencyNote.setEnabled(enabled);
        frequencyRegister.setEnabled(enabled);
        duration.setEnabled(enabled);
        reset.setEnabled(enabled);
        envelopeRegister.setEnabled(enabled);
        volume.setEnabled(enabled);
        volumeRegister.setEnabled(enabled);
        variable.setEnabled(enabled);
        fixed.setEnabled(enabled);
        modulationRegister.setEnabled(enabled);
        steps.setEnabled(enabled);
    }

    void updateVisibleStack() {
        Control[] childs = stackContainer.getChildren();

        int selection = action.getSelectionIndex();
        if (selection >= childs.length) {
            selection = childs.length - 1;
        }

        stackLayout.topControl = childs[selection];
        stackContainer.layout();
    }

    void createStack(Composite parent) {
        stackContainer = new Composite(parent, SWT.NONE);
        stackLayout = new StackLayout();
        stackLayout.marginWidth = stackLayout.marginHeight = 0;
        stackContainer.setLayout(stackLayout);
        stackContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        createJumpControls(stackContainer);

        Composite container = new Composite(stackContainer, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);

        property = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        property.setToolTipText("Property");
        property.setItems(new String[] {
            "FREQUENCY",
            "FREQUENCY_NOTE",
            "ENVELOPE",
            "VOLUME",
            "MODULATION",
            "FREQUENCY_REGISTER",
            "ENVELOPE_REGISTER",
            "VOLUME_REGISTER",
            "MODULATION_REGISTER",
        });
        property.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateVisibleProperty();
            }
        });

        propertiesStackContainer = new Composite(container, SWT.NONE);
        propertiesStackLayout = new StackLayout();
        propertiesStackLayout.marginWidth = propertiesStackLayout.marginHeight = 0;
        propertiesStackContainer.setLayout(propertiesStackLayout);
        propertiesStackContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        createFrequencyControls(propertiesStackContainer);
        createFrequencyNoteControls(propertiesStackContainer);
        createEnvelopeControls(propertiesStackContainer);
        createVolumeControls(propertiesStackContainer);
        createModulationControls(propertiesStackContainer);

        createFrequencyRegisterControls(propertiesStackContainer);
        createEnvelopeRegisterControls(propertiesStackContainer);
        createVolumeRegisterControls(propertiesStackContainer);
        createModulationRegisterControls(propertiesStackContainer);
    }

    void updateVisibleProperty() {
        Control[] childs = propertiesStackContainer.getChildren();
        if (property.getSelectionIndex() != -1) {
            propertiesStackLayout.topControl = childs[property.getSelectionIndex()];
        }
        else {
            propertiesStackLayout.topControl = childs[childs.length - 1];
        }
        propertiesStackContainer.layout();
    }

    void createFrequencyControls(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);

        frequency = new Text(container, SWT.BORDER | SWT.CENTER);
        frequency.setToolTipText("Frequency");
        frequency.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 11), SWT.DEFAULT));
        frequency.addFocusListener(textFocusListener);

        Label label = new Label(container, SWT.NONE);
        label.setText("Hz");
    }

    void createFrequencyNoteControls(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);

        frequencyNote = new Text(container, SWT.BORDER | SWT.CENTER);
        frequencyNote.setToolTipText("Note");
        frequencyNote.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 11), SWT.DEFAULT));
        frequencyNote.addFocusListener(textFocusListener);
    }

    void createFrequencyRegisterControls(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);

        frequencyRegister = new Text(container, SWT.BORDER | SWT.CENTER);
        frequencyRegister.setToolTipText("Register value (hex)");
        frequencyRegister.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 11), SWT.DEFAULT));
        frequencyRegister.addFocusListener(textFocusListener);
    }

    void createEnvelopeControls(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(4, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);

        duration = new Text(container, SWT.BORDER | SWT.CENTER);
        duration.setToolTipText("Duration");
        duration.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 11), SWT.DEFAULT));
        duration.addFocusListener(textFocusListener);

        Label label = new Label(container, SWT.NONE);
        label.setText("ms.");
        label.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 3), SWT.DEFAULT));

        reset = new Text(container, SWT.BORDER | SWT.CENTER);
        reset.setToolTipText("Reset amplitude");
        reset.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 6), SWT.DEFAULT));
        reset.addFocusListener(textFocusListener);

        label = new Label(container, SWT.NONE);
        label.setText("%");
    }

    void createEnvelopeRegisterControls(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);

        envelopeRegister = new Text(container, SWT.BORDER | SWT.CENTER);
        envelopeRegister.setToolTipText("Register value (hex)");
        envelopeRegister.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 11), SWT.DEFAULT));
        envelopeRegister.addFocusListener(textFocusListener);
    }

    void createVolumeControls(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);

        volume = new Text(container, SWT.BORDER | SWT.CENTER);
        volume.setToolTipText("Max. volume level");
        volume.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 11), SWT.DEFAULT));
        volume.addFocusListener(textFocusListener);

        Label label = new Label(container, SWT.NONE);
        label.setText("%");
    }

    void createVolumeRegisterControls(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);

        volumeRegister = new Text(container, SWT.BORDER | SWT.CENTER);
        volumeRegister.setToolTipText("Register value (hex)");
        volumeRegister.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 11), SWT.DEFAULT));
        volumeRegister.addFocusListener(textFocusListener);
    }

    void createModulationControls(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(4, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);

        variable = new Text(container, SWT.BORDER | SWT.CENTER);
        variable.setToolTipText("Variable modulation");
        variable.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 11), SWT.DEFAULT));
        variable.addFocusListener(textFocusListener);

        Label label = new Label(container, SWT.NONE);
        label.setText("Hz");
        label.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 3), SWT.DEFAULT));

        fixed = new Text(container, SWT.BORDER | SWT.CENTER);
        fixed.setToolTipText("Fixed duty cycle");
        fixed.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 6), SWT.DEFAULT));
        fixed.addFocusListener(textFocusListener);

        label = new Label(container, SWT.NONE);
        label.setText("%");
    }

    void createModulationRegisterControls(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);

        modulationRegister = new Text(container, SWT.BORDER | SWT.CENTER);
        modulationRegister.setToolTipText("Register value (hex)");
        modulationRegister.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 11), SWT.DEFAULT));
        modulationRegister.addFocusListener(textFocusListener);
    }

    void createJumpControls(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);

        steps = new Spinner(container, SWT.BORDER);
        steps.setValues(0, -9999, 9999, 0, 1, 1);
        steps.setToolTipText("Number of instructions");
        steps.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 5), SWT.DEFAULT));
    }

    public void bind(DataBindingContext ctx, Command cmd) {
        List<Binding> bindings = new ArrayList<Binding>();

        bindings.add(ctx.bindValue(WidgetProperties.selection().observe(disable), BeanProperties.value(cmd.getClass(),
            Command.PROP_DISABLED).observe(cmd)));

        bindings.add(ctx.bindValue(WidgetProperties.selection().observe(repeat), BeanProperties.value(cmd.getClass(),
            Command.PROP_REPEAT).observe(cmd)));
        bindings.add(ctx.bindValue(WidgetProperties.singleSelectionIndex().observe(action), BeanProperties.value(cmd.getClass(),
            Command.PROP_ACTION).observe(cmd)));
        bindings.add(ctx.bindValue(WidgetProperties.singleSelectionIndex().observe(property), BeanProperties.value(cmd.getClass(),
            Command.PROP_PROPERTY).observe(cmd)));

        bindings.add(ctx.bindValue(WidgetProperties.selection().observe(steps), BeanProperties.value(cmd.getClass(),
            Command.PROP_VALUE).observe(cmd)));

        bindings.add(ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(frequency), BeanProperties.value(cmd.getClass(),
            Command.PROP_FREQUENCY).observe(cmd),
            new UpdateValueStrategy().setConverter(new StringToDoubleConverter()),
            new UpdateValueStrategy().setConverter(new NumberToStringConverter())));
        bindings.add(ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(frequencyNote), BeanProperties.value(cmd.getClass(),
            Command.PROP_FREQUENCY_NOTE).observe(cmd)));

        bindings.add(ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(duration), BeanProperties.value(cmd.getClass(),
            Command.PROP_ENVELOPE_LENGTH).observe(cmd),
            new UpdateValueStrategy().setConverter(new StringToDoubleConverter()),
            new UpdateValueStrategy().setConverter(new NumberToStringConverter())));
        bindings.add(ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(reset), BeanProperties.value(cmd.getClass(),
            Command.PROP_ENVELOPE_RESET).observe(cmd),
            new UpdateValueStrategy().setConverter(new StringToDoubleConverter()),
            new UpdateValueStrategy().setConverter(new NumberToStringConverter())));

        bindings.add(ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(volume), BeanProperties.value(cmd.getClass(),
            Command.PROP_VOLUME).observe(cmd),
            new UpdateValueStrategy().setConverter(new StringToDoubleConverter()),
            new UpdateValueStrategy().setConverter(new NumberToStringConverter())));

        bindings.add(ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(variable), BeanProperties.value(cmd.getClass(),
            Command.PROP_MODULATION).observe(cmd),
            new UpdateValueStrategy().setConverter(new StringToDoubleConverter()),
            new UpdateValueStrategy().setConverter(new NumberToStringConverter())));
        bindings.add(ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(fixed), BeanProperties.value(cmd.getClass(),
            Command.PROP_DUTY_CYCLE).observe(cmd),
            new UpdateValueStrategy().setConverter(new StringToDoubleConverter()),
            new UpdateValueStrategy().setConverter(new NumberToStringConverter())));

        bindings.add(ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(frequencyRegister), BeanProperties.value(
            cmd.getClass(),
            Command.PROP_FREQUENCY_REGISTER).observe(cmd),
            new UpdateValueStrategy().setConverter(new HexStringToLongConverter()),
            new UpdateValueStrategy().setConverter(new NumberToHexStringConverter())));
        bindings.add(ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(envelopeRegister), BeanProperties.value(cmd.getClass(),
            Command.PROP_ENVELOPE_REGISTER).observe(cmd),
            new UpdateValueStrategy().setConverter(new HexStringToLongConverter()),
            new UpdateValueStrategy().setConverter(new NumberToHexStringConverter())));
        bindings.add(ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(volumeRegister), BeanProperties.value(cmd.getClass(),
            Command.PROP_VOLUME_REGISTER).observe(cmd),
            new UpdateValueStrategy().setConverter(new HexStringToLongConverter()),
            new UpdateValueStrategy().setConverter(new NumberToHexStringConverter())));
        bindings.add(ctx.bindValue(WidgetProperties.text(SWT.Modify).observe(modulationRegister), BeanProperties.value(
            cmd.getClass(),
            Command.PROP_MODULATION_REGISTER).observe(cmd),
            new UpdateValueStrategy().setConverter(new HexStringToLongConverter()),
            new UpdateValueStrategy().setConverter(new NumberToHexStringConverter())));

        for (Iterator<Binding> it = bindings.iterator(); it.hasNext();) {
            it.next().updateModelToTarget();
        }

        updateVisibleStack();
        updateVisibleProperty();
        updateEnablement();
    }

    public Control getControl() {
        return container;
    }

    public void setLayoutData(Object layoutData) {
        container.setLayoutData(layoutData);
    }

    public Object getLayoutData() {
        return container.getLayoutData();
    }
}
