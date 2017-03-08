package com.dfm.honglv.satecobanche.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dfm.honglv.satecobanche.databases.ConstructionDetails;

import java.util.List;

/**
 * Created by honglv on 07/03/2017.
 */

// Custom Adapter to feed data to the Construction Spinner
@SuppressWarnings("rawtypes")
public class FormworkAdapter extends ArrayAdapter<String> {
    LayoutInflater inflater;

    // Holds data of Construction Details
    List objects;

    @SuppressWarnings("unchecked")
    public FormworkAdapter(Context context, int resource, int dropDownViewResource, List objects) {
        super(context, resource, objects);
        this.setDropDownViewResource(dropDownViewResource);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // set the Construction Details objects to populate the Spinner
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    // Inflate the Android default spinner layout view, set the label according to passed data and
    // return the view to display as one row of the Construction Spinner
    public View getCustomView(int position, View convertView, ViewGroup parent) {

        final View row = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);

        final TextView label = (TextView) row.findViewById(android.R.id.text1);
        label.setPadding(16, 16, 16, 16);
        final ConstructionDetails construction = (ConstructionDetails) this.objects.get(position);
        label.setText(construction.constructionName);

        return row;
    }
}
