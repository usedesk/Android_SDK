package ru.usedesk.chat_gui.internal.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {
    private static final String IN_TIME_FORMAT_1 = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String IN_TIME_FORMAT_2 = "yyyy-MM-dd HH:mm:ss";

    private static final String OUT_TIME_FORMAT = "hh:mm";

    private TimeUtils() {
    }

    public static String parseTime(String stringTimestamp) {
        SimpleDateFormat outDateFormat = new SimpleDateFormat(OUT_TIME_FORMAT);

        try {
            // try to parse with format #1
            SimpleDateFormat inDateFormat = new SimpleDateFormat(IN_TIME_FORMAT_1);
            Date date1 = inDateFormat.parse(stringTimestamp.replaceAll("Z$", "+0000"));
            return outDateFormat.format(date1);
        } catch (ParseException e) {
            // try to parse with format #2
            try {
                SimpleDateFormat inDateFormat = new SimpleDateFormat(IN_TIME_FORMAT_2);
                Date date2 = inDateFormat.parse(stringTimestamp);
                return outDateFormat.format(date2);
            } catch (ParseException e1) {
                //nothing
            }
        }

        return null;
    }
}