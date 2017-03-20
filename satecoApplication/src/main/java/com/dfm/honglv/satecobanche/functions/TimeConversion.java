package com.dfm.honglv.satecobanche.functions;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by honglv on 20/03/2017.
 */

public class TimeConversion {
    public static int dateToTimestamp(Date time) {
        return (int)(time.getTime() / 1000);
    }

    public static Date timestampToDate(long timestamp) {
        Timestamp stamp = new Timestamp(timestamp);
        Date date = new Date(stamp.getTime());

        return date;
    }
}
