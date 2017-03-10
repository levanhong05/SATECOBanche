package com.dfm.honglv.satecobanche.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dfm.honglv.satecobanche.R;
import com.dfm.honglv.satecobanche.databases.FormWorkDetails;
import com.dfm.honglv.satecobanche.databases.DatabaseHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

public class ChooseFormWorkActivity extends Activity {

    ListView listView;
    TextView txtNameConstruction, txtLatitude, txtLongitude;

    // Reference of DatabaseHelper class to access its DAOs and other components
    private DatabaseHelper databaseHelper = null;

    private Dao<FormWorkDetails, Integer> formworkDao;

    // It holds the list of FormWorkDetails objects fetched from Database
    private List<FormWorkDetails> formworkList;

    private int constructionId;
    private double latitude;
    private double longitude;

    private String constructionName;

    private ArrayAdapter<FormWorkDetails> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_banche);

        txtNameConstruction = (TextView) findViewById(R.id.txtConstructionName);
        txtLatitude = (TextView) findViewById(R.id.txtLatitude);
        txtLongitude = (TextView) findViewById(R.id.txtLongitude);

        listView = (ListView) findViewById(R.id.list_banche);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();

            if (extras == null) {
                latitude = 48.857708;
                longitude = 2.348928;
                constructionId = -1;
                constructionName = "Paris";
            } else {
                constructionId = extras.getInt("constructionId");
                constructionName = extras.getString("constructionName");
                latitude = extras.getDouble("latitude");
                longitude = extras.getDouble("longitude");
            }
        } else {
            constructionId = (int) savedInstanceState.getSerializable("constructionId");
            constructionName = (String) savedInstanceState.getSerializable("constructionName");
            latitude = (double) savedInstanceState.getSerializable("latitude");
            longitude = (double) savedInstanceState.getSerializable("longitude");
        }

        txtNameConstruction.setText(constructionName);
        txtLatitude.setText("" + latitude);
        txtLongitude.setText("" + longitude);

        try {
            // This is how, a reference of DAO object can be done
            formworkDao = getHelper().getFormworkDao();

            // Get our query builder from the DAO
            final QueryBuilder<FormWorkDetails, Integer> queryBuilder = formworkDao.queryBuilder();

            // We need only Banche which are associated with the selected Construction, so build the query by "Where" clause
            queryBuilder.where().eq(FormWorkDetails.CONSTRUCTION_ID_FIELD, constructionId);

            // Prepare our SQL statement
            final PreparedQuery<FormWorkDetails> preparedQuery = queryBuilder.prepare();

            // Fetch the list from Database by querying it
            formworkList = formworkDao.query(preparedQuery);

            mAdapter = new ArrayAdapter<FormWorkDetails>(this, android.R.layout.simple_expandable_list_item_1, formworkList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    final TextView tv;
                    if (convertView == null){
                        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        tv = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, null);
                    } else {
                        tv = (TextView) convertView;
                    }

                    final FormWorkDetails formwork = formworkList.get(position);
                    final String name = formwork.formWorkName;

                    tv.setText(name);

                    return tv;
                }

            };

            listView.setAdapter(mAdapter);

            // Attach OnItemClickListener to track user action and perform accordingly
            listView.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position >= formworkList.size()) {
                        Toast.makeText(getApplicationContext(), "Illegal form-work position.!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final FormWorkDetails formwork = formworkList.get(position);
                    Intent intent = new Intent("com.dfm.honglv.satecobanche.main.FormWorkActivity");

                    intent.putExtra("formWorkId", formwork.formWorkId);
                    intent.putExtra("formWorkName", formwork.formWorkName);

                    startActivity(intent);
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
         */
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }


}
