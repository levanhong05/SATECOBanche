package com.dfm.honglv.satecobanche.functions;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.dfm.honglv.satecobanche.R;

public class SaveFileActivity extends AppCompatActivity
        implements OnClickListener, OnItemClickListener {

    ListView lvList;

    ArrayList<String> listItems = new ArrayList<String>();

    ArrayAdapter<String> adapter;

    EditText txtFileName;

    Button btnOK;
    Button btnCancel;

    String currentPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_file);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        try {
            /* Initializing Widgets */
            lvList = (ListView) findViewById(R.id.SFA_LvList);
            txtFileName = (EditText) findViewById(R.id.SFA_TxtFileName);
            btnOK = (Button) findViewById(R.id.SFA_BtnOK);
            btnCancel = (Button) findViewById(R.id.SFA_BtnCancel);

            if (savedInstanceState == null) {
                Bundle extras = getIntent().getExtras();

                if (extras != null) {
                    txtFileName.setText(extras.getString("fileName"));
                }
            } else {
                txtFileName.setText((String) savedInstanceState.getSerializable("fileName"));
            }

            /* Initializing Event Handlers */
            lvList.setOnItemClickListener(this);

            btnOK.setOnClickListener(this);
            btnCancel.setOnClickListener(this);

            setCurrentPath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void setCurrentPath(String path) {
        ArrayList<String> folders = new ArrayList<String>();

        ArrayList<String> files = new ArrayList<String>();

        currentPath = path;

        File[] allEntries = new File(path).listFiles();

        for (int i = 0; i < allEntries.length; i++) {
            if (allEntries[i].isDirectory()) {
                folders.add(allEntries[i].getName());
            } else if (allEntries[i].isFile()) {
                files.add(allEntries[i].getName());
            }
        }

        Collections.sort(folders, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });

        Collections.sort(files, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });

        listItems.clear();

        for (int i = 0; i < folders.size(); i++) {
            listItems.add(folders.get(i) + "/");
        }

        for (int i = 0; i < files.size(); i++) {
            String tmp = files.get(i);
            tmp = tmp.toLowerCase();

            if (tmp.endsWith(".csv")) {
                listItems.add(files.get(i));
            }
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
        adapter.notifyDataSetChanged();

        lvList.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!currentPath.equals(Environment.getExternalStorageDirectory().getAbsolutePath() + "/")) {
            setCurrentPath(new File(currentPath).getParent() + "/");
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.SFA_BtnOK:
                intent = new Intent();
                intent.putExtra("filePath", currentPath + txtFileName.getText().toString());
                intent.putExtra("shortFileName", txtFileName.getText().toString());
                setResult(RESULT_OK, intent);

                this.finish();

                break;

            case R.id.SFA_BtnCancel:
                intent = new Intent();
                intent.putExtra("filePath", "");
                intent.putExtra("shortFileName", "");
                setResult(RESULT_CANCELED, intent);

                this.finish();

                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String entryName = (String) parent.getItemAtPosition(position);
        if (entryName.endsWith("/")) {
            setCurrentPath(currentPath + entryName);
        } else {
            this.setTitle(this.getResources().getString(R.string.title_activity_open_file)
                    + "[" + entryName + "]");

            txtFileName.setText(entryName);
        }
    }
}
