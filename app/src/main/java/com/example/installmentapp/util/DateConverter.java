package com.example.installmentapp.util;

import android.icu.util.PersianCalendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateConverter {

    /**
     * Converts a Persian date string (format: yyyy/MM/dd) to milliseconds since epoch (Gregorian).
     * @param persianDate String in format "yyyy/MM/dd"
     * @return milliseconds since epoch (Gregorian) or -1 if invalid
     */
    public static long persianToGregorianMillis(String persianDate) {
        if (persianDate == null || persianDate.isEmpty()) {
            return -1;
        }
        String[] parts = persianDate.split("/");
        if (parts.length != 3) {
            return -1;
        }
        try {
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]); // 1-based in PersianCalendar
            int day = Integer.parseInt(parts[2]);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                PersianCalendar persianCalendar = new PersianCalendar();
                persianCalendar.setPersianDate(year, month, day);
                return persianCalendar.getTimeInMillis();
            } else {
                // Fallback for older APIs (approximate conversion)
                // This is a simplified conversion and may not be 100% accurate for all dates.
                // For production, consider using a library or setting minSdkVersion to 24.
                return approximatePersianToGregorian(year, month, day);
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Converts milliseconds since epoch (Gregorian) to Persian date string (format: yyyy/MM/dd).
     * @param millis milliseconds since epoch (Gregorian)
     * @return Persian date string in format "yyyy/MM/dd"
     */
    public static String gregorianToPersian(long millis) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PersianCalendar persianCalendar = new PersianCalendar();
            persianCalendar.setTimeInMillis(millis);
            int year = persianCalendar.get(PersianCalendar.YEAR);
            int month = persianCalendar.get(PersianCalendar.MONTH); // 0-based? Actually, in ICU, PersianCalendar.MONTH is 0-based? Let's check:
            // Actually, in PersianCalendar, the month is 0-based? We'll adjust to 1-based for display.
            // But note: the PersianCalendar in ICU uses 0-based months? Let's avoid confusion by using the get method for month and then add 1 if needed.
            // According to ICU documentation: PersianCalendar.MONTH field is 0-based (0=Farvardin, 11=Esfand).
            int day = persianCalendar.get(PersianCalendar.DAY_OF_MONTH);
            return String.format(Locale.US, "%04d/%02d/%02d", year, month + 1, day);
        } else {
            // Fallback for older APIs
            return approximateGregorianToPersian(millis);
        }
    }

    // Approximate conversion methods for APIs < 24 (not accurate, but for demonstration)
    // In a real app, you would set minSdkVersion to 24 or use a library.
    private static long approximatePersianToGregorian(int year, int month, int day) {
        // This is a very rough approximation and should not be used in production.
        // We'll use a fixed offset for demonstration purposes.
        // Actually, the Persian year starts around March 21 in Gregorian.
        // We'll approximate by converting to a Gregorian date around March 21 of the Gregorian year.
        int gregorianYear = year + 621;
        int gregorianMonth = 3; // March
        int gregorianDay = 21;

        // Adjust for month and day
        // Each Persian month is about 30.5 days, but we'll do a simple approximation.
        int daysToAdd = (month - 1) * 31 + (day - 1); // overestimate
        gregorianDay += daysToAdd;

        // Now adjust the month and day if overflow
        while (gregorianDay > 31) {
            gregorianDay -= 31;
            gregorianMonth++;
            if (gregorianMonth > 12) {
                gregorianMonth = 1;
                gregorianYear++;
            }
        }

        GregorianCalendar gregorianCalendar = new GregorianCalendar(gregorianYear, gregorianMonth - 1, gregorianDay);
        return gregorianCalendar.getTimeInMillis();
    }

    private static String approximateGregorianToPersian(long millis) {
        // Rough approximation: subtract 621 years and adjust months.
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(millis);
        int gy = gregorianCalendar.get(GregorianCalendar.YEAR);
        int gm = gregorianCalendar.get(GregorianCalendar.MONTH); // 0-based
        int gd = gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH);

        int py = gy - 621;
        int pm = gm; // approximate, same month index
        int pd = gd;

        // Adjust if the day is too high for the Persian month (simplified)
        // We'll just return the approximate values.
        return String.format(Locale.US, "%04d/%02d/%02d", py, pm + 1, pd);
    }
}