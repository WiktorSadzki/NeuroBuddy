package com.example.neurobuddy.Plan;

import java.util.UUID;

public class Plan implements DataItem {
    private String title;
    private String description;
    private String selectedDate;
    private String selectedDays;
    private String selectedTime;
    private String ndId;
    private boolean checked;
    private String type;
    private String planId;
    private String planType;
    private String duration;

    public Plan(String title, String description, String selectedDays, String selectedTime, String selectedDate) {
        this.title = title;
        this.description = description;
        this.selectedDays = selectedDays;
        this.selectedDate = selectedDate;
        this.selectedTime = selectedTime;

        this.ndId = UUID.randomUUID().toString();
        this.checked = false;
        this.type = "Specific";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getNdId() {
        return ndId;
    }

    public void setNdId(String ndId) {
        this.ndId = ndId;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getSelectedTime() {
        return selectedTime;
    }

    public void setSelectedTime(String selectedTime) {
        this.selectedTime = selectedTime;
    }

    public String getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSelectedDays() {
        return selectedDays;
    }

    public void setSelectedDays(String selectedDays) {
        this.selectedDays = selectedDays;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public Plan() {
    }
}
