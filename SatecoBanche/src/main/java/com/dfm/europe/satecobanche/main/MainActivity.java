package com.dfm.europe.satecobanche.main;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.dfm.europe.satecobanche.R;

import com.dfm.europe.satecobanche.databases.DataDetails;
import com.dfm.europe.satecobanche.databases.DatabaseHelper;
import com.dfm.europe.satecobanche.functions.SaveFileActivity;
import com.dfm.europe.satecobanche.functions.TimeConversion;
import com.dfm.europe.satecobanche.navigation.InformationActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.dfm.europe.satecobanche.functions.TimeConversion.dateToTimestamp;
import static com.dfm.europe.satecobanche.functions.TimeConversion.timestampToDate;

public class MainActivity extends AppCompatActivity implements OnChartValueSelectedListener {
    private static String H_DISTANCE = "dh";
    private static String V_DISTANCE = "dv";

    private static String H_ALIGNMENT = "ah";
    private static String V_ALIGNMENT = "av";

    private static String PRESSURE = "p";

    private static int X_RANGE = 100;

    private static final long TIMESTAMP_DELETE = 28800;

    public static final int PRESSURE_CHART_INVALIDATE = 1;
    public static final int PRESSURE_VALUE_CHANGE = 2;
    public static final int SCAN_FILE = 3;
    public static final int PRESSURE_CHART_CLEAR_DATA = 4;

    //private String urlServer = "http://192.168.1.1/sateco_server/test_server";

    TabHost mTabHost;

    private LineChart mPressureChart;
    private TextView mPressureValue;

    LineDataSet pressureDataset;
    ArrayList<Entry> pressureValue;

    private long mTime = 0;
    private float mPressureValueLatest = 0;
    private String mMessagePressure = "";

    protected Typeface mTfRegular;
    protected Typeface mTfLight;

    boolean paused = false;

    private Handler mThreadHandler;

    // Reference of DatabaseHelper class to access its DAOs and other components
    private DatabaseHelper databaseHelper = null;

