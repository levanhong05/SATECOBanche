package com.dfm.honglv.satecobanche.databases;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

/**
 * Created by honglv on 02/03/2017.
 */

public class FormworkDetails implements Serializable {
    /**
     * Model class for student_details database table
     */
    private static final long serialVersionUID = -222864131214757024L;

    public static final String ID_FIELD = "formwork_id";
    public static final String CONSTRUCTION_ID_FIELD = "construction_id";

    // Primary key defined as an auto generated integer
    // If the database table column name differs than the Model class variable name, the way to map to use columnName
    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    public int formworkId;

    // Define a String type field to hold student's name
    @DatabaseField(columnName = "formwork_name")
    public String formworkName;

    // Foreign key defined to hold associations
    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    public ConstructionDetails construction;

    // Default constructor is needed for the SQLite, so make sure you also have it
    public FormworkDetails(){

    }

    //For our own purpose, so it's easier to create a StudentDetails object
    public FormworkDetails(final String name, ConstructionDetails construction){
        this.formworkName = name;
        this.construction = construction;
    }
}
