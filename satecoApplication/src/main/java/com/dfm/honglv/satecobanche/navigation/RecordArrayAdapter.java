package com.dfm.honglv.satecobanche.navigation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dfm.honglv.satecobanche.R;
import com.dfm.honglv.satecobanche.databases.BancheDetails;
import com.dfm.honglv.satecobanche.databases.ConstructionDetails;
import com.j256.ormlite.dao.Dao;

import java.util.List;

@SuppressWarnings("rawtypes")
public class RecordArrayAdapter extends ArrayAdapter<String> {

    private LayoutInflater inflater;

    // This would hold the database objects. It could be TeacherDetails or StudentDetails objects
    private List records;

    // Declaration of DAO to interact with corresponding table
    private Dao<ConstructionDetails, Integer> constructionDao;

    @SuppressWarnings("unchecked")
    public RecordArrayAdapter(Context context, int resource, List objects, Dao<ConstructionDetails, Integer> constructionDao) {
        super(context, resource, objects);

        this.records = objects;
        this.constructionDao = constructionDao;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Reuse the view to make the scroll effect smooth
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_item, parent, false);

        // If the ListView needs to display the records of StudentDetails objects
        if (records.get(position).getClass().isInstance(new BancheDetails())) {
            final BancheDetails bancheDetails = (BancheDetails) records.get(position);

            try {
                // Invoking refresh() method to fetch the reference data stored into ConstructionDetails table/object
                // Basically, it is an example of Lazy loading. It will join two tables internally only on demand

                constructionDao.refresh(bancheDetails.construction);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ((TextView) convertView.findViewById(R.id.banche_name_tv)).setText(bancheDetails.bancheName);
            ((TextView) convertView.findViewById(R.id.construction_name_tv)).setText(bancheDetails.construction.constructionName);
        }
        // If the ListView needs to display the records of TeacherDetails objects
        else {
            final ConstructionDetails constructionDetails = (ConstructionDetails) records.get(position);
            ((TextView) convertView.findViewById(R.id.banche_name_tv)).setText(constructionDetails.constructionName);
            ((TextView) convertView.findViewById(R.id.construction_name_tv)).setText(constructionDetails.constructionName);
        }
        return convertView;
    }

}
