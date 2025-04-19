package com.lucky.truebond;

public class AnniversarySettings {
    private long anniversaryDate;
    private String reminderTime;
    private boolean reminderEnabled;

    public AnniversarySettings() {
        // Default constructor required for Firebase
    }

    public AnniversarySettings(long anniversaryDate, String reminderTime, boolean reminderEnabled) {
        this.anniversaryDate = anniversaryDate;
        this.reminderTime = reminderTime;
        this.reminderEnabled = reminderEnabled;
    }

    public long getAnniversaryDate() {
        return anniversaryDate;
    }

    public void setAnniversaryDate(long anniversaryDate) {
        this.anniversaryDate = anniversaryDate;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }
} 