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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxis.Position;
import org.swtchart.IAxisSet;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.Range;

public class InstrumentChart {

    Composite container;

    Chart chart;
    Spinner time;

    IAxis xAxis;
    IAxis yAxis1;
    IAxis yAxis2;
    ILineSeries lineSeries1;
    ILineSeries lineSeries2;
    ILineSeries lineSeries3;

    InstrumentChartCalculator instrument;
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

    public InstrumentChart(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = layout.marginHeight = 0;
        container.setLayout(layout);

        GC gc = new GC(container);
        fontMetrics = gc.getFontMetrics();
        gc.dispose();

        instrument = new InstrumentChartCalculator();

        createChart(container);
    }

    void createChart(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics, 300);
        gridData.heightHint = Dialog.convertVerticalDLUsToPixels(fontMetrics, 180);

        chart = new Chart(composite, SWT.NONE);
        chart.setLayoutData(gridData);
        chart.getTitle().setVisible(false);
        chart.getLegend().setPosition(SWT.TOP);

        createBottomControls(composite);

        IAxisSet axisSet = chart.getAxisSet();

        xAxis = axisSet.getXAxis(0);
        xAxis.getTitle().setVisible(false);

        yAxis1 = axisSet.getYAxis(0);
        yAxis1.getTitle().setVisible(false);
        yAxis1.setRange(new Range(0.0, 100.0));

        yAxis2 = axisSet.getYAxis(axisSet.createYAxis());
        yAxis2.getTitle().setVisible(false);
        yAxis2.setPosition(Position.Secondary);
        yAxis2.setRange(new Range(0.0, 5000.0));

        lineSeries1 = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "Volume");
        lineSeries1.setAntialias(SWT.ON);
        lineSeries1.setLineWidth(1);
        lineSeries1.setSymbolType(PlotSymbolType.NONE);
        lineSeries1.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_RED));
        lineSeries1.setYAxisId(0);

        lineSeries2 = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "Frequency");
        lineSeries2.setAntialias(SWT.ON);
        lineSeries2.setLineWidth(1);
        lineSeries2.setSymbolType(PlotSymbolType.NONE);
        lineSeries2.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
        lineSeries2.setYAxisId(1);

        lineSeries3 = (ILineSeries) chart.getSeriesSet().createSeries(SeriesType.LINE, "Duty (%)");
        lineSeries3.setAntialias(SWT.ON);
        lineSeries3.setLineWidth(1);
        lineSeries3.setSymbolType(PlotSymbolType.NONE);
        lineSeries3.setLineColor(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
        lineSeries3.setYAxisId(0);
    }

    void createBottomControls(Composite parent) {
        Composite group = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(4, false);
        layout.marginWidth = layout.marginHeight = 0;
        group.setLayout(layout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label label = new Label(group, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        label = new Label(group, SWT.NONE);
        label.setText("Duration");

        time = new Spinner(group, SWT.BORDER);
        time.setValues(100, 100, 100000, 0, 100, 100);
        time.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fontMetrics, 6), SWT.DEFAULT));
        time.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (instrument != null) {
                    startUpdateThread();
                }
            }
        });

        label = new Label(group, SWT.NONE);
        label.setText("ms.");
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

    public void setWaveTable(int[][] waveTable) {
        instrument.setWaveTable(waveTable);
        startUpdateThread();
    }

    void startUpdateThread() {
        final InstrumentChartCalculator instrument = this.instrument;
        final int time = this.time.getSelection();

        final Runnable chartUpdateRunnable = new Runnable() {

            @Override
            public void run() {
                instrument.setInitialValues(0, 0, 0, 0);
                instrument.play(time);

                if (chart.isDisposed()) {
                    return;
                }

                lineSeries1.setXSeries(instrument.getTime());
                lineSeries1.setYSeries(instrument.getVolume());

                lineSeries2.setXSeries(instrument.getTime());
                lineSeries2.setYSeries(instrument.getFrequency());

                lineSeries3.setXSeries(instrument.getTime());
                lineSeries3.setYSeries(instrument.getDuty());

                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (chart.isDisposed()) {
                            return;
                        }
                        xAxis.adjustRange();
                        chart.redraw();
                    }

                });
            }
        };
        new Thread(chartUpdateRunnable).start();
    }
}
