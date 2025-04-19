package com.lucky.truebond;

import android.content.Context;
import android.content.SharedPreferences;

public class StreakManager {
    private static final String PREF_NAME = "StreakPrefs";
    private static final String KEY_LAST_MESSAGE_DATE = "last_message_date";
    private static final String KEY_CURRENT_STREAK = "current_streak";
    
    private SharedPreferences preferences;
    private int currentStreak;

    public StreakManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        currentStreak = preferences.getInt(KEY_CURRENT_STREAK, 0);
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void updateStreak() {
        long lastMessageDate = preferences.getLong(KEY_LAST_MESSAGE_DATE, 0);
        long currentTime = System.currentTimeMillis();
        
        // Check if the last message was sent today
        if (isSameDay(lastMessageDate, currentTime)) {
            return; // Already sent a message today
        }
        
        // Check if the last message was sent yesterday
        if (isYesterday(lastMessageDate, currentTime)) {
            currentStreak++;
        } else {
            currentStreak = 1; // Reset streak if more than one day has passed
        }
        
        // Save the new streak and current time
        preferences.edit()
            .putInt(KEY_CURRENT_STREAK, currentStreak)
            .putLong(KEY_LAST_MESSAGE_DATE, currentTime)
            .apply();
    }

    private boolean isSameDay(long time1, long time2) {
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal1.setTimeInMillis(time1);
        cal2.setTimeInMillis(time2);
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    }

    private boolean isYesterday(long time1, long time2) {
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal1.setTimeInMillis(time1);
        cal2.setTimeInMillis(time2);
        cal1.add(java.util.Calendar.DAY_OF_YEAR, 1);
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    }
} 