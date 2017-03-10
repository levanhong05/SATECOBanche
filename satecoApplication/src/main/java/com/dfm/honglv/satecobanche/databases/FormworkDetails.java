package com.dfm.honglv.satecobanche.databases;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

/**
 * Created by honglv on 02/03/2017.
 */

public class FormWorkDetails implements Serializable {
    /**
     * Model class for student_details database table
     */
    private static final long serialVersionUID = -222864131214757024L;

    public static final String ID_FIELD = "formwork_id";
    public static final String CONSTRUCTION_ID_FIELD = "construction_id";

    // Primary key defined as an auto generated integer
    // If the database table column name differs than the Model class variable name, the way to map to use columnName
    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    public int formWorkId;

    // Define a String type field to hold student's name
    @DatabaseField(columnName = "formwork_name")
    public String formWorkName;

    // Foreign key defined to hold associations
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    public ConstructionDetails construction;

    // Default constructor is needed for the SQLite, so make sure you also have it
    public FormWorkDetails(){

    }

    //For our own purpose, so it's easier to create a StudentDetails object
    public FormWorkDetails(final String name, ConstructionDetails construction){
        this.formWorkName = name;
        this.construction = construction;
    }
}
