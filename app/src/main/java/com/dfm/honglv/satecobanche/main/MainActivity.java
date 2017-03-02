package com.dfm.honglv.satecobanche.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.dfm.honglv.satecobanche.R;
import com.dfm.honglv.satecobanche.data.ConstructionDetails;
import com.dfm.honglv.satecobanche.data.DatabaseHelper;
import com.dfm.honglv.satecobanche.navigation.ConstructionAddActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        View.OnClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private double longitude;
    private double latitude;

    private FloatingActionButton fabAdd;

    private static final int MESSAGE_REFRESH = 101;
    private static final int MESSAGE_SEND = 102;
    private static final int MESSAGE_RECEIVED = 103;

    private static final long REFRESH_TIMEOUT_MILLIS = 10000;
    private static final long REFRESH_DATA_TIMEOUT_MILLIS = 25000;

    private final Handler mDeviceHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH:
                    onScanDeviceList();
                    mDeviceHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

    };

    private final Handler mDataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SEND:
                    onSendData();
                    mDataHandler.sendEmptyMessageDelayed(MESSAGE_SEND, REFRESH_DATA_TIMEOUT_MILLIS);
                    break;
                case MESSAGE_RECEIVED:
                    onReceivedData();
                    mDataHandler.sendEmptyMessageDelayed(MESSAGE_RECEIVED, REFRESH_DATA_TIMEOUT_MILLIS);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

    };

    // Reference of DatabaseHelper class to access its DAOs and other components
    private DatabaseHelper databaseHelper = null;

    // Declaration of DAO to interact with corresponding table
    private Dao<ConstructionDetails, Integer> constructionDao;

    // It holds the list of ConstructionDetails object fetched from Database
    private List<ConstructionDetails> constructionList;

    private List<UsbSerialPort> mEntries = new ArrayList<UsbSerialPort>();

    private UsbManager mUsbManager;

    private static UsbSerialPort mPort = null;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private SerialInputOutputManager mSerialIoManager;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d("SATECO", "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {

                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabAdd = (FloatingActionButton) findViewById(R.id.fab);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fabAdd.setOnClickListener(clickListener);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
    }

    private void onScanDeviceList() {
        new AsyncTask<Void, Void, List<UsbSerialPort>>() {
            @Override
            protected List<UsbSerialPort> doInBackground(Void... params) {
                SystemClock.sleep(1000);

                final List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);

                final List<UsbSerialPort> result = new ArrayList<UsbSerialPort>();
                for (final UsbSerialDriver driver : drivers) {
                    final List<UsbSerialPort> ports = driver.getPorts();
                    result.addAll(ports);
                }

                return result;
            }

            @Override
            protected void onPostExecute(List<UsbSerialPort> result) {
                mEntries.clear();
                mEntries.addAll(result);

                if (mEntries.size() == 0) {
                    Toast.makeText(MainActivity.this, "RF device not found!!!", Toast.LENGTH_SHORT).show();
                } else {
                    if (mPort != mEntries.get(0)) {
                        mPort = mEntries.get(0);
                    }

                    final UsbSerialDriver driver = mPort.getDriver();
                    final UsbDevice device = driver.getDevice();

                    final String title = String.format("Vendor %s Product %s",
                            HexDump.toHexString((short) device.getVendorId()),
                            HexDump.toHexString((short) device.getProductId()));

                    final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

                    UsbDeviceConnection connection = usbManager.openDevice(mPort.getDriver().getDevice());

                    if (connection == null) {
                        Log.d("SATECO", "Opening device failed");
                        return;
                    }

                    try {
                        mPort.open(connection);
                        mPort.setParameters(UsbSerialPort.BAUDRATE_9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                    } catch (IOException e) {
                        Log.e("SATECO", "Error setting up device: " + e.getMessage(), e);
                        try {
                            mPort.close();
                        } catch (IOException e2) {
                            // Ignore.
                        }
                        mPort = null;
                        return;
                    }

                    Toast.makeText(MainActivity.this, String.format("%s device(s) found %s",Integer.valueOf(mEntries.size()), title), Toast.LENGTH_SHORT).show();
                }
            }
        }.execute((Void) null);
    }

    private void onSendData() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                SystemClock.sleep(1000);
                Log.d("onSendData", "doInBackground onSendData");

                String text = "Hello...";

                return text;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    Log.d("onSendData", "onPostExecute onSendData");
                    if (mPort != null) {
                        //final UsbSerialPort sPort = mEntries.get(0);
                        byte[] bytes = result.getBytes("UTF-8");
                        mPort.write(bytes, 1000);

                        Toast.makeText(MainActivity.this, String.format("Send data: %s", result), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.execute((Void) null);
    }

    private void onReceivedData() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                SystemClock.sleep(1000);

                String strData = "";


                return strData;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    if (mPort != null) {
                        //final UsbSerialPort sPort = mEntries.get(0);
                        byte data[] = new byte[1000];
                        mPort.read(data, 1000);

                        result = new String(data);

                        Toast.makeText(MainActivity.this, String.format("Received data: %s", result), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.execute((Void) null);
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i("SATECO", "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (mPort != null) {
            Log.i("SATECO", "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(mPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(MainActivity.this, v);

            popup.getMenuInflater().inflate(R.menu.add_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    Toast.makeText(MainActivity.this,
                            "Clicked popup menu item " + item.getTitle(),
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            popup.show();
        }
    };

    // This is how, DatabaseHelper can be initialized for future use
    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        boolean isMarked = false;

        try {
            // This is how, a reference of DAO object can be done
            constructionDao = getHelper().getConstructionDao();

            // Query the database. We need all the records so, used queryForAll()
            constructionList = constructionDao.queryForAll();

            // Iterate through the BancheDetails object iterator and populate the comma separated String
            for (ConstructionDetails construction : constructionList) {
                LatLng latLg = new LatLng(construction.latitude, construction.longitude);
                mMap.addMarker(new MarkerOptions().position(latLg).title(construction.constructionName));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLg));

                isMarked = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (!isMarked) {
            // Add a marker in Sydney and move the camera
            LatLng paris = new LatLng(48.857708, 2.348928);

            mMap.addMarker(new MarkerOptions().position(paris).title("Paris"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(paris));
        }

        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_scan: {
                Toast.makeText(getApplicationContext(), "Scan contructions!",
                        Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.nav_add_contruction: {
                Toast.makeText(getApplicationContext(), "Add new contruction!",
                        Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.nav_add_banche: {
                Toast.makeText(getApplicationContext(), "Add new banche!",
                        Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.nav_infos: {
                Toast.makeText(getApplicationContext(), "Informations!",
                        Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.nav_settings: {
                Toast.makeText(getApplicationContext(), "Settings!",
                        Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.nav_share: {
                Toast.makeText(getApplicationContext(), "Share!",
                        Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.nav_send: {
                Toast.makeText(getApplicationContext(), "Sends!",
                        Toast.LENGTH_SHORT).show();
                break;
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    //Getting current location
    private void getCurrentLocation() {
        mMap.clear();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location != null) {
            //Getting longitude and latitude
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            //moving the map to location
            moveMap();
        }
    }

    private void moveMap() {
        /**
         * Creating the latlng object to store lat, long coordinates
         * adding marker to map
         * move the camera with animation
         */
        LatLng latLng = new LatLng(latitude, longitude);

        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("New construction!"));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mDeviceHandler.sendEmptyMessage(MESSAGE_REFRESH);
        mDataHandler.sendEmptyMessage(MESSAGE_SEND);
        mDataHandler.sendEmptyMessage(MESSAGE_RECEIVED);

        Log.d("SATECO", "Resumed, port=" + mPort);

        if (mPort == null) {
            Log.d("SATECO", "No serial device.");
        } else {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            UsbDeviceConnection connection = usbManager.openDevice(mPort.getDriver().getDevice());
            if (connection == null) {
                Log.d("SATECO", "Opening device failed");
                return;
            }

            try {
                mPort.open(connection);
                mPort.setParameters(UsbSerialPort.BAUDRATE_9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (IOException e) {
                Log.e("SATECO", "Error setting up device: " + e.getMessage(), e);
                try {
                    mPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                mPort = null;
                return;
            }
        }

        onDeviceStateChange();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mDeviceHandler.removeMessages(MESSAGE_REFRESH);
        mDataHandler.removeMessages(MESSAGE_SEND);
        mDataHandler.removeMessages(MESSAGE_RECEIVED);

        stopIoManager();
        if (mPort != null) {
            try {
                mPort.close();
            } catch (IOException e) {
                // Ignore.
            }
            mPort = null;
        }
        finish();
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(getApplicationContext(), "onViewClick", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));

        Intent intent = new Intent(MainActivity.this, ConstructionAddActivity.class);
        startActivity(intent);
        intent.putExtra("latitude", latLng.latitude);
        intent.putExtra("longitude", latLng.longitude);
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Toast.makeText(getApplicationContext(), "onMarkerDragStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        Toast.makeText(getApplicationContext(), "onMarkerDrag", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        // getting the Co-ordinates
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;

        //move to current position
        moveMap();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Intent intent = new Intent("com.dfm.honglv.satecobanche.BancheActivity");
        startActivity(intent);

        return true;
    }
}
