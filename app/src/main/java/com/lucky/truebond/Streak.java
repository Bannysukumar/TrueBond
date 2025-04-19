package com.lucky.truebond;

import java.util.Calendar;
import java.util.Date;

public class Streak {
    private String userId;
    private String partnerId;
    private int currentStreak;
    private Date lastMessageDate;
    private int longestStreak;
    private boolean isActive;

    public Streak() {
        // Default constructor required for Firebase
    }

    public Streak(String userId, String partnerId) {
        this.userId = userId;
        this.partnerId = partnerId;
        this.currentStreak = 0;
        this.lastMessageDate = new Date();
        this.longestStreak = 0;
        this.isActive = false;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Date getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(Date lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void updateStreak() {
        Calendar today = Calendar.getInstance();
        Calendar lastMessage = Calendar.getInstance();
        lastMessage.setTime(lastMessageDate);

        // Reset time parts to compare only dates
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        lastMessage.set(Calendar.HOUR_OF_DAY, 0);
        lastMessage.set(Calendar.MINUTE, 0);
        lastMessage.set(Calendar.SECOND, 0);
        lastMessage.set(Calendar.MILLISECOND, 0);

        long diffInMillis = today.getTimeInMillis() - lastMessage.getTimeInMillis();
        long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

        if (diffInDays == 1) {
            // Consecutive day
            currentStreak++;
            if (currentStreak > longestStreak) {
                longestStreak = currentStreak;
            }
            isActive = true;
        } else if (diffInDays > 1) {
            // Streak broken
            currentStreak = 1;
            isActive = true;
        }

        lastMessageDate = new Date();
    }
} 