    /*
     * Notifications from UsbService will be received here.
    */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case USBService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, getString(R.string.usb_ready), Toast.LENGTH_SHORT).show();
                    break;
                case USBService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, getString(R.string.usb_permission_not_granted), Toast.LENGTH_SHORT).show();
                    break;
                case USBService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, getString(R.string.no_usb_connected), Toast.LENGTH_SHORT).show();
                    break;
                case USBService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, getString(R.string.usb_disconnected), Toast.LENGTH_SHORT).show();
                    break;
                case USBService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, getString(R.string.usb_device_not_supported), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    //private final ConnectivityChangeReceiver mNetworkReceiver = new ConnectivityChangeReceiver();

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class USBHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public USBHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case USBService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    //Toast.makeText(mActivity.get(), String.format("Received: %s", data), Toast.LENGTH_SHORT).show();
                    mActivity.get().setData(data);
                    break;
                case USBService.CTS_CHANGE:
                    //Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case USBService.DSR_CHANGE:
                    //Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case USBService.SYNC_READ:
                    String buffer = (String) msg.obj;
                    //Toast.makeText(mActivity.get(), String.format("Received: %s", buffer), Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_main);

        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        configuration.setLocale(new Locale("fr"));
        resources.updateConfiguration(configuration, displayMetrics);

        mHandler = new USBHandler(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.mipmap.ic_launcher);

        mTabHost = (TabHost) findViewById(R.id.tabHost);
        mTabHost.setup();

        mTfRegular = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
        mTfLight = Typeface.createFromAsset(getAssets(), "OpenSans-Light.ttf");

        //Tab 1
        TabHost.TabSpec spec = mTabHost.newTabSpec("Pressure");
        spec.setContent(R.id.activity_pressure_line_chart);
        spec.setIndicator(getString(R.string.tabpressure));
        mTabHost.addTab(spec);

        //Tab 2
        spec = mTabHost.newTabSpec("Distance");
        spec.setContent(R.id.activity_distance_line_chart);
        spec.setIndicator(getString(R.string.tabdistance));
        mTabHost.addTab(spec);

        //Tab 3
        spec = mTabHost.newTabSpec("Angle");
        spec.setContent(R.id.activity_angle_line_chart);
        spec.setIndicator(getString(R.string.tabangle));
        mTabHost.addTab(spec);

        mTabHost.setCurrentTab(0);

        mThreadHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case PRESSURE_CHART_INVALIDATE:
                        mPressureChart.notifyDataSetChanged();
                        mPressureChart.invalidate();
                        break;

                    case PRESSURE_VALUE_CHANGE:
                        mPressureValue.setText((String) msg.obj);
                        break;

                    case SCAN_FILE:
                        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{(String) msg.obj}, null, null);

                        break;

                    case PRESSURE_CHART_CLEAR_DATA:
                        mPressureChart.clear();
                        pressureDataset.clear();
                        pressureValue.clear();
                        break;
                }
            }
        };

        setupCharts();

        //readData();

        //start thread
        ReadThread thread = new ReadThread();
        thread.start();

        final Handler handler = new Handler();
        Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!paused) {
                            mTime++;

                            //mPressureValueLatest = (float) (Math.random() * 30);
                            //mMessagePressure = "222864131214757024 123 p " + mPressureValueLatest;

                            mPressureValue.setText("" + mPressureValueLatest);
                            setData(mTime, mPressureValueLatest);

                            mPressureChart.invalidate();

                            if (!mMessagePressure.isEmpty()) {
                                final DataDetails dataDetails = new DataDetails();
                                // Then, set all the values from user input
                                dataDetails.addedDate = dateToTimestamp(new Date());
                                dataDetails.sensorId = Long.parseLong(mMessagePressure.split(" ")[0]);
                                dataDetails.key = PRESSURE;
                                dataDetails.value = mPressureValueLatest;

                                try {
                                    // This is how, a reference of DAO object can be done
                                    final Dao<DataDetails, Integer> dataDao = getHelper().getDataDao();

                                    //This is the way to insert data into a database table
                                    dataDao.create(dataDetails);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        };

        timer.schedule(timerTask, 5000, 5000);

        final Handler handlerClear = new Handler();
        Timer timerClear = new Timer(true);
        TimerTask timerTaskClear = new TimerTask() {
            @Override
            public void run() {
                handlerClear.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            long days = (new Date()).getTime() / 1000 - TIMESTAMP_DELETE;
                            
                            // Declaration of DAO to interact with corresponding table
                            Dao<DataDetails, Integer> dataDao = getHelper().getDataDao();

                            // Get our query builder from the DAO
                            final QueryBuilder<DataDetails, Integer> queryBuilder = dataDao.queryBuilder();

                            // We need only Banche which are associated with the selected Construction, so build the query by "Where" clause
                            queryBuilder.where().le(DataDetails.TIMESTAMP_FIELD, days);

                            // Prepare our SQL statement
                            final PreparedQuery<DataDetails> preparedQuery = queryBuilder.prepare();

                            // Fetch the list from Database by querying it
                            final Iterator<DataDetails> dataIt = dataDao.query(preparedQuery).iterator();

                            // Iterate through the DataDetails object iterator and populate the comma separated String
                            while (dataIt.hasNext()) {
                                final DataDetails data = dataIt.next();

                                //delete elements from table in field
                                dataDao.delete(data);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        timerClear.schedule(timerTaskClear, 300000, 300000);
    }

    private class ReadThread extends Thread implements Runnable {
        @Override
        public void run() {
            mPressureChart.clear();
            pressureDataset.clear();
            pressureValue.clear();

            setData(0, 0);
            mPressureChart.invalidate();

            try {
                // Declaration of DAO to interact with corresponding table
                Dao<DataDetails, Integer> dataDao = getHelper().getDataDao();

                // Get our query builder from the DAO
                final QueryBuilder<DataDetails, Integer> queryBuilder = dataDao.queryBuilder();

                long numRows = dataDao.countOf();

                queryBuilder.offset(numRows - (long)X_RANGE * 3).limit((long)X_RANGE * 3);
                queryBuilder.orderBy(DataDetails.ID_FIELD, true);  // true for ascending, false for descending

                // We need only Banche which are associated with the selected Construction, so build the query by "Where" clause
                queryBuilder.where().eq(DataDetails.KEY_FIELD, PRESSURE);

                // Prepare our SQL statement
                final PreparedQuery<DataDetails> preparedQuery = queryBuilder.prepare();

                // Fetch the list from Database by querying it
                final Iterator<DataDetails> dataIt = dataDao.query(preparedQuery).iterator();

                boolean isFirst = true;

                long days = (new Date()).getTime() / 1000 - TIMESTAMP_DELETE;

                Log.i("ReadData", "" + days);

                float value = 0;

                // Iterate through the DataDetails object iterator and populate the comma separated String
                while (dataIt.hasNext()) {
                    final DataDetails data = dataIt.next();
                    //if (data.key.equals(PRESSURE)) {
                        if (data.addedDate > days) {
                            setData(data.dataId, data.value);
                            mTime = data.dataId;

                            //mPressureChart.notifyDataSetChanged();
                            //mPressureChart.invalidate();
                            mThreadHandler.obtainMessage(PRESSURE_CHART_INVALIDATE);

                            value = data.value;
                        } else {
                            //delete elements from table in field
                            dataDao.delete(data);
                        }
                    //}
                }

                mThreadHandler.obtainMessage(PRESSURE_VALUE_CHANGE, "" + value);

                //today = new Date();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
                Intent intent = new Intent(MainActivity.this, InformationActivity.class);
                startActivity(intent);
                break;

            case R.id.toolbar_clear:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.title_clear_data))
                        .setMessage(getString(R.string.message_clear_data))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Dao<DataDetails, Integer> dataDao = getHelper().getDataDao();
                                    // continue with delete
                                    TableUtils.dropTable(dataDao, true);
                                    TableUtils.createTable(dataDao);

                                    mPressureValueLatest = 0;
                                    mMessagePressure = "";

                                    mPressureChart.clear();
                                    pressureDataset.clear();
                                    pressureValue.clear();

                                    mTime = 0;

                                    setData(0, 0);

                                    mPressureChart.notifyDataSetChanged();
                                    mPressureChart.invalidate();
                                } catch (SQLException e) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.unable_clear_database), Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                break;

            case R.id.toolbar_export:
                Intent i = new Intent(this, SaveFileActivity.class);
                this.startActivityForResult(i, 123);

                break;
        }

        return true;
    }

    private class ClearDatabaseThread extends Thread implements Runnable {
        @Override
        public void run() {
            try {
                Dao<DataDetails, Integer> dataDao = getHelper().getDataDao();
                // continue with delete
                TableUtils.dropTable(dataDao, true);
                TableUtils.createTable(dataDao);

                mPressureValueLatest = 0;
                mMessagePressure = "";

                mThreadHandler.obtainMessage(PRESSURE_CHART_CLEAR_DATA);

                mTime = 0;

                setData(0, 0);

                mThreadHandler.obtainMessage(PRESSURE_CHART_INVALIDATE);
            } catch (SQLException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.unable_clear_database), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 123:
                if (resultCode == RESULT_OK) {
                    String fileName = data.getStringExtra("filePath");
                    //String shortFileName = data.getStringExtra("shortFileName");

                    String temp = fileName;
                    temp = temp.toLowerCase();

                    if (!temp.endsWith(".csv")) {
                        fileName += ".csv";
                    }

                    if (pressureValue.size() > 0) {
                        SaveCSVThread thread = new SaveCSVThread(fileName);
                        thread.start();

                        Toast.makeText(getApplicationContext(), getString(R.string.export_data_successful), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.export_no_data), Toast.LENGTH_LONG).show();
                    }
                }

                break;
        }
    }

    private class SaveCSVThread extends Thread implements Runnable {
        String fileName;

        SaveCSVThread(String path) {
            this.fileName = path;
        }

        @Override
        public void run() {
            try {
                File file = new File(fileName);
                FileOutputStream outFile = new FileOutputStream(file);
                OutputStreamWriter out = new OutputStreamWriter(outFile);

                out.append(getString(R.string.export_data_time)  + "          " + getString(R.string.export_data_pressure) + "\n");

                for (Entry e : pressureValue) {
                    out.append(((int)(e.getX()) + "          " + e.getY() + "\n"));
                }

                out.flush();
                out.close();
                outFile.close();

                mThreadHandler.obtainMessage(SCAN_FILE, file.getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // This is how, DatabaseHelper can be initialized for future use
    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }

        return databaseHelper;
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
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(USBService.ACTION_USB_PERMISSION_GRANTED);
        usbFilter.addAction(USBService.ACTION_NO_USB);
        usbFilter.addAction(USBService.ACTION_USB_DISCONNECTED);
        usbFilter.addAction(USBService.ACTION_USB_NOT_SUPPORTED);
        usbFilter.addAction(USBService.ACTION_USB_PERMISSION_NOT_GRANTED);

        registerReceiver(mUsbReceiver, usbFilter);

        //final IntentFilter networkFilter = new IntentFilter();
        //networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        //registerReceiver(mNetworkReceiver, networkFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        configuration.setLocale(new Locale("fr"));
        resources.updateConfiguration(configuration, displayMetrics);

        setFilters();  // Start listening notifications from USBService
        startService(USBService.class, usbConnection, null); // Start USBService(if it was not started before) and Bind it

        //readData();

        mPressureChart.invalidate();

        paused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        //paused = true;

        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);

        //unregisterReceiver(mNetworkReceiver);
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
        mPressureChart.setBackgroundColor(Color.WHITE);

        // add data
        setData(0, 0);

        mPressureChart.animateX(2500);

        mPressureChart.setViewPortOffsets(60, 15, 15, 40);

        // get the legend (only possible after setting data)
        Legend legend = mPressureChart.getLegend();
        legend.setEnabled(false);

        // modify the legend ...
        //legend.setForm(Legend.LegendForm.LINE);
        //legend.setTypeface(mTfLight);
        //legend.setTextSize(14f);
        //legend.setTextColor(Color.WHITE);
        //legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        //legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        //legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        //legend.setDrawInside(false);

        XAxis xAxis = mPressureChart.getXAxis();
        xAxis.setTypeface(mTfLight);
        xAxis.setTextSize(15f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setLabelCount(12);
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(12f); // one minutes
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                long millis = (long)(value / 12);
                return "" + millis;
            }
        });

        YAxis leftAxis = mPressureChart.getAxisLeft();
        leftAxis.setTypeface(mTfLight);
        leftAxis.setTextSize(15f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mPressureChart.getAxisRight();
        rightAxis.setDrawZeroLine(false);

        mPressureChart.getAxisRight().setEnabled(false);
    }

    private void setData(long time, float value) {
        pressureValue.add(new Entry(time, value));

        if (mPressureChart.getData() != null && mPressureChart.getData().getDataSetCount() > 0) {
            pressureDataset = (LineDataSet) mPressureChart.getData().getDataSetByIndex(0);
            pressureDataset.setValues(pressureValue);
            mPressureChart.getData().notifyDataChanged();
            mPressureChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            pressureDataset = new LineDataSet(pressureValue, getString(R.string.legend_pressure));

            pressureDataset.setAxisDependency(YAxis.AxisDependency.LEFT);
            pressureDataset.setColor(Color.RED);
            pressureDataset.setLineWidth(3f);
            pressureDataset.setCircleRadius(1f);
            pressureDataset.setFillAlpha(65);
            pressureDataset.setHighLightColor(Color.rgb(244, 117, 117));
            pressureDataset.setDrawCircleHole(false);
            pressureDataset.setDrawValues(false);

            // create a data object with the datasets
            LineData data = new LineData(pressureDataset);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(12f);

            // set data
            mPressureChart.setData(data);
        }

        // this automatically refreshes the chart (calls invalidate())
        mPressureChart.setVisibleXRangeMaximum(X_RANGE);
        mPressureChart.moveViewToX((float)(time - X_RANGE));
    }

    private void setData(String value) {
        //Format of data
        //SensorID MessageID FieldKey value (ex: 123 102010 p 658)

        String[] values = value.split(" ");

        if (values.length >= 4) {
            if (values[2].equals(H_DISTANCE)) {

            } else if (values[2].equals(V_DISTANCE)) {

            } else if (values[2].equals(H_ALIGNMENT)) {

            } else if (values[2].equals(V_ALIGNMENT)) {

            } else if (values[2].equals(PRESSURE)) {
                mPressureValueLatest = Float.parseFloat(values[3]);
                mMessagePressure = value;

                //mPressureValueLatest = (float) (Math.random() * 30);
                //mMessagePressure = "1 123 p " + mPressureValueLatest;

                //SendHttpRequestTask task = new SendHttpRequestTask();

                //String[] params = new String[]{urlServer, value};
                //task.execute(params);
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
