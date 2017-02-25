package com.dfm.honglv.satecobanche.main;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.Toast;

import com.dfm.honglv.satecobanche.R;

public class MainActivity extends AppCompatActivity {

    TabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

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

        int position = mTabHost.getCurrentTab();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_contruction: {
                Toast.makeText(getApplicationContext(), "Scan contructions!",
                        Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.action_information: {
                Toast.makeText(getApplicationContext(), "Informations!",
                        Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.action_settings: {
                Toast.makeText(getApplicationContext(), "Settings!",
                        Toast.LENGTH_SHORT).show();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
