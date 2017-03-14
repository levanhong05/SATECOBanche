package com.dfm.honglv.satecobanche.databases;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;
import java.text.SimpleDateFormat;
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
    public static final String CHIP_ID_FIELD = "sensor_id";

    // Primary key defined as an auto generated integer
    // If the database table column name differs than the Model class variable name, the way to map to use columnName
    @DatabaseField(generatedId = true, columnName = ID_FIELD)
    public int dataId;

    @DatabaseField(columnName = "added_date")
    public String addedDate;

    @DatabaseField(columnName = "sensor_id")
    public int sensorId;

    @DatabaseField(columnName = "key")
    public String key;

    @DatabaseField(columnName = "value")
    public float value;

    @DatabaseField(columnName = "str_message")
    public String message;

    // Default constructor is needed for the SQLite, so make sure you also have it
    public DataDetails(){

    }

    //For our own purpose, so it's easier to create a DataDetails object
    public DataDetails(final int sensorId, final String key, final float value, final String message){
        this.addedDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
        this.sensorId = sensorId;
        this.key = key;
        this.value = value;
        this.message = message;
    }
}
