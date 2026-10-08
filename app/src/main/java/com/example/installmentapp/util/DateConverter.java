package com.example.installmentapp.util;

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
     * Uses Jalaali algorithm for accurate conversion without Android ICU dependency.
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
            int month = Integer.parseInt(parts[1]); // 1-based (1-12)
            int day = Integer.parseInt(parts[2]);   // 1-based (1-31)

            // Use Jalaali to Gregorian conversion (pure Java)
            return jalaliToGregorian(year, month, day).getTimeInMillis();
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Converts milliseconds since epoch (Gregorian) to Persian date string (format: yyyy/MM/dd).
     * Uses Jalaali algorithm for accurate conversion without Android ICU dependency.
     * @param millis milliseconds since epoch (Gregorian)
     * @return Persian date string in format "yyyy/MM/dd"
     */
    public static String gregorianToPersian(long millis) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        gregorianCalendar.setTimeInMillis(millis);
        int gy = gregorianCalendar.get(GregorianCalendar.YEAR);
        int gm = gregorianCalendar.get(GregorianCalendar.MONTH) + 1; // 1-based
        int gd = gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH);

        // Use Gregorian to Jalaali conversion (pure Java)
        int[] j = gregorianToJalali(gy, gm, gd);
        return String.format(Locale.US, "%04d/%02d/%02d", j[0], j[1], j[2]);
    }

    // ============================================================
    // Pure Java Jalaali (Persian) <-> Gregorian conversion algorithms
    // Based on Jalaali calendar algorithm (public domain)
    // ============================================================

    private static GregorianCalendar jalaliToGregorian(int jy, int jm, int jd) {
        int[] g = jalaliToGregorianArray(jy, jm, jd);
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(g[0], g[1] - 1, g[2]);
        return cal;
    }

    private static int[] jalaliToGregorianArray(int jy, int jm, int jd) {
        // Jalaali to Gregorian conversion
        // Based on algorithm from: https://github.com/jalaali/jalaali-js
        int[] d = new int[]{0, 31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29};
        int gy = jy + 621;
        int days = (jm - 1) * 31 + (jd - 1);

        // Adjust for leap years
        int leap = isJalaliLeapYear(jy) ? 1 : 0;
        if (jm > 7) {
            days += 6 - leap;
        } else {
            days += 0;
        }

        // Convert to day of year
        int doy = days + 79; // 1 Farvardin = March 21 (approximately)

        // Now find the Gregorian year and day of year
        while (true) {
            int daysInYear = isGregorianLeapYear(gy) ? 366 : 365;
            if (doy > daysInYear) {
                doy -= daysInYear;
                gy++;
            } else {
                break;
            }
        }

        // Now find month and day from day of year
        int[] daysInMonth = {31, isGregorianLeapYear(gy) ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int month = 1;
        int day = doy;
        for (int m = 0; m < 12; m++) {
            if (day <= daysInMonth[m]) {
                month = m + 1;
                break;
            }
            day -= daysInMonth[m];
        }

        return new int[]{gy, month, day};
    }

    private static int[] gregorianToJalali(int gy, int gm, int gd) {
        // Gregorian to Jalaali conversion
        // Based on algorithm from: https://github.com/jalaali/jalaali-js

        int[] gDaysInMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int[] jDaysInMonth = {31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29};

        // Day of year in Gregorian
        int doy = gd;
        for (int m = 0; m < gm - 1; m++) {
            doy += gDaysInMonth[m];
        }
        if (gm > 2 && isGregorianLeapYear(gy)) {
            doy++;
        }

        // Convert to Jalaali
        // 1 Farvardin = March 21 (day 80 or 81 in Gregorian)
        int offset = isGregorianLeapYear(gy) ? 80 : 79;
        int jDoy = doy - offset;

        int jy = gy - 621;
        if (jDoy <= 0) {
            jy--;
            int daysInPrevJalaliYear = isJalaliLeapYear(jy) ? 366 : 365;
            jDoy += daysInPrevJalaliYear;
        } else {
            int daysInJalaliYear = isJalaliLeapYear(jy) ? 366 : 365;
            if (jDoy > daysInJalaliYear) {
                jDoy -= daysInJalaliYear;
                jy++;
            }
        }

        // Find Jalaali month and day
        int jm = 1;
        int jd = jDoy;
        for (int m = 0; m < 12; m++) {
            int daysInMonth = jDaysInMonth[m];
            // Adjust for leap year on last month
            if (m == 11 && isJalaliLeapYear(jy)) {
                daysInMonth = 30;
            }
            if (jd <= daysInMonth) {
                jm = m + 1;
                break;
            }
            jd -= daysInMonth;
        }

        return new int[]{jy, jm, jd};
    }

    private static boolean isGregorianLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    private static boolean isJalaliLeapYear(int year) {
        // Jalaali leap year rule: years divisible by 33 with remainder in {1, 5, 9, 13, 17, 22, 26, 30}
        int[] leapYears = {1, 5, 9, 13, 17, 22, 26, 30};
        int remainder = year % 33;
        for (int ly : leapYears) {
            if (remainder == ly) return true;
        }
        return false;
    }
}