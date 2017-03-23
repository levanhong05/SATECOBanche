package eu.dfm.satecobanche.navigation;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import eu.dfm.satecobanche.BuildConfig;
import eu.dfm.satecobanche.R;

import java.io.File;

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

        txtVersion.setText(getString(R.string.version) + " " + versionName);
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
