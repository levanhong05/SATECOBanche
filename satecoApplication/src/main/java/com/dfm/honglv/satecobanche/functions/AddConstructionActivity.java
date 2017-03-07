package com.dfm.honglv.satecobanche.functions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dfm.honglv.satecobanche.R;
import com.dfm.honglv.satecobanche.databases.ConstructionDetails;
import com.dfm.honglv.satecobanche.databases.DatabaseHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

public class AddConstructionActivity extends Activity implements View.OnClickListener {

    // Reference of DatabaseHelper class to access its DAOs and other components
    private DatabaseHelper databaseHelper = null;

    private EditText txtConstructionName;
    private Button btnOK, btnCancel;

    double latitude, longitude;

    private EditText txtLatitude, txtLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_construction);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        getWindow().setLayout((int)(dm.widthPixels * 0.9), (int)(dm.heightPixels * 0.4));

        txtConstructionName = (EditText) findViewById(R.id.txtConstructionName);
        txtLatitude = (EditText) findViewById(R.id.add_latitude);
        txtLongitude = (EditText) findViewById(R.id.add_longitude);

        btnOK = (Button) findViewById(R.id.btnOK);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        btnOK.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();

            if (extras == null) {
                latitude = 48.857708;
                longitude = 2.348928;
            } else {
                latitude= extras.getDouble("latitude");
                longitude= extras.getDouble("longitude");
            }
        } else {
            latitude = (double) savedInstanceState.getSerializable("latitude");
            longitude = (double) savedInstanceState.getSerializable("longitude");
        }

        txtLatitude.setText("" + latitude);
        txtLongitude.setText("" + longitude);
    }

    // This is how, DatabaseHelper can be initialized for future use
    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this,DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
         *  * You'll need this in your class to release the helper when done.
         *  */
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnOK) {
            // All input fields are mandatory, so made a check
            if (txtConstructionName.getText().toString().trim().length() > 0) {
                // Once click on "Submit", it's first creates the TeacherDetails object
                final ConstructionDetails constructionDetails = new ConstructionDetails();

                // Then, set all the values from user input
                constructionDetails.constructionName = txtConstructionName.getText().toString();
                constructionDetails.latitude = latitude;
                constructionDetails.longitude = longitude;

                try {
                    // This is how, a reference of DAO object can be done
                    final Dao<ConstructionDetails, Integer> constructionDao = getHelper().getConstructionDao();

                    //This is the way to insert data into a database table
                    constructionDao.create(constructionDetails);

                    finish();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showMessageDialog("Error!!");
                }
            } else {
                showMessageDialog("All fields are mandatory !!");
            }
        } else if (v == btnCancel) {
            finish();
        }
    }

    private void showMessageDialog(final String message)
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
