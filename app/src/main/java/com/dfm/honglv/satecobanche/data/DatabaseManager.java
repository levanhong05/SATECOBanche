package com.dfm.honglv.satecobanche.data;

import android.content.Context;

/**
 * Created by honglv on 02/03/2017.
 */

public class DatabaseManager {
    static private DatabaseManager instance;

    static public void init(Context ctx) {
        if (null == instance) {
            instance = new DatabaseManager(ctx);
        }
    }

    static public DatabaseManager getInstance() {
        return instance;
    }

    private DatabaseHelper helper;
    private DatabaseManager(Context ctx) {
        helper = new DatabaseHelper(ctx);
    }

    public DatabaseHelper getHelper() {
        return helper;
    }

}
