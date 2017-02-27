package com.dfm.honglv.satecobanche.main;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.dfm.honglv.satecobanche.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class BancheActivity extends ChartBase implements SeekBar.OnSeekBarChangeListener,
        OnChartValueSelectedListener {

    TabHost mTabHost;

    private LineChart mPressureChart;
    private TextView mPressureValue;

    private SeekBar mSeekBarX, mSeekBarY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banche);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mTabHost = (TabHost)findViewById(R.id.tabHost);
        mTabHost.setup();

        //Tab 1
        TabHost.TabSpec spec = mTabHost.newTabSpec("Distance");
        spec.setContent(R.id.activity_distance_line_chart);
        spec.setIndicator("Capteur de distance");
        mTabHost.addTab(spec);

        //Tab 2
        spec = mTabHost.newTabSpec("Angle");
        spec.setContent(R.id. activity_angle_line_chart);
        spec.setIndicator("Inclinomètre");
        mTabHost.addTab(spec);

        //Tab 3
        spec = mTabHost.newTabSpec("Pressure");
        spec.setContent(R.id.activity_pressure_line_chart);
        spec.setIndicator("Capteur de pression");
        mTabHost.addTab(spec);

        mTabHost.setCurrentTab(2);

        setupCharts();
    }

    private void setupCharts() {
        mPressureValue = (TextView) findViewById(R.id.pressurevalue);

        mSeekBarX = (SeekBar) findViewById(R.id.seekBar1);
        mSeekBarY = (SeekBar) findViewById(R.id.seekBar2);

        mSeekBarX.setProgress(45);
        mSeekBarY.setProgress(100);

        mSeekBarY.setOnSeekBarChangeListener(this);
        mSeekBarX.setOnSeekBarChangeListener(this);

        mPressureChart = (LineChart) findViewById(R.id.chartPressure);
        mPressureChart.setOnChartValueSelectedListener(this);

        // no description text
        mPressureChart.getDescription().setEnabled(false);

        // enable touch gestures
        mPressureChart.setTouchEnabled(true);

        mPressureChart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        mPressureChart.setDragEnabled(true);
        mPressureChart.setScaleEnabled(true);
        mPressureChart.setDrawGridBackground(false);
        mPressureChart.setHighlightPerDragEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mPressureChart.setPinchZoom(true);

        // set an alternative background color
        mPressureChart.setBackgroundColor(Color.LTGRAY);

        // add data
        setData(20, 30);

        mPressureChart.animateX(2500);

        // get the legend (only possible after setting data)
        Legend legend = mPressureChart.getLegend();

        // modify the legend ...
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTypeface(mTfLight);
        legend.setTextSize(11f);
        legend.setTextColor(Color.WHITE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        XAxis xAxis = mPressureChart.getXAxis();
        xAxis.setTypeface(mTfLight);
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);

        YAxis leftAxis = mPressureChart.getAxisLeft();
        leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);

        mPressureChart.getAxisRight().setEnabled(false);
    }

    private void setData(int count, float range) {

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            float mult = range / 2f;
            float val = (float) (Math.random() * mult) + 50;
            yVals1.add(new Entry(i, val));
        }

        LineDataSet pressureDataset;

        if (mPressureChart.getData() != null &&
                mPressureChart.getData().getDataSetCount() > 0) {
            pressureDataset = (LineDataSet) mPressureChart.getData().getDataSetByIndex(0);
            pressureDataset.setValues(yVals1);
            mPressureChart.getData().notifyDataChanged();
            mPressureChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            pressureDataset = new LineDataSet(yVals1, "Pression");

            pressureDataset.setAxisDependency(YAxis.AxisDependency.LEFT);
            pressureDataset.setColor(ColorTemplate.getHoloBlue());
            pressureDataset.setCircleColor(Color.WHITE);
            pressureDataset.setLineWidth(2f);
            pressureDataset.setCircleRadius(3f);
            pressureDataset.setFillAlpha(65);
            pressureDataset.setFillColor(ColorTemplate.getHoloBlue());
            pressureDataset.setHighLightColor(Color.rgb(244, 117, 117));
            pressureDataset.setDrawCircleHole(false);

            //set1.setFillFormatter(new MyFillFormatter(0f));
            //set1.setDrawHorizontalHighlightIndicator(false);
            //set1.setVisible(false);
            //set1.setCircleHoleColor(Color.WHITE);

            // create a data object with the datasets
            LineData data = new LineData(pressureDataset);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(9f);

            // set data
            mPressureChart.setData(data);
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());

        mPressureChart.centerViewToAnimated(e.getX(), e.getY(), mPressureChart.getData().getDataSetByIndex(h.getDataSetIndex())
                .getAxisDependency(), 500);
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        mPressureValue.setText("" + (mSeekBarY.getProgress()));

        setData(mSeekBarX.getProgress() + 1, mSeekBarY.getProgress());

        // redraw
        mPressureChart.invalidate();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
    }
}
