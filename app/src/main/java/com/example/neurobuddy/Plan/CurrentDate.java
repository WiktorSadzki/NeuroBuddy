package com.example.neurobuddy.Plan;

import com.example.neurobuddy.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class CurrentDate {
    public static int dayOfWeek;
    public static int weekOfYear;
    public static LocalDate firstDayOfWeek;
    public static LocalDate lastDayOfWeek;

    public static LocalDate currentDate = LocalDate.now();
    WeekFields weekFields = WeekFields.of(Locale.getDefault());

    Calendar c = new GregorianCalendar();
    public void initializeDay() {
        c.setFirstDayOfWeek(Calendar.MONDAY);
        dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1;
    }

    public void initializeWeek() {
        weekOfYear = c.get(Calendar.WEEK_OF_YEAR);
        firstDayOfWeek = currentDate.with(weekFields.dayOfWeek(), 1);
        lastDayOfWeek = currentDate.with(weekFields.dayOfWeek(), 7);
    }
}