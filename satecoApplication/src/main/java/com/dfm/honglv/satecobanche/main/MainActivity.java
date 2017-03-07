package com.dfm.honglv.satecobanche.main;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import com.dfm.honglv.satecobanche.databases.ConstructionDetails;
import com.dfm.honglv.satecobanche.databases.DatabaseHelper;
import com.dfm.honglv.satecobanche.functions.AddChipActivity;
import com.dfm.honglv.satecobanche.functions.AddConstructionActivity;
import com.dfm.honglv.satecobanche.functions.AddFormworkActivity;
import com.dfm.honglv.satecobanche.navigation.InformationActivity;
import com.dfm.honglv.satecobanche.navigation.SettingsActivity;
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
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        View.OnClickListener {

    /*
     * Notifications from UsbService will be received here.
    */
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

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private double longitude;
    private double latitude;

    private FloatingActionButton fabAdd;

    // Reference of DatabaseHelper class to access its DAOs and other components
    private DatabaseHelper databaseHelper = null;

    // Declaration of DAO to interact with corresponding table
    private Dao<ConstructionDetails, Integer> constructionDao;

    // It holds the list of ConstructionDetails object fetched from Database
    private List<ConstructionDetails> constructionList;

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

    private static final int MESSAGE_SEND_DATA = 101;
    private static final long REFRESH_TIMEOUT_MILLIS = 5000;

    private final Handler mDataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SEND_DATA:
                    onSendData();
                    mDataHandler.sendEmptyMessageDelayed(MESSAGE_SEND_DATA, REFRESH_TIMEOUT_MILLIS);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

    };

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
                    Log.d("", String.format("R: %s", data));
                    Toast.makeText(mActivity.get(), String.format("R: %s", data), Toast.LENGTH_SHORT).show();
                    //mActivity.get().display.append(data);
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
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new USBHandler(this);

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
    }

    private void onSendData() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                SystemClock.sleep(1000);

                return "";
            }

            @Override
            protected void onPostExecute(String result) {
                if (usbService != null) { // if UsbService was correctly binded, Send data
                    String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
                    result = date + ": Hi arduino\n";

                    usbService.write(result.getBytes());

                    Toast.makeText(MainActivity.this, String.format("S: %s", result), Toast.LENGTH_SHORT).show();
                }
            }
        }.execute((Void) null);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(MainActivity.this, v);

            popup.getMenuInflater().inflate(R.menu.add_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {

                    int id = item.getItemId();

                    switch (id) {
                        case R.id.add_construction: {
                            Intent intent = new Intent(MainActivity.this, AddConstructionActivity.class);

                            //intent.putExtra("latitude", latLng.latitude);
                            //intent.putExtra("longitude", latLng.longitude);

                            startActivity(intent);

                            break;
                        }

                        case R.id.add_formwork: {
                            Intent intent = new Intent(MainActivity.this, AddFormworkActivity.class);

                            startActivity(intent);

                            break;
                        }

                        case R.id.add_chip: {
                            Intent intent = new Intent(MainActivity.this, AddChipActivity.class);

                            startActivity(intent);

                            break;
                        }
                    }

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

        //boolean isMarked = false;

        try {
            // This is how, a reference of DAO object can be done
            constructionDao = getHelper().getConstructionDao();

            // Query the database. We need all the records so, used queryForAll()
            constructionList = constructionDao.queryForAll();

            // Iterate through the FormworkDetails object iterator and populate the comma separated String
            for (ConstructionDetails construction : constructionList) {
                LatLng latLg = new LatLng(construction.latitude, construction.longitude);
                mMap.addMarker(new MarkerOptions().position(latLg).title(construction.constructionName));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLg));

                //isMarked = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

//        if (!isMarked) {
//            // Add a marker in Sydney and move the camera
//            LatLng paris = new LatLng(48.857708, 2.348928);
//
//            mMap.addMarker(new MarkerOptions().position(paris).title("Paris"));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(paris));
//        }

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
            case R.id.nav_sync: {
                Toast.makeText(getApplicationContext(), "Sync data!",
                        Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.nav_info: {
                Intent intent = new Intent(MainActivity.this, InformationActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.nav_settings: {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);

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
    }

    @Override
    protected void onResume() {
        super.onResume();

        mDataHandler.sendEmptyMessage(MESSAGE_SEND_DATA);

        setFilters();  // Start listening notifications from USBService
        startService(USBService.class, usbConnection, null); // Start USBService(if it was not started before) and Bind it
    }

    @Override
    protected void onPause() {
        super.onPause();

        mDataHandler.removeMessages(MESSAGE_SEND_DATA);

        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
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

        Intent intent = new Intent(MainActivity.this, AddConstructionActivity.class);

        intent.putExtra("latitude", latLng.latitude);
        intent.putExtra("longitude", latLng.longitude);

        startActivity(intent);
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
        // getting the Coordinates
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
        Intent intent = new Intent(MainActivity.this, ChooseBancheActivity.class);

        intent.putExtra("constructionName", marker.getTitle());
        intent.putExtra("latitude", marker.getPosition().latitude);
        intent.putExtra("longitude", marker.getPosition().longitude);

        startActivity(intent);

        return true;
    }
}
