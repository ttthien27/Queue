package com.android.queue.utils;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class TimestampHelper {
    static public Timestamp datetimeToTimestamp(String datetime){
        return Timestamp.valueOf(datetime);
    }

    static public String toDatetime (Long timestamp) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss");
        return simpleDateFormat.format(timestamp);
    }

    static public Long TimestampDiff (Long before, Long after) {
        return after - before;
    }

    static public Long getMinutes (Long duration) {
        long minutes = TimeUnit.MINUTES.toMinutes(duration);
        if (minutes == 0) {
            return 1L;
        } else {
            return minutes;
        }
    }
}
