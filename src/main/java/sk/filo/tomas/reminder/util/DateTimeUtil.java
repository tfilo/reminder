package sk.filo.tomas.reminder.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by tomas on 12.6.2017.
 */

public class DateTimeUtil {

    public static final SimpleDateFormat[] birthdayFormats = {
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("yyyy.MM.dd"),
            new SimpleDateFormat("yy-MM-dd"),
            new SimpleDateFormat("yy.MM.dd"),
            new SimpleDateFormat("yy/MM/dd"),
            new SimpleDateFormat("MMM dd, yyyy")
    };

    public static final SimpleDateFormat birthdayFormatNoYear = new SimpleDateFormat("--MM-dd");

    public static Integer getDateTime(Date date) {
        if (date == null) return null;
        return new Long(date.getTime() / 1000).intValue();
    }

    public static Date getDateTime(Integer date) {
        if (date == null) return null;
        return new Date(date * 1000L);
    }

    public static Calendar getNextMidnight() {
        Calendar nextMidnight = Calendar.getInstance();
        nextMidnight.set(Calendar.HOUR_OF_DAY, 0);
        nextMidnight.set(Calendar.MINUTE, 0);
        nextMidnight.set(Calendar.SECOND, 0);
        nextMidnight.set(Calendar.MILLISECOND, 0);
        nextMidnight.set(Calendar.DAY_OF_YEAR, nextMidnight.get(Calendar.DAY_OF_YEAR) + 1);
        return nextMidnight;
    }

    public static Calendar getLastMidnight() {
        Calendar lastMidnight = Calendar.getInstance();
        lastMidnight.set(Calendar.HOUR_OF_DAY, 0);
        lastMidnight.set(Calendar.MINUTE, 0);
        lastMidnight.set(Calendar.SECOND, 0);
        lastMidnight.set(Calendar.MILLISECOND, 0);
        return lastMidnight;
    }
}
