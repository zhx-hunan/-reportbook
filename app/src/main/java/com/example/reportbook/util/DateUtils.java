package com.example.reportbook.util;

import android.content.Context;

import com.example.reportbook.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间格式化与问候语工具类。
 */
public final class DateUtils {

    private static final Locale LOCALE = Locale.CHINA;

    private DateUtils() {
    }

    public static String formatFull(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", LOCALE).format(new Date(timestamp));
    }

    /**
     * 今天 / 昨天显示具体时间，同一年显示月日，跨年显示完整日期。
     */
    public static String formatRelative(Context context, long timestamp) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(timestamp);

        boolean sameYear = now.get(Calendar.YEAR) == target.get(Calendar.YEAR);
        if (sameYear && now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)) {
            String time = new SimpleDateFormat("HH:mm", LOCALE).format(target.getTime());
            return context.getString(R.string.date_today, time);
        }

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        if (sameYear && yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)) {
            String time = new SimpleDateFormat("HH:mm", LOCALE).format(target.getTime());
            return context.getString(R.string.date_yesterday, time);
        }

        if (sameYear) {
            return new SimpleDateFormat("MM-dd HH:mm", LOCALE).format(target.getTime());
        }
        return new SimpleDateFormat("yyyy-MM-dd", LOCALE).format(target.getTime());
    }

    public static String greeting(Context context) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int resId;
        if (hour < 6) {
            resId = R.string.greeting_dawn;
        } else if (hour < 9) {
            resId = R.string.greeting_morning;
        } else if (hour < 12) {
            resId = R.string.greeting_forenoon;
        } else if (hour < 14) {
            resId = R.string.greeting_noon;
        } else if (hour < 18) {
            resId = R.string.greeting_afternoon;
        } else {
            resId = R.string.greeting_evening;
        }
        return context.getString(resId);
    }
}
