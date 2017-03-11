package com.dfm.honglv.satecobanche.main;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.dfm.honglv.satecobanche.R;

import com.dfm.honglv.satecobanche.functions.ConnectivityChangeReceiver;
import com.dfm.honglv.satecobanche.navigation.InformationActivity;
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

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class FormWorkActivity extends AppCompatActivity implements OnChartValueSelectedListener {
    private static String H_DISTANCE = "dh";
    private static String V_DISTANCE = "dv";

    private static String H_ALIGNMENT = "ah";
    private static String V_ALIGNMENT = "av";

    private static String PRESSURE = "p";

    private String urlServer = "http://10.0.2.2:8080/TestAndroid/TestServlet";

    TabHost mTabHost;

    private LineChart mPressureChart;
    private TextView mPressureValue;

    LineDataSet pressureDataset;
    ArrayList<Entry> pressureValue;

    private int mTime = 0;

    protected Typeface mTfRegular;
    protected Typeface mTfLight;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case USBService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case USBService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case USBService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case USBService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case USBService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private final ConnectivityChangeReceiver mNetworkReceiver = new ConnectivityChangeReceiver();

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class USBHandler extends Handler {
        private final WeakReference<FormWorkActivity> mActivity;

        public USBHandler(FormWorkActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case USBService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    Log.d("", String.format("R: %s", data));
                    Toast.makeText(mActivity.get(), String.format("R: %s", data), Toast.LENGTH_SHORT).show();
                    //mActivity.get().display.append(data);
                    mActivity.get().setData(data);
                    break;
                case USBService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case USBService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case USBService.SYNC_READ:
                    String buffer = (String) msg.obj;
                    Log.d("", String.format("RS: %s", buffer));
                    Toast.makeText(mActivity.get(), String.format("RS: %s", buffer), Toast.LENGTH_SHORT).show();
                    //mActivity.get().display.append(buffer);
                    mActivity.get().setData(buffer);
                    break;
            }
        }
    }

    private USBService usbService;

    private USBHandler mHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((USBService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formwork);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.mipmap.ic_launcher);

        mTabHost = (TabHost) findViewById(R.id.tabHost);
        mTabHost.setup();

        mHandler = new USBHandler(this);

        mTfRegular = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
        mTfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");

        //Tab 1
        TabHost.TabSpec spec = mTabHost.newTabSpec("Distance");
        spec.setContent(R.id.activity_distance_line_chart);
        spec.setIndicator("Capteur de distance");
        mTabHost.addTab(spec);

        //Tab 2
        spec = mTabHost.newTabSpec("Angle");
        spec.setContent(R.id.activity_angle_line_chart);
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
        timer.schedule(timerTask, 10000, 10000);
        ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_info:
                Intent intent = new Intent(FormWorkActivity.this, InformationActivity.class);
                startActivity(intent);
                break;
        }

        return true;
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!USBService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }

            startService(startService);
        }

        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(USBService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(USBService.ACTION_NO_USB);
        filter.addAction(USBService.ACTION_USB_DISCONNECTED);
        filter.addAction(USBService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(USBService.ACTION_USB_PERMISSION_NOT_GRANTED);

        registerReceiver(mUsbReceiver, filter);

        final IntentFilter networkFilter = new IntentFilter();
        networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        registerReceiver(mNetworkReceiver, networkFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setFilters();  // Start listening notifications from USBService
        startService(USBService.class, usbConnection, null); // Start USBService(if it was not started before) and Bind it
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);

        unregisterReceiver(mNetworkReceiver);
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

    private void setData(String value) {
        mTime++;

        //Format of data
        //SensorID MessageID FieldKey value (ex: 123 102010 p 658)

        float fValue = 0;

        String[] values = value.split(" ");

        if (values.length >= 4) {
            if (values[values.length - 2] == H_DISTANCE) {

            } else if (values[values.length - 2] == V_DISTANCE) {

            } else if (values[values.length - 2] == H_ALIGNMENT) {

            } else if (values[values.length - 2] == V_ALIGNMENT) {

            } else if (values[values.length - 2] == PRESSURE) {
                mPressureValue.setText("" + fValue);
                setData(mTime, fValue);

                mPressureChart.invalidate();

                SendHttpRequestTask task = new SendHttpRequestTask();

                String[] params = new String[]{urlServer, value};
                task.execute(params);
            }
        }
    }

    private class SendHttpRequestTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String name = params[1];

            String data = sendHttpRequest(url, name);
            System.out.println("Data [" + data + "]");

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            //edtResp.setText(result);
            //item.setActionView(null);
        }
    }

    private String sendHttpRequest(String url, String name) {
        StringBuffer buffer = new StringBuffer();

        try {
            System.out.println("URL [" + url + "] - Name [" + name + "]");

            HttpURLConnection con = (HttpURLConnection) (new URL(url)).openConnection();
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.connect();
            con.getOutputStream().write(("name=" + name).getBytes());

            InputStream is = con.getInputStream();
            byte[] b = new byte[1024];

            while (is.read(b) != -1) {
                buffer.append(new String(b));
            }

            con.disconnect();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return buffer.toString();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());

        mPressureChart.centerViewToAnimated(e.getX(), e.getY(),
                mPressureChart.getData().getDataSetByIndex(h.getDataSetIndex()).getAxisDependency(),
                500);
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }
}
