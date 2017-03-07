package com.dfm.honglv.satecobanche.navigation;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.dfm.honglv.satecobanche.R;
import com.dfm.honglv.satecobanche.databases.FormworkDetails;
import com.dfm.honglv.satecobanche.databases.ConstructionDetails;
import com.dfm.honglv.satecobanche.databases.DatabaseHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ViewConstructionDetailsActivity extends Activity implements OnClickListener {

    // Reference of DatabaseHelper class to access its DAOs and other components
    private DatabaseHelper databaseHelper = null;

    // Declaration of screen components
    private TextView txtConstructionName, txtLatitude, txtLongitude;
    private Button btnClose;

    // Declaration of DAO to interact with corresponding table
    private Dao<FormworkDetails, Integer> bancheDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_construction);

        txtConstructionName = (TextView) findViewById(R.id.txtConstructionName);
        txtLatitude = (TextView) findViewById(R.id.txtLatitude);
        txtLongitude = (TextView) findViewById(R.id.txtLongitude);
        btnClose = (Button) findViewById(R.id.close_btn);

        btnClose.setOnClickListener(this);

        //Receive the ConstructionDetails object which has sent by the previous screen through Intent
        final ConstructionDetails tDetails = (ConstructionDetails) getIntent().getExtras().getSerializable("details");

        txtConstructionName.setText(tDetails.constructionName);
        txtLatitude.setText("" + tDetails.latitude);
        txtLongitude.setText("" + tDetails.longitude);

        // This String would hold the list of all associated banche's name (comma separated) for the selected Construction
        final List<String> bancheName = new ArrayList<String>();

        try {
            // This is how, a reference of DAO object can be done
            bancheDao = getHelper().getFormworkDao();

            // Get our query builder from the DAO
            final QueryBuilder<FormworkDetails, Integer> queryBuilder = bancheDao.queryBuilder();

            // We need only Banche who are associated with the selected Constructions, so build the query by "Where" clause
            queryBuilder.where().eq(FormworkDetails.CONSTRUCTION_ID_FIELD, tDetails.constructionId);

            // Prepare our SQL statement
            final PreparedQuery<FormworkDetails> preparedQuery = queryBuilder.prepare();

            // Fetch the list from Database by queryingit
            final Iterator<FormworkDetails> bancheIt = bancheDao.query(preparedQuery).iterator();

            // Iterate through the FormworkDetails object iterator and populate the comma separated String
            while (bancheIt.hasNext()) {
                final FormworkDetails sDetails = bancheIt.next();
                bancheName.add(sDetails.formworkName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        txtLongitude.setText(bancheName.toString());
    }

    // This is how, DatabaseHelper can be initialized for future use
    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
         * You'll need this in your class to release the helper when done.
         * */
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    @Override
    public void onClick(View v) {

        if (v == btnClose) {
            finish();
        }

    }
}
