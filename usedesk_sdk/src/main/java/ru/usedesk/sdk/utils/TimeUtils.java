package ru.usedesk.sdk.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

    private static final String TAG = TimeUtils.class.getSimpleName();

    private static final String IN_TIME_FORMAT_1 = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String IN_TIME_FORMAT_2 = "yyyy-MM-dd HH:mm:ss";

    private static final String OUT_TIME_FORMAT = "hh:mm:ss";

    private TimeUtils() {
    }

    public static String parseTime(String stringTimestamp) {
        SimpleDateFormat inDateFormat1 = new SimpleDateFormat(IN_TIME_FORMAT_1);
        SimpleDateFormat inDateFormat2 = new SimpleDateFormat(IN_TIME_FORMAT_2);

        SimpleDateFormat outDateFormat = new SimpleDateFormat(OUT_TIME_FORMAT);

        try {
            // try to parse with format #1
            Date date1 = inDateFormat1.parse(stringTimestamp.replaceAll("Z$", "+0000"));
            return outDateFormat.format(date1);
        } catch (ParseException e) {
            LogUtils.LOGE(TAG, e);

            // try to parse with format #2
            try {
                Date date2 = inDateFormat2.parse(stringTimestamp);
                return outDateFormat.format(date2);
            } catch (ParseException e1) {
                LogUtils.LOGE(TAG, e);
            }
        }

        return null;
    }
}