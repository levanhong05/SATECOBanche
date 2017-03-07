package com.dfm.honglv.satecobanche.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TwoLineListItem;

import com.dfm.honglv.satecobanche.R;
import com.dfm.honglv.satecobanche.databases.FormworkDetails;
import com.dfm.honglv.satecobanche.databases.DatabaseHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.List;

public class ChooseBancheActivity extends Activity {

    ListView listView;
    TextView txtNameConstruction, txtLatitude, txtLongitude;

    // Reference of DatabaseHelper class to access its DAOs and other components
    private DatabaseHelper databaseHelper = null;

    private Dao<FormworkDetails, Integer> bancheDao;

    // It holds the list of FormworkDetails objects fetched from Database
    private List<FormworkDetails> bancheList;

    private int constructionId;
    private double latitude;
    private double longitude;

    private String constructionName;

    private ArrayAdapter<FormworkDetails> mAdapter;

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
            bancheDao = getHelper().getFormworkDao();

            // Get our query builder from the DAO
            final QueryBuilder<FormworkDetails, Integer> queryBuilder = bancheDao.queryBuilder();

            // We need only Banche which are associated with the selected Construction, so build the query by "Where" clause
            queryBuilder.where().eq(FormworkDetails.CONSTRUCTION_ID_FIELD, constructionId);

            // Prepare our SQL statement
            final PreparedQuery<FormworkDetails> preparedQuery = queryBuilder.prepare();

            // Fetch the list from Database by querying it
            bancheList = bancheDao.query(preparedQuery);

            mAdapter = new ArrayAdapter<FormworkDetails>(this,
                    android.R.layout.simple_expandable_list_item_2, bancheList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    final TwoLineListItem row;
                    if (convertView == null){
                        final LayoutInflater inflater =
                                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        row = (TwoLineListItem) inflater.inflate(android.R.layout.simple_list_item_2, null);
                    } else {
                        row = (TwoLineListItem) convertView;
                    }

                    final FormworkDetails banche = bancheList.get(position);
                    final String name = banche.formworkName;

                    row.getText1().setText(name);

                    return row;
                }

            };

            listView.setAdapter(mAdapter);

            // Attach OnItemClickListener to track user action and perform accordingly
            listView.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("ChooseBanche", "Pressed item " + position);
                    if (position >= bancheList.size()) {
                        Log.w("ChooseBanche", "Illegal position.");
                        return;
                    }

                    final FormworkDetails banche = bancheList.get(position);
                    Intent intent = new Intent("com.dfm.honglv.satecobanche.main.FormworkActivity");
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
