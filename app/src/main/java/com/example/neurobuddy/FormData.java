package com.example.neurobuddy;

import com.example.neurobuddy.Plan.DataItem;

public class FormData implements DataItem {

    private int time;
    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;

    public FormData() {

    }

    public FormData(int time, int startHour, int startMinute, int endHour, int endMinute) {
        this.time = time;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }
}
