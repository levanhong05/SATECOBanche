package com.dfm.honglv.satecobanche.main;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.os.Handler;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TextView;

import com.dfm.honglv.satecobanche.R;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class BancheActivity extends ChartBase implements OnChartValueSelectedListener {

    TabHost mTabHost;

    private LineChart mPressureChart;
    private TextView mPressureValue;

    LineDataSet pressureDataset;
    ArrayList<Entry> pressureValue;

    private int mTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banche);

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

        final Handler handler = new Handler();
        Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTime++;

                        float value = (float) (Math.random() * 1000) + 50;
                        mPressureValue.setText("" + (value));
                        setData(mTime, value);

                        mPressureChart.invalidate();
                    }
                });
            }
        };
        timer.schedule(timerTask, 30000, 30000);;
    }

    private void setupCharts() {
        pressureValue = new ArrayList<Entry>();

        mPressureValue = (TextView) findViewById(R.id.pressurevalue);

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
        setData(0, 0);

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

    private void setData(int time, float value) {
        pressureValue.add(new Entry(time, value));

        if (mPressureChart.getData() != null && mPressureChart.getData().getDataSetCount() > 0) {
            pressureDataset = (LineDataSet) mPressureChart.getData().getDataSetByIndex(0);
            pressureDataset.setValues(pressureValue);
            mPressureChart.getData().notifyDataChanged();
            mPressureChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            pressureDataset = new LineDataSet(pressureValue, "Pression");

            pressureDataset.setAxisDependency(YAxis.AxisDependency.LEFT);
            pressureDataset.setColor(ColorTemplate.getHoloBlue());
            pressureDataset.setCircleColor(Color.WHITE);
            pressureDataset.setLineWidth(2f);
            pressureDataset.setCircleRadius(3f);
            pressureDataset.setFillAlpha(65);
            pressureDataset.setFillColor(ColorTemplate.getHoloBlue());
            pressureDataset.setHighLightColor(Color.rgb(244, 117, 117));
            pressureDataset.setDrawCircleHole(false);

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
}
