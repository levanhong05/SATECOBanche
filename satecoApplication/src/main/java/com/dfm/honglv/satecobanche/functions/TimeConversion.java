package com.dfm.honglv.satecobanche.functions;

import java.util.Date;

/**
 * Created by honglv on 20/03/2017.
 */

public class TimeConversion {
    public static int dateToTimestamp(Date time) {
        return (int)(time.getTime() / 1000);
    }

    public static Date timestampToDate(long timestamp) {
        Date date = new Date(timestamp * 1000);

        return date;
    }
}
