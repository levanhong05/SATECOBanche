package com.dfm.honglv.satecobanche.functions;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.dfm.honglv.satecobanche.R;
import com.dfm.honglv.satecobanche.databases.ConstructionDetails;
import com.dfm.honglv.satecobanche.databases.DatabaseHelper;
import com.dfm.honglv.satecobanche.databases.FormworkDetails;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddFormworkActivity extends Activity implements View.OnClickListener  {

    // Reference of DatabaseHelper class to access its DAOs and other components
    private DatabaseHelper databaseHelper = null;

    private EditText txtFormworkName;
    private Spinner cboConstruction;

    private Button btnOK, btnCancel;

    // Declaration of DAO to interact with corresponding table
    private Dao<ConstructionDetails, Integer> constructionDao;

    // It holds the list of ConstructionDetails object fetched from Database
    private List<ConstructionDetails> constructionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_formwork);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        getWindow().setLayout((int)(dm.widthPixels * 0.9), (int)(dm.heightPixels * 0.4));

        txtFormworkName = (EditText) findViewById(R.id.txtFormworkName);
        cboConstruction = (Spinner) findViewById(R.id.cboConstruction);

        btnOK = (Button) findViewById(R.id.btnOK);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        btnOK.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        try {
            // This is how, a reference of DAO object can be done
            constructionDao = getHelper().getConstructionDao();

            // Query the database. We need all the records so, used queryForAll()
            constructionList = constructionDao.queryForAll();

            ArrayList<String> constructions = new ArrayList<String>();

            // Iterate through the FormworkDetails object iterator and populate the comma separated String
            for (ConstructionDetails construction : constructionList) {
                constructions.add(construction.constructionName);
            }

            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_item, constructions);

            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            cboConstruction.setAdapter(adapter);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            if (txtFormworkName.getText().toString().trim().length() > 0) {
                // Once click on "Submit", it's first creates the TeacherDetails object
                final FormworkDetails formwork = new FormworkDetails();

                // Then, set all the values from user input
                formwork.formworkName = txtFormworkName.getText().toString();

                try {
                    // This is how, a reference of DAO object can be done
                    final Dao<FormworkDetails, Integer> formworkDao = getHelper().getFormworkDao();

                    //This is the way to insert data into a database table
                    formworkDao.create(formwork);

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
