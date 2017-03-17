package com.dfm.honglv.satecobanche.functions;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    String rootPath = null;

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

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            txtFileName.setText("sateco_" + timeStamp + ".csv");

            /* Initializing Event Handlers */
            lvList.setOnItemClickListener(this);

            btnOK.setOnClickListener(this);
            btnCancel.setOnClickListener(this);

            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            } else {
                rootPath = getFilesDir().getAbsolutePath();
            }

            setCurrentPath(rootPath + "/");
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
        if (!currentPath.equals(rootPath + "/")) {
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
                if (txtFileName.getText().toString().trim().equals("")) {
                    txtFileName.setError(getString(R.string.error_empty_field));
                } else {
                    Pattern pattern = Pattern.compile("^[a-zA-Z0-9_.-]*$");
                    Matcher matcher = pattern .matcher(txtFileName.getText().toString().trim());

                    if (!matcher.matches()) {
                        txtFileName.setError(getString(R.string.error_invalid_file_name));
                    } else {
                        intent = new Intent();
                        intent.putExtra("filePath", currentPath + txtFileName.getText().toString());
                        setResult(RESULT_OK, intent);
                        this.finish();
                    }
                }

                break;

            case R.id.SFA_BtnCancel:
                intent = new Intent();
                intent.putExtra("filePath", "");
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
