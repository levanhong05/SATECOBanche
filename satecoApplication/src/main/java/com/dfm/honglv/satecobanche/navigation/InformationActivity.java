package com.dfm.honglv.satecobanche.navigation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.dfm.honglv.satecobanche.BuildConfig;
import com.dfm.honglv.satecobanche.R;

public class InformationActivity extends AppCompatActivity {

    TextView txtVersion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txtVersion = (TextView) findViewById(R.id.txtVersion);

        String versionName = BuildConfig.VERSION_NAME;

        txtVersion.setText("Version " + versionName);
    }


}
