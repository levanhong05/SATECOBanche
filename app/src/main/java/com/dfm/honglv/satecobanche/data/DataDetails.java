package com.dfm.honglv.satecobanche.data;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by honglv on 02/03/2017.
 */

public class DataDetails implements Serializable {
    /**
     * Model class for student_details database table
     */
    private static final long serialVersionUID = -222864131214757024L;

    public static final String ID_FIELD = "data_id";
    public static final String CHIP_ID_FIELD = "chip_id";

    // Primary key defined as an auto generated integer
    // If the database table column name differs than the Model class variable name, the way to map to use columnName
    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    public int dataId;

    @DatabaseField(columnName = "key")
    public String key;

    @DatabaseField(columnName = "value")
    public String value;

    // Foreign key defined to hold associations
    @DatabaseField(canBeNull = false, foreign = true)
    public ChipDetails chip;

    // Define a String type field to hold student's date of insertion
    @DatabaseField(columnName = "added_date")
    public Date addedDate;

    // Default constructor is needed for the SQLite, so make sure you also have it
    public DataDetails(){

    }

    //For our own purpose, so it's easier to create a StudentDetails object
    public DataDetails(final String key, final String value, ChipDetails chip){
        this.addedDate = new Date();
        this.key = key;
        this.value = value;
        this.chip = chip;
    }
}
