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

    // Define a String type field to hold teacher's name
    @DatabaseField(columnName = "construction_name")
    public String constructionName;

    // Define a String type field to hold student's address
    public String address;

    // Default constructor is needed for the SQLite, so make sure you also have it
    public ConstructionDetails(){

    }

    //For our own purpose, so it's easier to create a TeacherDetails object
    public ConstructionDetails(final String name, final String address){
        this.constructionName = name;
        this.address = address;
    }
}
