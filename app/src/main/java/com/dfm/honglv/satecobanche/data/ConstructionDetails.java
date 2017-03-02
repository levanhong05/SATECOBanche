package com.dfm.honglv.satecobanche.data;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

/**
 * Created by honglv on 02/03/2017.
 */

public class ConstructionDetails implements Serializable {
    /**
     *  Model class for teacher_details database table
     */
    private static final long serialVersionUID = -222864131214757024L;

    // Primary key defined as an auto generated integer
    // If the database table column name differs than the Model class variable name, the way to map to use columnName
    @DatabaseField(generatedId = true, columnName = "construction_id")
    public int constructionId;

    @DatabaseField(columnName = "construction_name")
    public String constructionName;

    // Define a String type field to hold student's address
    @DatabaseField(columnName = "latitude")
    public double latitude;

    @DatabaseField(columnName = "longitude")
    public double longitude;

    // Default constructor is needed for the SQLite, so make sure you also have it
    public ConstructionDetails(){

    }

    //For our own purpose, so it's easier to create a ConstructionDetails object
    public ConstructionDetails(final String name, final double latitude, final double longitude){
        this.constructionName = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
